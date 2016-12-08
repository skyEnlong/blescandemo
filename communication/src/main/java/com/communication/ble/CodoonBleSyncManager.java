package com.communication.ble;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Message;
import android.util.Log;

import com.communication.data.AccessoryDataParseUtil;
import com.communication.data.AccessoryValues;
import com.communication.data.CLog;
import com.communication.data.DataUtil;
import com.communication.data.DeviceUpgradeCallback;
import com.communication.data.ISyncDataCallback;
import com.communication.data.SendData;
import com.communication.data.TransferStatus;
import com.communication.gpsband.GpsBandParseUtil;
import com.communication.util.CodoonEncrypt;
import com.communication.util.CommonUtils;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

public class CodoonBleSyncManager extends BaseDeviceSyncManager {

    protected final String TAG = "ble";
    protected int frameCount = 0;
    protected int indexFrame = 0;
    protected ByteArrayOutputStream mBaos;
    protected ISyncDataCallback mISyncDataCallback;
    protected ArrayList<ArrayList<Integer>> mRecordDatas;
    protected CodoonDeviceUpgradeManager upgradeManager;
    protected boolean isStartBoot;

    public CodoonBleSyncManager(Context context, ISyncDataCallback callback) {

        super(context, callback);

        mISyncDataCallback = callback;

    }

    @Override
    protected boolean handMessage(Message messagge) {
        return false;
    }

    @Override
    protected void dealResponse(byte[] data) {
        if (isStart) {
            ArrayList<Integer> list = new ArrayList<Integer>();
            for (int i = 0; i < data.length; i++) {

                list.add(data[i] & 0xff);
            }
            DataUtil.DebugPrint(data);
            analysis(list);
        } else {
            CLog.d(TAG, " isStart:" + isStart);
        }
    }

    @Override
    protected BaseBleManager initBleManager() {
        bleManager = new CodoonBleManager(mContext);
        bleManager.setWriteCallback(this);
        bleManager.setConnectCallBack(this);
        
        return bleManager;
    }


    public void startUpgrade(BluetoothDevice device, String bootfile, DeviceUpgradeCallback upgradeCallback){
        isStartBoot = true;
        mTimeoutCheck.stopCheckTimeout();
        if(null != upgradeManager){
            upgradeManager.stop();
            upgradeManager = null;
        }
        upgradeManager = new CodoonDeviceUpgradeManager(mContext,
                upgradeCallback, bleManager, mISyncDataCallback);

        upgradeManager.setUpgradeFilePath(bootfile);
        if(bleManager.isConnect){

            upgradeManager.startUpgrade();
        }else {
            upgradeManager.startDevice(device);
        }
    }

    public void stopUpgrade(){
        isStartBoot = false;
        if(null != upgradeManager){
            upgradeManager.stop();
            upgradeManager = null;
        }

        bleManager.setWriteCallback(this);
        bleManager.setConnectCallBack(this);
    }

    public void start(int[] order) {
        isStart = true;
        SendDataToDevice(order);
        mTimeoutCheck.setIsConnection(true);
    }

    /**
     *
     */
    public void start(BluetoothDevice device) {
        super.startDevice(device);
    }


    /**
     * send data from application to device
     *
     * @param datas
     */
    public void SendDataToDevice(final int[] datas) {
        isStart = true;
        writeDataToDevice(CommonUtils.intToByte(datas));
    }



    /**
     * @param datas
     */
    public void analysis(ArrayList<Integer> datas) {
        CLog.d(TAG, "codoonble analysis datas");
        if (datas == null) {
            mISyncDataCallback.onNullData();
        } else {
            final int msgID = datas.get(1);
            switch (msgID) {
                case TransferStatus.RECEIVE_CONNECTION_ID:
                    mTimeoutCheck.stopCheckTimeout();
                    mTimeoutCheck.setTryConnectCounts(3);
                    mTimeoutCheck.setIsConnection(false);
                    mTimeoutCheck.setTimeout(3000);

                    mISyncDataCallback.onConnectSuccessed();
                    break;

                case TransferStatus.RECEIVE_BINED_ID:
                    mTimeoutCheck.stopCheckTimeout();
                    mISyncDataCallback.onBindSucess();
                    break;

                case TransferStatus.RECEIVE_GETVERSION_ID:
                    mTimeoutCheck.stopCheckTimeout();
                    String hVersion = datas.get(4) + "." + datas.get(5);
                    mISyncDataCallback.onGetVersion(hVersion);
                    break;
                case TransferStatus.RECEIVE_DEVICE_ID:
                    mTimeoutCheck.stopCheckTimeout();
                    String deviceid = getDeviceID(datas);
                    mISyncDataCallback.onGetDeviceID(deviceid);
                    break;
                case TransferStatus.RECEIVE_UPDATETIME_ID:
                    mTimeoutCheck.stopCheckTimeout();
                    mISyncDataCallback.onUpdateTimeSuccessed();
                    break;

                case TransferStatus.RECEIVE_DEVICE_TIME_ID:
                    mTimeoutCheck.stopCheckTimeout();
                    ArrayList<Integer> content = new ArrayList<>(
                            datas.subList(3,3 + datas.get(2)));
                    content.add(0, 0x20);
                    long time = GpsBandParseUtil.getSysTime(content);
                    SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

                    mISyncDataCallback.onGetDeviceTime(mFormat.format(new Date(time)));
                    break;
                case TransferStatus.RECEIVE_CLEARDATA_ID:
                    mTimeoutCheck.stopCheckTimeout();
                    mISyncDataCallback.onClearDataSuccessed();

                    break;
                case TransferStatus.RECEIVE_FRAMECOUNT_ID:
                    mTimeoutCheck.stopCheckTimeout();
                    frameCount = (datas.get(4) << 8) + datas.get(5);     //careful the priority of << is lower than + ;
                    indexFrame = 0;
                    mBaos = new ByteArrayOutputStream();
                    Log.d(TAG, " framecount:" + frameCount);
                    // read data after get the frame count
                    if (frameCount > 0) {

                        SendDataToDevice(SendData.getPostReadSportData(indexFrame));

                        mRecordDatas = new ArrayList<ArrayList<Integer>>();
                    } else {
                        mISyncDataCallback.onSyncDataProgress(100);

                        mISyncDataCallback.onSyncDataOver(null, null);
                    }
                    break;
                case TransferStatus.RECEIVE_READDATA_ID:
                    mTimeoutCheck.stopCheckTimeout();
                    // upload data from device
                    final int length = datas.get(2);

                    ArrayList<Integer> currentDatas = new ArrayList<Integer>();
                    for (int i = 3; i < length + 3; i++) {
                        mBaos.write(CodoonEncrypt.encryptMyxor(datas.get(i), mBaos.size() % 6));
                        currentDatas.add(datas.get(i));
                    }
                    mRecordDatas.add(currentDatas);
                    ++indexFrame;

                    mISyncDataCallback.onSyncDataProgress(indexFrame * 100 / frameCount);

                    if (indexFrame < frameCount) {
                        SendDataToDevice(SendData
                                .getPostReadSportData(indexFrame));

                    } else {
                        frameCount = 0;
                        indexFrame = 0;

                        mISyncDataCallback.onSyncDataProgress(100);
                        AccessoryDataParseUtil decode = AccessoryDataParseUtil.getInstance(mContext);
                        HashMap<String, AccessoryValues> data = decode.analysisDatas(mRecordDatas);

                        mISyncDataCallback.onSyncDataOver(data, mBaos);
                    }
                    break;

                case TransferStatus.RECEIVE_GETUSERINFO_ID:
                    mTimeoutCheck.stopCheckTimeout();
                    getUserInfo(datas);
                    break;

                case TransferStatus.RECEIVE_UPDATEUSERINFO_SUCCESS_ID:
                    mTimeoutCheck.stopCheckTimeout();
                    mISyncDataCallback.onUpdateUserinfoSuccessed();
                    break;

                case TransferStatus.RECEIVE_UPDATEUSERINFO2_SUCCESS_ID:
                    mTimeoutCheck.stopCheckTimeout();
                    CLog.d(TAG, "update alarm and activity remind success.");
                    mISyncDataCallback.onUpdateAlarmReminderSuccessed();

                    break;
                case TransferStatus.RECEIVE_GETUSERINFO2_ID:
                    mTimeoutCheck.stopCheckTimeout();
                    int battery = 0;
                    if (datas.size() > 15) {
                        battery = datas.get(15);
                    } else {
                        battery = datas.get(10);
                    }

                    mISyncDataCallback.onBattery(battery);

                    break;
                case TransferStatus.RECEIVE_FRIENDS_REQUEST_ID:
                    SendDataToDevice(SendData
                            .postBlueFriendWarning());
                    break;
                case TransferStatus.RECEIVE_FRIENDS_WARNING_ID:
                    mTimeoutCheck.stopCheckTimeout();
                    mISyncDataCallback.onFriedWarningSuccess();
                    break;
//			case TransferStatus.REICEIVE_FRIENDS_SWITCH_ID:
//				mTimeoutCheck.stopCheckTimeout();
//				for(ISyncDataCallback mCallback: mISyncDataCallback){
//					mCallback.onSetFrindSwitchOver();
//				}
//				break;
                default:
                    CLog.d(TAG, "on get other datas");
                    mISyncDataCallback.onGetOtherDatas(datas);
                    break;
            }
        }
    }


    /**
     * @param datas
     */
    protected void getUserInfo(ArrayList<Integer> datas) {
        int height = datas.get(3);
        int weigh = datas.get(4);
        int age = datas.get(5);
        int gender = datas.get(6);
        int stepLength = datas.get(7);
        int runLength = datas.get(8);
        int sportType = datas.get(11);
        int goalValue = datas.get(12) << 8;
        goalValue += datas.get(13);

        mISyncDataCallback.onGetUserInfo(height, weigh, age, gender, stepLength,
                runLength, sportType, goalValue);
    }

    /**
     * @param datas
     * @return
     */
    protected String countTime(ArrayList<Integer> datas) {
        int year = 2000 + Integer.parseInt(Integer.toHexString(datas.get(3)));
        int month = Integer.parseInt(Integer.toHexString(datas.get(4))) -1;
        int day = Integer.parseInt(Integer.toHexString(datas.get(5)));
        int hour = Integer.parseInt(Integer.toHexString(datas.get(6)));
        int minute = Integer.parseInt(Integer.toHexString(datas.get(7)));
        int second = Integer.parseInt(Integer.toHexString(datas.get(8)));

        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getDefault());
        cal.set(year, month, day, hour, minute, second);

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format.setTimeZone(TimeZone.getDefault());
        return format.format(new Date(cal.getTimeInMillis()));
    }

    /**
     * @param list
     * @return
     */
    protected String getDeviceID(ArrayList<Integer> list) {
        StringBuilder append = new StringBuilder();
        append.append(list.get(3));
        append.append("-");

        append.append(((list.get(4) & 0xff) << 8) + (list.get(5) & 0xff));
        append.append("-");

        append.append(((list.get(6) & 0xff) << 8) + (list.get(7) & 0xff));
        append.append("-");

        append.append(((list.get(8) & 0xff) << 8) + (list.get(9) & 0xff));
        append.append("-");

        append.append(list.get(10) & 0xff);
        append.append("-");

        append.append(((list.get(11) & 0xff) << 8) + (list.get(12) & 0xff));
        append.append("-");

        append.append(((list.get(13) & 0xff) << 8) + (list.get(14) & 0xff));
        append.append("-");

        append.append(list.get(15) & 0xff);

        return append.toString();
    }

    public void getDeviceTotalInfo(){

    }

    @Override
    public void stop(){
        if(null != mRecordDatas ) mRecordDatas.clear();
        super.stop();
        stopUpgrade();

    }

}
