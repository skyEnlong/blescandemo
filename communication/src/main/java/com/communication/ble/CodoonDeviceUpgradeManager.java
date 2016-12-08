package com.communication.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.communication.bean.CodoonProfile;
import com.communication.data.CLog;
import com.communication.data.DataUtil;
import com.communication.data.DeviceUpgradeCallback;
import com.communication.data.ISyncCallBack;
import com.communication.data.SendData;
import com.communication.data.TimeoutCheck;
import com.communication.data.TransferStatus;
import com.communication.util.CommonUtils;
import com.communication.util.MobileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class CodoonDeviceUpgradeManager extends BaseDeviceSyncManager implements Runnable {

    protected static final String TAG = "CodoonDeviceUpgradeManager";
    private DeviceUpgradeCallback upgradeCallback;


    private boolean isBootMode;
    private int frame;
    private int totalFrame;
    private static int EACH_PART_NUM = 12;
    private String filePath;

    private boolean isVerify;

    private int checkData = 0;
    byte[] buffer = null;

    private FileInputStream input;

    private final int MSG_CONNECT_SUCCESS = 0xa11a1;

    public CodoonDeviceUpgradeManager(Context context,
                                      DeviceUpgradeCallback upgradeCallback,
                                      BaseBleManager bleManager,
                                      ISyncCallBack callBack) {
        super(context, callBack);
        this.bleManager = bleManager;
        this.upgradeCallback = upgradeCallback;
        bleManager.setWriteCallback(this);
        bleManager.setConnectCallBack(this);
        buffer = new byte[EACH_PART_NUM];
    }




    @Override
    public void onNotifySuccess() {
        // TODO Auto-generated method stub
        mTimeoutCheck.stopCheckTimeout();
        mHandler.sendEmptyMessageDelayed(MSG_CONNECT_SUCCESS, 1200);

    }

    @Override
    public void connectState(BluetoothDevice device, int status,
                             int newState) {
        // TODO Auto-generated method stub

        if (newState == BluetoothAdapter.STATE_CONNECTED) {

            if (!isBootMode && "Cboot".equalsIgnoreCase(device.getName())) {
                isBootMode = true;
            }

        }
    }


    public void startUpgrade(){
        mHandler.sendEmptyMessage(MSG_CONNECT_SUCCESS);
    }

    @Override
    protected boolean handMessage(Message messagge) {
        switch (messagge.what){
            case MSG_CONNECT_SUCCESS:

                if (isBootMode) {
                    writeDataToDevice(
                            CommonUtils.intToByte(
                                    (SendData.postConnectBootOrder())));
                } else {

                    writeDataToDevice(
                            CommonUtils.intToByte(SendData.postBootMode()));
                }

                break;
        }
        return false;
    }

    @Override
    protected void dealResponse(byte[] data) {

        if(!isStart) return;

        ArrayList<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < data.length; i++) {

            list.add(data[i] & 0xff);
        }
        DataUtil.DebugPrint(data);
        analysis(list);
    }

    @Override
    protected BaseBleManager initBleManager() {
        return bleManager;
    }


    /**
     *
     */
    public void stop() {
        isStart = false;
        isBootMode = false;
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
            upgradeCallback.onCheckBootResult(true, 0);
        } else {
            upgradeCallback.onTimeOut();
        }
    }


    /**
     * @param datas
     */
    @SuppressWarnings("deprecation")
    protected void analysis(ArrayList<Integer> datas) {

        if (datas == null) {

        } else {
            final int msgID = datas.get(1);
            switch (msgID) {

                case TransferStatus.RECEIVE_BOOT_STATE_ID:
                    upgradeCallback.onChangeToBootMode();

                    isBootMode = true;

                    bleManager.disconnect();
                    mTimeoutCheck.stopCheckTimeout();

                    mHandler.removeMessages(BLE_CONNECT);
                    mHandler.sendEmptyMessageDelayed(BLE_CONNECT, 1200);

                    break;


                case TransferStatus.RECEIVE_BOOT_VERSION_ID:

                    if (null == datas || datas.size() < 6) {
                        onReceivedFailed();
                        return;
                    }
                    mTimeoutCheck.stopCheckTimeout();

                    upgradeCallback.onGetBootVersion(datas.get(4) + "." + datas.get(5));

                    CLog.d(TAG, "onGetBootVersion" + datas.get(4) + "." + datas.get(5));

                    frame = 0;

                    calcToatals();
                    sendData();

                    break;

                case TransferStatus.RECEIVE_BOOT_CONNECT_ID:

                    upgradeCallback.onConnectBootSuccess();
                    CLog.d(TAG, "begint to get boot version");
                    writeDataToDevice(CommonUtils.intToByte(
                            SendData.postConnectBootVersion()));
                    isBootMode = true;


                    break;


                case TransferStatus.RECEIVE_BOOT_UPGRADE_ID:

                    upgradeCallback.onWriteFrame(frame, totalFrame);
                    int frame_back = datas.get(3) << 8 + datas.get(4);

                    if (checkBackIsRight(datas)) {
                        frame++;

                        if (!sendData()) {

                            try {
                                input.close();
                                input = null;
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            isVerify = true;
                            writeDataToDevice(CommonUtils.intToByte(
                                    (SendData.postBootEnd((int)
                                            (checkData & 0x0000FFFF)))));
                            mTimeoutCheck.setTimeout(TIME_OUT / 2);

                        }
                    } else {
                        CLog.e(TAG, "frame: err:" + frame);
                        onReSend();
                    }


                    break;

                case TransferStatus.RECEIVE_UPGRADE_OVER_ID:
                    boolean result = parseIsUpSuccess(datas);
                    upgradeCallback.onCheckBootResult(result, 0);
                    break;

                default:
                    if (isVerify) {
                        upgradeCallback.onCheckBootResult(true, 0);
                        isVerify = false;
                    } else {
                        onReSend();
                    }
                    break;
            }
        }
    }

    private boolean checkBackIsRight(ArrayList<Integer> datas) {
        // TODO Auto-generated method stub
        return true;
    }


    /**
     * 0x00 success else failed
     *
     * @param datas
     * @return
     */
    private boolean parseIsUpSuccess(ArrayList<Integer> datas) {
        // TODO Auto-generated method stub
        if (null == datas || datas.size() < 5) {
            return false;
        }
        int check = datas.get(3);

        return (check == 0);
    }

    public String getUpgradeFilePath() {
        return filePath;
    }

    public void setUpgradeFilePath(String filePath) {
        this.filePath = filePath;
    }


    @Override
    public void run() {
        // TODO Auto-generated method stub

    }

    public void calcToatals() {
        File file = new File(filePath);
        if (!file.exists()) {
            return;
        }

        totalFrame = (int) (file.length() / (EACH_PART_NUM));
        frame = 0;
        checkData = 0;
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


    public boolean sendData() {

        try {
            int length = 0;
            if (-1 != (length = input.read(buffer))) {

                for (int i = 0; i < length; i++) {
                    checkData += (buffer[i] & 0x00ff);
                }
//					Log.d(TAG, "send frame is:" + frame + " total:" + totalFrame + " length:" + length);
                if (length < buffer.length) {
                    for (int i = length; i < buffer.length; i++) {
                        buffer[i] = 0;
                    }
                }
                writeDataToDevice(CommonUtils.intToByte((
                         SendData.postBootUploadData(frame, buffer, buffer.length))));
            } else {
                return false;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        }
        return true;
    }


}
