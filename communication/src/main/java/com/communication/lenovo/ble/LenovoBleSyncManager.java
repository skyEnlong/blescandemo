package com.communication.lenovo.ble;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.communication.ble.BaseCommunicationManager;
import com.communication.ble.IConnectCallback;


import com.communication.data.AccessoryConfig;
import com.communication.data.AccessoryDataParseUtil;
import com.communication.data.AccessoryValues;
import com.communication.data.CLog;
import com.communication.data.ISyncDataCallback;
import com.communication.data.TimeoutCheck;
import com.communication.provider.HeartBean;
import com.communication.provider.HeartRateHelper;


public class LenovoBleSyncManager extends BaseCommunicationManager {

    private String TAG = "CodoonBleSyncManager";

    private LenovoBleManager mBleManager;


    private String mClicentUUID, mCharacteristicUUID;

    private String mWriteClicentUUID = "00001500-0000-1000-8000-00805F9B34FB",
            mWriteCharacteristicUUID = "00001530-0000-1000-8000-00805F9B34FB";

    private ByteArrayOutputStream mBaos;
    private Handler mHandler;
    private BluetoothDevice mDevice;

    private boolean isStart;
    private final int TIME_OUT = 12000;
    private final int CONNECT_AGAIN = 2;
    private final int ORDER_CONNECT = 1;
    private final int TIME_OUT_CALL_BACK = 0x111;
    private final int GET_SLEEP_DATA = 11113;
    private final int GET_DAILY_DATA = 11114;
    private IConnectCallback connectBack;

    private List<ISyncDataCallback> mISyncDataCallbacks;

    private LenovoCommandStatus analysisReplay;
    protected ArrayList<ArrayList<Integer>> mSportDatas;
    private int lastMsgID = LenovoCommandStatus.CONNECT_SUCCESS;
    private ArrayList<Integer> oneSportData;
    private ArrayList<Integer> oneSleepData;
    private int lastSportID;
    private ArrayList<HeartBean> hearRateData;

    private String deviceId;
    private boolean isSendGetSleepCommand = false;
    private boolean isSendGetDailyCommand = false;

    public void register(ISyncDataCallback callback) {
        if (null != callback && !mISyncDataCallbacks.contains(callback)) {
//			mISyncDataCallback.add(callback);
        }
    }

    public LenovoBleSyncManager(Context context, ISyncDataCallback callback) {

        mSportDatas = new ArrayList<ArrayList<Integer>>();
        mISyncDataCallbacks = new ArrayList<ISyncDataCallback>();
        mISyncDataCallbacks.add(callback);
        mBaos = new ByteArrayOutputStream();
        mRecordDatas = new ArrayList<ArrayList<Integer>>();
        analysisReplay = new LenovoCommandStatus();
        // TODO Auto-generated constructor stub
        mHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                super.handleMessage(msg);
                if (msg.what == ORDER_CONNECT) {

//					SendDataToDevice(SendData
//							.getPostDeviceID());
                    if (isStart) {
                        SendDataToDevice(LenovoSendData.getPostConnect());
                        CLog.i(TAG, "send order to device after ble connect " + mISyncDataCallbacks.size());
//						for(ISyncDataCallback mCallback: mISyncDataCallback){
//							mCallback.onConnectSuccessed();
//						}
                    } else {
                        CLog.e(TAG, "not start");

                    }

                } else if (msg.what == CONNECT_AGAIN) {

                    CLog.i(TAG, "connect  ble device  again");

                    mBleManager.connect(mDevice, true);
                } else if (msg.what == TIME_OUT_CALL_BACK) {
                    if (isStart) {

                        for (ISyncDataCallback mCallback : mISyncDataCallbacks) {
                            mCallback.onTimeOut();
                        }
                    }
                } else if (msg.what == GET_SLEEP_DATA) {
                    if (isStart) {
                        SendDataToDevice(LenovoSendData.getSleepDataCommandCommand());
                    }
                } else if (msg.what == GET_DAILY_DATA) {
                    if (isStart) {
                        SendDataToDevice(LenovoSendData.getDailySportData());
                    }
                }
            }

        };
        mContext = context;
        mTimeoutCheck = new TimeoutCheck(this);
        mTimeoutCheck.setTryConnectCounts(5);
        mTimeoutCheck.setTimeout(TIME_OUT);

        //mBleManager =  LenovoBleManager.getInstance(mContext);
        mBleManager = new LenovoBleManager(mContext);
        //mISyncDataCallback.add(callback);

    }

    public void registerBLE() {
        if (null == connectBack) {
            connectBack = new IConnectCallback() {

                @Override
                public void getValues(byte[] bytes) {
                    if (isStart) {
                        int length = bytes.length;
                        ArrayList<Integer> values = new ArrayList<Integer>();
                        for (int i = 0; i < length; i++) {

                            values.add(bytes[i] & 0xff);
                        }

                        analysis(values);
                    } else {
                        CLog.d(TAG, " isStart:" + isStart);
                    }
                }

                @Override
                public void getValue(int value) {
                    for (ISyncDataCallback callback : mISyncDataCallbacks) {
                        callback.onBattery(value);
                    }
                    //mBleManager.endReadBatteryValue();
                }

                @Override
                public void onNotifySuccess() {
                    // TODO Auto-generated method stub
                    //mCallback.onConnectSuccessed();
                    if (isStart) {

                        mHandler.removeMessages(ORDER_CONNECT);
                        mHandler.sendEmptyMessageDelayed(ORDER_CONNECT, 1000);
                    }
                }

                @Override
                public void connectState(BluetoothDevice device, int status, int newState) {
                    // TODO Auto-generated method stub

                    if (newState == BluetoothAdapter.STATE_CONNECTED) {
                        mDevice = device;
                        mHandler.removeMessages(CONNECT_AGAIN);
                    } else if (newState == BluetoothAdapter.STATE_DISCONNECTED) {

                        if (isStart && status != BluetoothGatt.GATT_SUCCESS) {
                            CLog.i(TAG, "disconnected been down so connect again");
                            reConnectBle();
                        }
                    }

                }
            };
        }
        mBleManager.register(connectBack);
    }

    public void unRegisterBLE() {
    }

    public boolean isConnect() {
        return mBleManager.isConnect;
    }


    /**
     * @param count default count is 3 times
     */
    public void setTryConnectCounts(int count) {
        mTimeoutCheck.setTryConnectCounts(count);
    }



    public void start(int[] order) {
        isStart = true;
        registerBLE();
        SendDataToDevice(order);
        mTimeoutCheck.setIsConnection(true);
    }

    /**
     *
     */
    public void start(BluetoothDevice device) {
        isStart = true;
        registerBLE();

        mBleManager.connect(device, true);

        mTimeoutCheck.startCheckTimeout();
        mTimeoutCheck.setIsConnection(true);
        mTimeoutCheck.setTryConnectCounts(2);
        mTimeoutCheck.setTimeout(TIME_OUT);
    }

    public BluetoothDevice getDevice() {
        return mDevice;
    }

    private void reConnectBle() {

        mTimeoutCheck.setTryConnectCounts(1);
        mTimeoutCheck.setTimeout(TIME_OUT);
        mTimeoutCheck.startCheckTimeout();

        mHandler.removeMessages(ORDER_CONNECT);
        mHandler.removeMessages(CONNECT_AGAIN);

        if (null != mBleManager) {
            mBleManager.close();
            mHandler.removeMessages(CONNECT_AGAIN);
            mHandler.sendEmptyMessageDelayed(CONNECT_AGAIN, 1200);
        }

    }

    public void cancel() {
        isStart = false;
        mLastSendData = null;
        mHandler.removeMessages(ORDER_CONNECT);
        mHandler.removeMessages(CONNECT_AGAIN);
        mHandler.removeMessages(TIME_OUT_CALL_BACK);

        if (null != mTimeoutCheck) {
            mTimeoutCheck.stopCheckTimeout();
        }
        mBleManager.unRegister(connectBack);
    }

    /**
     *
     */
    public void stop() {

        cancel();
//		mISyncDataCallback.clear();
        if (mBleManager != null) {
            mBleManager.close();
            mDevice = null;
        }
    }


    @Override
    public void onReceivedFailed() {
        CLog.d(TAG, "receivedFailed()");
        mHandler.sendEmptyMessage(TIME_OUT_CALL_BACK);
    }

    @Override
    public void onReSend() {
        CLog.d(TAG, "reSend()");
        if (isStart) {

            reSendDataToDevice(mLastSendData);
        }

    }


    @Override
    public void onReConnect(int tryConnectIndex) {
       CLog.d(TAG, "reConnect() tryConnectIndex:" + tryConnectIndex);
        mHandler.sendEmptyMessage(TIME_OUT_CALL_BACK);
        //reConnectBle();
    }

    @Override
    public void onConnectFailed(int tryConnectIndex) {
        CLog.d(TAG, "ConnectFailed() tryConnectIndex:" + tryConnectIndex);
//			for(ISyncDataCallback mCallback: mISyncDataCallback){
//				mCallback.onTimeOut();
//			}
        mHandler.sendEmptyMessage(TIME_OUT_CALL_BACK);
    }

    /**
     * send data from application to device
     *
     * @param datas
     */
    public void SendDataToDevice(final int[] datas) {
//		if(DataUtil.equalArray(mLastSendData, datas)){
//			return;
//		}
        if (mBleManager != null) {
            mTimeoutCheck.setIsConnection(false);
            mTimeoutCheck.setTimeout(TIME_OUT);
            mTimeoutCheck.setTryConnectCounts(2);
            mTimeoutCheck.startCheckTimeout();
            mLastSendData = datas;
            mBleManager.writeIasAlertLevel(mWriteClicentUUID, mWriteCharacteristicUUID, intToByte(datas));
        }
    }


    /**
     * @param datas
     */
    public void reSendDataToDevice(final int[] datas) {
        if (mBleManager != null) {
//			mLastSendData = datas;
            mBleManager.writeIasAlertLevel(mWriteClicentUUID, mWriteCharacteristicUUID, intToByte(datas));
        }
    }

    /**
     * @param datas
     */
    public void analysis(ArrayList<Integer> datas) {


        if (datas == null) {
            for (ISyncDataCallback mCallback : mISyncDataCallbacks) {
                mCallback.onNullData();
            }
        } else {
            final boolean isEndOfData = analysisReplay.isEndOfData(datas);
            int msgID = analysisReplay.decideWhichCommandReplay(datas);
            if (isEndOfData) {
                if (lastMsgID == LenovoCommandStatus.CONNECT_SUCCESS) {
                    lastMsgID = LenovoCommandStatus.GET_SPORT_DATA;
                } else if (isSendGetDailyCommand) {
                    lastMsgID = LenovoCommandStatus.GET_DAILY_DATA;
                    isSendGetDailyCommand = false;
                } else if (isSendGetSleepCommand) {
                    lastMsgID = LenovoCommandStatus.GET_SLEEP_DATA;
                    isSendGetSleepCommand = false;
                }
                msgID = lastMsgID;
            } else {
                lastMsgID = msgID;
            }


            switch (msgID) {
                case LenovoCommandStatus.CONNECT_SUCCESS:
                    mTimeoutCheck.stopCheckTimeout();
                    mTimeoutCheck.setTryConnectCounts(3);
                    mTimeoutCheck.setIsConnection(false);
                    mTimeoutCheck.setTimeout(3000);

                    CLog.d(TAG, "mISyncDataCallback size" + mISyncDataCallbacks.size());
                    for (ISyncDataCallback callback : mISyncDataCallbacks) {

                        mBleManager.readBatteryValue();
                        deviceId = analysisReplay.getModelNo(datas);
                        callback.onGetDeviceID(deviceId);
                        callback.onGetVersion(analysisReplay.getVersion(datas));
                        callback.onBindSucess();
                        callback.onConnectSuccessed();
                    }
                    break;


                case LenovoCommandStatus.SET_SETTING_VALUE_USERINFO:
                    mTimeoutCheck.stopCheckTimeout();
                    for (ISyncDataCallback callback : mISyncDataCallbacks) {
                        callback.onUpdateUserinfoSuccessed();
                    }
                    break;
                case LenovoCommandStatus.SET_HEART_RATE_MAX_MIN_VALUE:
                    mTimeoutCheck.stopCheckTimeout();
                    for (ISyncDataCallback callback : mISyncDataCallbacks) {
                        callback.onUpdateHeartWarningSuccess();
                    }
                    break;

                case LenovoCommandStatus.CLEAR_DATA:
                    mTimeoutCheck.stopCheckTimeout();
                    for (ISyncDataCallback mCallback : mISyncDataCallbacks) {
                        mCallback.onClearDataSuccessed();
                    }

                    break;
                case LenovoCommandStatus.GET_SPORT_DATA:
                    mTimeoutCheck.stopCheckTimeout();
                    if (!analysisReplay.isHasNoSportData(datas) && !analysisReplay.isEndOfData(datas)) {
                        mSportDatas.add(datas);
                        //mRecordDatas.add(datas);
                    }

                    if (analysisReplay.isEndOfData(datas)) {
                        //SendDataToDevice(LenovoSendData.getSleepDataCommandCommand());
                        mHandler.sendEmptyMessageDelayed(GET_DAILY_DATA, 500);
                        isSendGetDailyCommand = true;

                    }
                    break;
                case LenovoCommandStatus.GET_DAILY_DATA:
                    if (!analysisReplay.hasNoDailyData(datas) && !analysisReplay.isEndOfData(datas)) {
                        mRecordDatas.add(datas);
                    }
                    if (analysisReplay.isEndOfData(datas)) {
                        //SendDataToDevice(LenovoSendData.getSleepDataCommandCommand());
                        mHandler.sendEmptyMessageDelayed(GET_SLEEP_DATA, 500);
                        isSendGetSleepCommand = true;

                    }

                    break;
                case LenovoCommandStatus.GET_SLEEP_DATA:
                    mTimeoutCheck.stopCheckTimeout();
                    // upload data from device
                    CLog.d("enLong", "add sleep data");
                    if (!analysisReplay.isHasNoSleepData(datas) && !analysisReplay.isEndOfData(datas)) {

                        mRecordDatas.add(datas);
                    }
                    if (analysisReplay.isEndOfData(datas)) {
                        CLog.d("enLong", "end of sleep data");
                        ArrayList<ArrayList<Integer>> comformData = analysisReplay.getSportSleepData(mRecordDatas);
                        if (null != comformData) {
                            StringBuilder sb = new StringBuilder();

                            ArrayList<Integer> allData = new ArrayList<Integer>();
                            for (int i = 0; i < comformData.size(); i++) {
                                ArrayList<Integer> item = comformData.get(i);
                                for (int j = 0; j < item.size(); j++) {
                                    sb.append(item.get(j) + " ");
                                    allData.add(item.get(j));
                                }
                            }

                            CLog.d("enLong", "data str =" + sb.toString());

                            for (int i = 0; i < allData.size(); i++) {

                                mBaos.write(encryptMyxor(allData.get(i), mBaos.size() % 6));
                            }


                            hearRateData = analysisReplay.getHeartData(mSportDatas, LenovoCommandStatus.productId);
                            HeartRateHelper.saveHeartRateDataToLocal(mContext, hearRateData, AccessoryConfig.userID);
                        }
                        AccessoryDataParseUtil decode = new AccessoryDataParseUtil(mContext);
                        HashMap<String, AccessoryValues> data = decode.analysisDatas(comformData);
                        CLog.d("enLong", "mISyncDataCallback size=" + mISyncDataCallbacks.size());
//					mISyncDataCallback.get(0).onSyncDataOver(data, mBaos);
                        for (ISyncDataCallback mCallback : mISyncDataCallbacks) {
                            mCallback.onSyncDataOver(data, mBaos);
                        }


                    }


                    break;


                case LenovoCommandStatus.SET_SETTING_VALUE_ALARM_GOAL_CAL_STEP:
                    mTimeoutCheck.stopCheckTimeout();
                    for (ISyncDataCallback mCallback : mISyncDataCallbacks) {
                        mCallback.onSetTargetStepOver();
                    }
                    break;
            /*case LenovoCommandStatus.SET_HEART_RATE_MAX_MIN_VALUE:
				for(ISyncDataCallback mCallback: mISyncDataCallback){
					mCallback.onUpdateHeartRateMaxMinValueSuccess();
				}
				
				break;*/
                default:
                    CLog.d(TAG, "on get other datas");
                    for (ISyncDataCallback mCallback : mISyncDataCallbacks) {
                        mCallback.onGetOtherDatas(datas);
                    }
                    break;
            }
        }
    }


    /**
     *
     * @param datas

    private void getUserInfo(ArrayList<Integer> datas) {
    int height = datas.get(3);
    int weigh = datas.get(4);
    int age = datas.get(5);
    int gender = datas.get(6);
    int stepLength = datas.get(7);
    int runLength = datas.get(8);
    int sportType = datas.get(11);
    int goalValue = datas.get(12) << 8;
    goalValue += datas.get(13);

    for(ISyncDataCallback mCallback: mISyncDataCallback){
    mCallback.onGetUserInfo(height, weigh, age, gender, stepLength,
    runLength, sportType, goalValue);
    }
    }*/

    /**
     * @param datas
     * @return
     */
    private String countTime(ArrayList<Integer> datas) {
        int year = 2000 + Integer.parseInt(Integer.toHexString(datas.get(3)));
        int month = Integer.parseInt(Integer.toHexString(datas.get(4)));
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
    private String getDeviceID(ArrayList<Integer> list) {
        StringBuilder append = new StringBuilder();
        append.append(list.get(3));
        append.append("-");

        append.append((list.get(4) << 8) + list.get(5));
        append.append("-");

        append.append((list.get(6) << 8) + list.get(7));
        append.append("-");

        append.append((list.get(8) << 8) + list.get(9));
        append.append("-");

        append.append(list.get(10));
        append.append("-");

        append.append((list.get(11) << 8) + list.get(12));
        append.append("-");

        append.append((list.get(13) << 8) + list.get(14));
        append.append("-");

        append.append(list.get(15));

        return append.toString();
    }

    private int isHasDataLargeThan0(int[] data) {
        int count = 0;
        for (int i = 0; i < data.length; i++) {
            if (data[i] > 0) {
                count++;

            }
        }
        return count;
    }
}
