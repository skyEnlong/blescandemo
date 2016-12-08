package com.communication.ble;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Message;
import android.util.SparseArray;

import com.communication.bean.CodoonShoesMinuteModel;
import com.communication.bean.CodoonShoesModel;
import com.communication.common.BaseCommand;
import com.communication.common.BaseCommandHelper;
import com.communication.data.AccessoryDataParseUtil;
import com.communication.data.AccessoryValues;
import com.communication.data.CLog;
import com.communication.data.DeviceUpgradeCallback;
import com.communication.gpsband.GpsBandParseUtil;
import com.communication.shoes.CodoonShoesCommand;
import com.communication.shoes.CodoonShoesParseHelper;
import com.communication.shoes.ShoseCommand;
import com.communication.shoes.ShoseUpGradeMangaer;
import com.communication.util.CodoonEncrypt;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


/**
 * Created by enlong on 2016/12/7.
 */

public class CodoonShoesSyncManager extends BaseDeviceSyncManager {
    private ICodoonShoesCallBack mICodoonShoesCallBack;
    private BaseCommandHelper commandHelper;
    private ShoseUpGradeMangaer mShoseUpGradeMangaer;

    protected ByteArrayOutputStream mBaos;
    private boolean isStartBoot;

    private  SparseArray runDatas;
    private  SparseArray stepDatas;

    private CodoonShoesParseHelper mParseHelper;
    /**
     * 每次读取帧数的最大帧数
     */
    private int FRAME_BLOCK = 16;
    /**
     * 基础数据的总帧数
     */
    private int totalStepFrame;

    /**
     * 当前读取到的基础数据帧数
     */
    private int curStepFrame;

    /**
     * 跑步数据帧数
     */
    private int totalRunFrame;

    /**
     * 当前读取到的跑步数据帧数
     */
    private int currRunFrame;


    /**
     * 跑步数据每帧数据最大长度
     */
    private int runDataEachLen;

    /**
     * 上一次解析的跑步数据索引
     */
    private int lastParseFrame = -1;


    /**
     * @param mContext
     * @param mCallBack can't be null
     */
    public CodoonShoesSyncManager(Context mContext, ICodoonShoesCallBack mCallBack) {
        super(mContext, mCallBack);
        this.mICodoonShoesCallBack = mCallBack;
        mTimeoutCheck.setTimeout(10000);
        commandHelper = new BaseCommandHelper();
        runDatas = new SparseArray();
        stepDatas = new SparseArray();
    }

    @Override
    protected boolean handMessage(Message messagge) {
        return false;
    }



    protected BaseBleManager initBleManager() {
        bleManager = new CodoonShoesBleManger(mContext);

        bleManager.setConnectCallBack(this);
        bleManager.setWriteCallback(this);

        return bleManager;
    }


    private void getShoseDataByFrame(int shoseDataIndex) {
        byte[] bytes = new byte[]{(byte)((shoseDataIndex >> 8) & 0xff),
                (byte)(shoseDataIndex & 0xff)
        };
        writeDataToDevice(commandHelper.getCommand(
                ShoseCommand.CODE_SHOES_DETAIL,
                bytes));

    }

    public void stopUpgrade() {
        if(null != mShoseUpGradeMangaer) mShoseUpGradeMangaer.stop();
        isStartBoot = false;

        bleManager.setWriteCallback(this);
        bleManager.setConnectCallBack(this);

    }

    public void startUpgrade(BluetoothDevice device, String bootfile, DeviceUpgradeCallback upgradeCallback) {
        isStartBoot = true;
        CLog.i(TAG, "startUpgrade file:" + bootfile);
        mHandler.removeMessages(SEND_DATA);
        lastData = null;
        mTimeoutCheck.stopCheckTimeout();
        if(null != mShoseUpGradeMangaer){
            mShoseUpGradeMangaer.stop();
            mShoseUpGradeMangaer = null;
        }
        mShoseUpGradeMangaer = new ShoseUpGradeMangaer(mContext,
                upgradeCallback, bleManager, mICodoonShoesCallBack);

        mShoseUpGradeMangaer.setUpgradeFilePath(bootfile);
        if(bleManager.isConnect){

            mShoseUpGradeMangaer.startUpgrade();
        }else {
            mShoseUpGradeMangaer.startDevice(device);
        }
    }

    @Override
    protected void dealResponse(byte[] data) {

        if (null == data || data.length < 2) return;
        if ((data[0] & 0xff) == (0xAA)) {
            dealResCommand(data);
        } else if ((data[0] & 0xf0) == (0xB0)) {

            dealDataContent(data);
        }

    }

    /**
     * 拿到跑步数据的， 进行预处理 放入总缓存
     * @param data
     */
    private void dealDataContent(byte[] data) {
        int len = (data[0] & 0x0f) + 1;  //max is 16
        int frameH = data[1] & 0xff;
        int frameL = data[2] & 0xff;
        int resFrame = (frameH << 8) + frameL;

        byte[] resData = null;
        if(len > 0){
            resData = Arrays.copyOfRange(data, 3, 3 + len);
        }


        runDatas.put(resFrame, resData);
        int hasRead = runDatas.size();
        mICodoonShoesCallBack.onSyncDataProgress(hasRead * 100 / totalRunFrame);


        CLog.i("ble", " has receive run frame " + resFrame);
        if(currRunFrame == 0){ // all has receive
            dealRunData(runDatas, 0, lastParseFrame);

        }else if(resFrame == currRunFrame + FRAME_BLOCK -1){

            /**开始处理当前读取到的16贞**/
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            for(int i = currRunFrame; i<= resFrame; i++ ){
                byte[] datas = (byte[]) runDatas.get(i);
                byteArrayOutputStream.write(datas, 0 , datas.length);
            }

            int start_offset_frame = mParseHelper.findStartTags(byteArrayOutputStream.toByteArray());
            if(-1 != start_offset_frame){
                CLog.i(TAG, "we have find start frame");
                dealRunData(runDatas, currRunFrame + start_offset_frame,  lastParseFrame );
                lastParseFrame = currRunFrame + start_offset_frame;
            }

            byteArrayOutputStream.reset();

            // 这一次的16贞已经读取完毕。读取上16贞
            currRunFrame -= FRAME_BLOCK;
            currRunFrame = (currRunFrame < 0) ? 0 : currRunFrame;
            getRunFromFrame(currRunFrame);
        }else {
            CLog.i(TAG, " waiting receive next run frame");
        }


    }

    private void dealRunData(SparseArray runDatas, int start, int end) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        for (int frameIndex = start; frameIndex < end; frameIndex++){
            byte[] datas = (byte[]) runDatas.get(frameIndex);
            byteArrayOutputStream.write(datas, 0 , datas.length);
        }
        byteArrayOutputStream.reset();
        List<CodoonShoesModel> ls = mParseHelper.parseData(byteArrayOutputStream.toByteArray());
        if(null != mICodoonShoesCallBack) mICodoonShoesCallBack.onGetRunSports(ls);
    }


    /**
     * 处理记步数据
     * @param stepDatas
     */
    private void dealStepDatas(SparseArray stepDatas) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        for (int frameIndex = 0; frameIndex < totalStepFrame; frameIndex++){
            byte[] datas = (byte[]) stepDatas.get(frameIndex);
            for (byte data : datas){
                mBaos.write(CodoonEncrypt.encryptMyxor(data, mBaos.size() % 6));
            }

            byteArrayOutputStream.write(datas, 0 , datas.length);

        }

        AccessoryDataParseUtil decode = AccessoryDataParseUtil.getInstance(mContext);
        HashMap<String, AccessoryValues> res = decode.analysisDatas(byteArrayOutputStream.toByteArray());

        mICodoonShoesCallBack.onSyncDataOver(res, mBaos);
        byteArrayOutputStream.reset();
        totalStepFrame = -1;
        curStepFrame = -1;
    }

    /**
     * 读取记步数据
     * @param curStepFrame
     */
    private void getStepFromFrame(int curStepFrame) {

        CLog.i("ble", "get step frame start by:" + curStepFrame);


        byte[] bytes = new byte[]{(byte)((curStepFrame >> 8) & 0xff),
                (byte)(curStepFrame & 0xff)
        };

        writeDataToDevice(commandHelper.getCommand(
                BaseCommand.CODE_READ_STEP_FRAME,
                bytes));

    }


    /**
     * 读取记步数据
     * @param curStepFrame
     */
    private void getRunFromFrame(int curStepFrame) {

        CLog.i("ble", "get run frame start by:" + curStepFrame);

        byte[] bytes = new byte[]{(byte)((curStepFrame >> 8) & 0xff),
                (byte)(curStepFrame & 0xff)
        };

        writeDataToDevice(commandHelper.getCommand(
                CodoonShoesCommand.CODE_READ_RUN_FRAME,
                bytes));

    }

    private void dealResCommand(byte[] data) {
        if(CLog.isDebug)
        if(null != mICodoonShoesCallBack) mICodoonShoesCallBack.onResponse(data);

        mTimeoutCheck.stopCheckTimeout();

        int resKey = data[1];
        int len = data[2] & 0xff;
        byte[] resData = null;
        if(len > 0){
            resData = Arrays.copyOfRange(data, 3, 3 + len);
        }

        switch (resKey){
            case BaseCommand.RES_BIND:
                mICodoonShoesCallBack.onBindSucess();
                break;

            case BaseCommand.RES_READ_VERSION:
                String hVersion = (resData[1] & 0xff) + "." + (resData[2] & 0xff);

                 if(len == 5){
                    hVersion = hVersion + "_" + resData[3] + "." + resData[4];
                }
                mICodoonShoesCallBack.onGetVersion(hVersion);
                break;

            case BaseCommand.RES_READ_ID:

                mICodoonShoesCallBack.onGetDeviceID(GpsBandParseUtil.getDeviceId(resData));
                break;

            case BaseCommand.RES_UPDATE_USER_INFO:
                int height = resData[0] & 0xff;
                int weight = resData[1] & 0xff;
                int age = resData[2] & 0xff;
                int gender = resData[3] & 0xff;
                CLog.i("ble_receive", String.format(" height %d, weight %d, age %d, gender %d ", height, weight, age, gender));
                mICodoonShoesCallBack.onUpdateUserinfoSuccessed();
                break;

            case BaseCommand.RES_UPADTE_TIME:
                mICodoonShoesCallBack.onUpdateTimeSuccessed();
                break;

            case CodoonShoesCommand.RES_READY_DATA:
                mICodoonShoesCallBack.onShoesDataSyncRedy();
                break;

            case BaseCommand.RES_TOTAL_STEP_FRAME: {
                int frameType = resData[0] & 0xff;
                int frameH = resData[1] & 0xff;
                int frameL = resData[2] & 0xff;
                totalStepFrame = (frameH << 8) + frameL;
                CLog.i("ble", "total step frame:" + totalStepFrame);

                if(totalStepFrame > 0){
                    curStepFrame = totalStepFrame - FRAME_BLOCK;
                    curStepFrame = (curStepFrame < 0) ? 0 : curStepFrame;
                    getStepFromFrame(curStepFrame);
                }else {
                    mICodoonShoesCallBack.onSyncDataProgress(100);

                    mICodoonShoesCallBack.onSyncDataOver(null, null);
                }

            }
                break;
            case BaseCommand.RES_READ_STEP_FRAME: {
                int frameH = resData[0] & 0xff;
                int frameL = resData[1] & 0xff;
                int frame = (frameH << 8) + frameL;
                byte[] steps = Arrays.copyOfRange(resData, 2, resData.length);
                stepDatas.put(frame, steps);

                int hasRead = stepDatas.size();
                mICodoonShoesCallBack.onSyncDataProgress(hasRead * 100 / totalStepFrame);

                CLog.i("ble", " has receive frame " + frame);
                if(curStepFrame == 0){ // all has receive
                    dealStepDatas(stepDatas);
                }else if(frame == curStepFrame + FRAME_BLOCK -1){
                    // 这一次的16贞已经读取完毕。读取上16贞
                    curStepFrame -= FRAME_BLOCK;
                    curStepFrame = (curStepFrame < 0) ? 0 : curStepFrame;
                    getStepFromFrame(curStepFrame);
                }else {
                    CLog.i(TAG, " waiting receive next step frame");
                }
             }
                break;

            case BaseCommand.RES_CLEAR_SPORT_DATA:
                mICodoonShoesCallBack.onClearDataSuccessed();
                break;

            case CodoonShoesCommand.RES_ACCESSORY_BD:
                mICodoonShoesCallBack.onAccessoryBDSuccess(resData[0] & 0xff);
                break;

            case CodoonShoesCommand.RES_START_RUN:
                mICodoonShoesCallBack.onStartRunResult(resData[0] & 0xff);
                break;

            case CodoonShoesCommand.RES_STOP_RUN:
                mICodoonShoesCallBack.onStopRun();
                break;

            case CodoonShoesCommand.RES_RUN_DATA_TOTAL_FRAME:
                runDataEachLen = resData[0] & 0xff;
                int frameH = resData[1] & 0xff;
                int frameL = resData[2] & 0xff;
                totalRunFrame = (frameH << 8) + frameL;
                lastParseFrame = totalRunFrame;
                CLog.i("ble", "total step frame:" + totalStepFrame + " each framLen " + runDataEachLen);
                if(totalRunFrame > 0){
                    currRunFrame = totalRunFrame - FRAME_BLOCK;
                    currRunFrame = (currRunFrame < 0) ? 0 : currRunFrame;
                    getStepFromFrame(currRunFrame);
                }else {
                    mICodoonShoesCallBack.onSyncDataProgress(100);

                    mICodoonShoesCallBack.onSyncDataOver(null, null);
                }
                break;
            case CodoonShoesCommand.RES_SHOES_STAE:

                mICodoonShoesCallBack.onGetShoesState(CodoonShoesParseHelper.parseState(resData));
                break;

            case CodoonShoesCommand.RES_SHOES_TOTAL_RUN:

                ByteBuffer byteBuffer = ByteBuffer.wrap(resData).order(ByteOrder.BIG_ENDIAN);

                int total = byteBuffer.getInt();
                mICodoonShoesCallBack.onGetTotalRun(total);
                break;

            case CodoonShoesCommand.RES_RUN_STATE_DATA:
                CodoonShoesMinuteModel model = mParseHelper.parseMinutePercents(resData);
                mICodoonShoesCallBack.onGetRunState(model);
                break;

        }

    }


    @Override
    public void stop(){
         super.stop();
        stopUpgrade();
        runDatas.clear();
        stepDatas.clear();
        currRunFrame = -1;
        curStepFrame = -1;
        lastParseFrame = -1;
        totalRunFrame = -1;
        totalStepFrame = -1;
    }

    @Override
    public void onReSend() {
        if(curStepFrame != totalStepFrame){
            CLog.i(TAG, " re read step");
            for(int i = curStepFrame; i < curStepFrame + FRAME_BLOCK && i < totalStepFrame; i++){
                stepDatas.remove(i);
            }
        }else if(currRunFrame != totalRunFrame){
            CLog.i(TAG, " re read run");
            for(int i = currRunFrame; i < currRunFrame + FRAME_BLOCK && i < totalRunFrame; i++){
                stepDatas.remove(i);
            }
        }

        super.onReSend();


    }
}
