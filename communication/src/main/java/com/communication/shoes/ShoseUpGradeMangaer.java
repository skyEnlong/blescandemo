package com.communication.shoes;

import android.content.Context;
import android.os.Message;

import com.communication.ble.BaseBleManager;
import com.communication.ble.BaseDeviceSyncManager;
import com.communication.common.BaseCommandHelper;
import com.communication.data.CLog;
import com.communication.data.DeviceUpgradeCallback;
import com.communication.data.ISyncCallBack;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by 0H7RXL on 2016/5/27.
 */
public class ShoseUpGradeMangaer extends BaseDeviceSyncManager {
    private int curFrame;
    private int totalFrame;
    private static int EACH_PART_NUM = 14;
    private DeviceUpgradeCallback upgradeCallback;
    private String filePath;

    private boolean isVerify;

    private int crc = 0;
    private long fileSize = 0;
    private int retryCount = 0;
    public static  final int DEFAULT_COUNT = 3;
    private FileInputStream input;
    private BaseCommandHelper commandHelper;

    private boolean isTransData;

    public ShoseUpGradeMangaer(Context context,
                               DeviceUpgradeCallback upgradeCallback,
                               BaseBleManager bleManager,
                               ISyncCallBack callBack) {
        super(context, callBack);
        this.bleManager = bleManager;
        this.upgradeCallback = upgradeCallback;
        bleManager.setWriteCallback(this);
        bleManager.setConnectCallBack(this);
        setFrameDelay(0);
        commandHelper = new BaseCommandHelper();

    }

    @Override
    protected boolean handMessage(Message messagge) {
        if(messagge.what == NOTIFY_SUCEESS){
            startUpgrade();
            return true;
        }
        return false;
    }

    @Override
    protected void dealResponse(byte[] res) {
        if(null != res && res.length > 3){
            int resCode = res[1] & 0xff;
            int lenth = res[2] &  0xff;
            byte[] content = Arrays.copyOfRange(res, 3, 3 + lenth);

            switch (resCode){
                case ShoseCommand.RES_BOOT_CHANGE:
                    writeDataToDevice(commandHelper.getCommand(ShoseCommand.CODE_BOOT_BEGIN_UPGRADE));

                    break;

                case ShoseCommand.RES_BOOT_BEGIN_UPGRADE:
                    initParams();

                    writeFrameToDevice(0);
                    break;


                case ShoseCommand.RES_BOOT_TRANS_DATA:
                    CLog.i("shose", "hase send 15 frame and curFrame:" + curFrame);
                    break;

                case ShoseCommand.RES_BOOT_TRANS_CHECK:
                    boolean isSuccess = (0 == (content[0] & 0xff));
                    if(!isSuccess){

                        retryCount++;
                        if(retryCount < DEFAULT_COUNT){  //最多升级三次
                            startUpgrade();
                        }
                    }else {
                        retryCount = 0; //超过三次，置0
                    }
                    stopTimeCheckOut();
                    upgradeCallback.onCheckBootResult(isSuccess, retryCount);

                    break;
                default:
                    CLog.e(TAG, "not need response");
                    break;
            }
        }
    }



    @Override
    public void onWriteSuccess(){

        CLog.i(TAG, "shoseUp onWriteSuccess");

        if(!isTransData) return;


            upgradeCallback.onWriteFrame(curFrame, totalFrame);
            curFrame++;
            writeFrameToDevice(curFrame);

    }


    /**
     * send crc check data to device
     * @param crc
     */
    private void writeCrcToDevice(int crc) {

        CLog.i(TAG, "crc：" + Integer.toHexString(crc));

        byte[] buff = new byte[2];
        buff[0] = (byte) ((crc >> 8)& 0xff);
        buff[1] = (byte) ((crc)& 0xff);
        writeDataToDevice(commandHelper.getCommand(ShoseCommand.CODE_BOOT_TRANS_CHECK, buff));
    }


    /**
     * send frame data to device
     * @param frame
     */
    private void writeFrameToDevice(int frame){

        CLog.i(TAG, "write frame:" + frame + " total:" + totalFrame);

        byte[] buffer = new byte[EACH_PART_NUM ];

        try {
            int len = input.read(buffer);
            CLog.i(TAG, "frame len = " + len);

            if(len != -1){
                isTransData = true;
                crc = ShoesParseHelper.CalCrc(buffer, len, crc);

                byte[] bufferSend = new byte[2 + len];
                bufferSend[0] = (byte) ((frame  >> 8) & 0xff);
                bufferSend[1] = (byte) ((frame & 0xff));
                for(int i = 2; i < bufferSend.length; i++){
                    bufferSend[i] = buffer[i -2];
                }

                writeDataToDevice(commandHelper.getCommand(ShoseCommand.CODE_BOOT_TRANS_DATA, bufferSend));

            }else {
                CLog.e(TAG, "read out file length, need send crc");
                isTransData = false;
                writeCrcToDevice(crc);
            }

        } catch (IOException e) {
            CLog.e(TAG, e.getMessage());
            e.printStackTrace();
        }

    }
    @Override
    protected BaseBleManager initBleManager() {
        return bleManager;
    }


    private void initParams() {
        File file = new File(filePath);
        if (!file.exists() || file.length() == 0) {
            return;
        }
        fileSize = file.length();

        totalFrame =  (int)(
                (0 == (fileSize % EACH_PART_NUM ))?
                        (fileSize / EACH_PART_NUM ) : (fileSize / EACH_PART_NUM + 1));
        curFrame = 0;
        crc = 0;
        if (null != input) {
            try {
                input.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        input = null;
        try {
            input = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }



    public void setUpgradeFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void startUpgrade(){
        CLog.i(TAG, "startUpgrade");
        writeDataToDevice(commandHelper.getCommand(ShoseCommand.CODE_BOOT_CHANGE));
    }

    /**
     *
     */
    public void stop() {
        isTransData = false;
        retryCount = 0;
        if (null != mTimeoutCheck) {
            mTimeoutCheck.stopCheckTimeout();
        }
        if (bleManager != null) {
            bleManager.close();
        }
        if (null != input) {
            try {
                input.close();
            } catch (Exception e) {

            }
        }

    }


    @Override
    public void onReceivedFailed() {
        CLog.d(TAG, "receivedFailed()");
        if (isVerify) {
            isVerify = false;
            stopTimeCheckOut();
            upgradeCallback.onCheckBootResult(true, retryCount);
        } else {
            upgradeCallback.onTimeOut();
        }
    }


}
