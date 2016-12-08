package com.communication.fsk;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;

import com.communication.ble.BaseCommunicationManager;
import com.communication.data.AccessoryDataParseUtil;
import com.communication.data.AccessoryValues;
import com.communication.data.ISyncDataCallback;
import com.communication.data.ISyncDataTask;
import com.communication.data.SendData;
import com.communication.data.TimeoutCheck;
import com.communication.data.TransferStatus;

public class FSKDeviceDataManager extends BaseCommunicationManager implements ISyncDataTask {

    private String TAG = "SyncDeviceDataManager";

    private ISyncDataCallback mCallback;


    private SendDataManager mSendDataManager;

    private ReceiveManager mReceiveManager;

    private int mTimeout = 2000;

    private int errCodeTimes = 0;

    public FSKDeviceDataManager(Context context, ISyncDataCallback callback) {
        mContext = context;
        mCallback = callback;
        mTimeoutCheck = new TimeoutCheck(this);
        mTimeoutCheck.setTryConnectCounts(8);
        mTimeoutCheck.setTimeout(mTimeout);
    }

    /**
     * @param count
     */
    @Override
    public void setTryConnectCounts(int count) {
        mTimeoutCheck.setTryConnectCounts(count);
    }

    /**
     *
     */
    @Override
    public void start() {
        mSendDataManager = new SendDataManager();
        mReceiveManager = new ReceiveManager(this);
        mReceiveManager.start();
    }

    /**
     *
     */
    @Override
    public void stop() {
        if (mTimeoutCheck != null) {
            mTimeoutCheck.stopCheckTimeout();
        }
        if (mSendDataManager != null) {
            mSendDataManager.stopAudio();
        }

        if (mReceiveManager != null) {
            mReceiveManager.stop();
        }
    }

    @Override
    public void onReceivedFailed() {
        Log.d(TAG, "receivedFailed()");
        mCallback.onTimeOut();
    }

    @Override
    public void onReSend() {
        Log.d(TAG, "reSend()");
        reSendDataToDevice(mLastSendData);
    }


    @Override
    public void onReConnect(int tryConnectIndex) {
        Log.d(TAG, "reConnect() tryConnectIndex:" + tryConnectIndex);
//			reSendDataToDevice(mLastSendData);
        //	mSendDataManager.write(SendData.getPostConnection());

        if (tryConnectIndex == 5) {
            Log.i(TAG, "reinit choose other channel");
            mSendDataManager.reInitAudio();
        }
        reSendDataToDevice(mLastSendData);
    }

    @Override
    public void onConnectFailed(int tryConnectIndex) {
        Log.d(TAG, "ConnectFailed() tryConnectIndex:" + tryConnectIndex);
        mCallback.onTimeOut();
    }

    /**
     * send data from application to device
     *
     * @param datas
     */
    @Override
    public void SendDataToDevice(final int[] datas) {
        if (mSendDataManager != null) {
            mLastSendData = datas;
            mSendDataManager.write(datas);
            mTimeoutCheck.startCheckTimeout();
        }
    }

    /**
     *
     */
    @Override
    public void connectDevice() {
        mLastSendData = SendData.getPostConnection();
        mTimeoutCheck.startCheckTimeout();
        mTimeoutCheck.setIsConnection(true);
        mTimeoutCheck.setTimeout(1000);
        reSendDataToDevice(mLastSendData);
    }

    public void reSendDataToDevice(final int[] datas) {

        if (mSendDataManager != null) {
            mLastSendData = datas;
            mTimeoutCheck.restartChectTime();
            mSendDataManager.write(datas);
        }
    }

    public void redoLastAction() {
//		if (mSendDataManager != null) {
//			mSendDataManager.write(mLastSendData);
//		}
    }

    /**
     * @param datas
     */
    public void analysis(ArrayList<Integer> datas) {

        if (datas == null) {
            mCallback.onNullData();
        } else {
            final int msgID = datas.get(1);
            Log.d(TAG, "message ID:" + msgID);
            switch (msgID) {
                case TransferStatus.RECEIVE_CONNECTION_ID:
                    errCodeTimes = 0;
                    mTimeoutCheck.stopCheckTimeout();
//                    mTimeoutCheck.setTryConnectCounts(5);
                    mTimeoutCheck.setIsConnection(false);
                    mTimeoutCheck.setTimeout(mTimeout);
                    mCallback.onConnectSuccessed();
                    break;
                case TransferStatus.RECEIVE_GETVERSION_ID:
                    errCodeTimes = 0;
                    mTimeoutCheck.stopCheckTimeout();
                    String hVersion = datas.get(4) + "." + datas.get(5);
                    mCallback.onGetVersion(hVersion);
                    break;
                case TransferStatus.RECEIVE_DEVICE_ID:
                    errCodeTimes = 0;
                    mTimeoutCheck.stopCheckTimeout();
                    String deviceid = getDeviceID(datas);
                    mCallback.onGetDeviceID(deviceid);
                    break;
                case TransferStatus.RECEIVE_UPDATETIME_ID:
                    errCodeTimes = 0;
                    mTimeoutCheck.stopCheckTimeout();
                    mCallback.onUpdateTimeSuccessed();
                    break;
                case TransferStatus.RECEIVE_DEVICE_TIME_ID:
                    errCodeTimes = 0;
                    mTimeoutCheck.stopCheckTimeout();
                    String time = countTime(datas);
                    mCallback.onGetDeviceTime(time);
                    break;
                case TransferStatus.RECEIVE_CLEARDATA_ID:
                    errCodeTimes = 0;
                    mTimeoutCheck.stopCheckTimeout();
                    mCallback.onClearDataSuccessed();
                    break;
                case TransferStatus.RECEIVE_FRAMECOUNT_ID:
                    errCodeTimes = 0;
                    mTimeoutCheck.stopCheckTimeout();
                    frameCount = (datas.get(4) * 256 + datas.get(5) + 3) / 4;
                    indexFrame = 0;
                    mBaos = new ByteArrayOutputStream();
                    Log.d(TAG, "framecount:" + frameCount);
                    // read data after get the frame count
                    SendDataToDevice(SendData
                            .getPostReadSportData((indexFrame++) << 2));

                    mRecordDatas = new ArrayList<ArrayList<Integer>>();
                    break;
                case TransferStatus.RECEIVE_READDATA_ID:
                    errCodeTimes = 0;
                    mTimeoutCheck.stopCheckTimeout();
                    // upload data from device
                    final int length = datas.size() - 1;
                    ArrayList<Integer> currentDatas = new ArrayList<Integer>();
                    for (int i = 3; i < length; i++) {
                        mBaos.write(encryptMyxor(datas.get(i), mBaos.size() % 6));
                        currentDatas.add(datas.get(i));
                    }
                    mRecordDatas.add(currentDatas);
                    if (indexFrame < frameCount) {
                        mCallback.onSyncDataProgress(indexFrame * 100 / frameCount);
                        SendDataToDevice(SendData
                                .getPostReadSportData((indexFrame++) << 2));

                    } else {
                        frameCount = 0;
                        indexFrame = 0;
                        mCallback.onSyncDataProgress(100);
                        AccessoryDataParseUtil decode = new AccessoryDataParseUtil(mContext);
                        HashMap<String, AccessoryValues> data = decode.analysisDatas(mRecordDatas);
                        mCallback.onSyncDataOver(data, mBaos);
                    }
                    break;
                case TransferStatus.RECEIVE_GETUSERINFO_ID:
                    errCodeTimes = 0;
                    mTimeoutCheck.stopCheckTimeout();
                    getUserInfo(datas);
                    break;

                case TransferStatus.RECEIVE_UPDATEUSERINFO_SUCCESS_ID:
                    errCodeTimes = 0;
                    mTimeoutCheck.stopCheckTimeout();
                    mCallback.onUpdateUserinfoSuccessed();
                    break;

                case TransferStatus.RECEIVE_UPDATEUSERINFO2_SUCCESS_ID:
                    errCodeTimes = 0;
                    mTimeoutCheck.stopCheckTimeout();
                    Log.d(TAG, "update alarm and activity remind success.");
                    mCallback.onUpdateAlarmReminderSuccessed();
                    String values = "";
                    for (int i : datas) {
                        values += ",0x" + Integer.toHexString(i);
                    }
                    Log.d(TAG, values);
                    break;
                case TransferStatus.RECEIVE_GETUSERINFO2_ID:
                    errCodeTimes = 0;
                    mTimeoutCheck.stopCheckTimeout();
                    if (datas.size() > 15) {
                        mCallback.onBattery(datas.get(15));
                    } else {
                        mCallback.onBattery(datas.get(10));
                    }

                    break;

                default:
                    Log.e(TAG, "get err response: 0x" + Integer.toHexString(msgID));
                    errCodeTimes++;
                    if (errCodeTimes > 3) {
                        errCodeTimes = 0;
                        mCallback.onTimeOut();
                    } else {

                        mTimeoutCheck.stopCheckTimeout();
                        SendDataToDevice(mLastSendData);
                    }
                    break;
            }
        }
    }


    /**
     * @param datas
     */
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

        mCallback.onGetUserInfo(height, weigh, age, gender, stepLength,
                runLength, sportType, goalValue);
    }

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

    @Override
    public void start(int[] order) {
        // TODO Auto-generated method stub

    }

    @Override
    public void start(BluetoothDevice device) {
        // TODO Auto-generated method stub

    }


    @Override
    public boolean isConnect() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void register(ISyncDataCallback callback) {
        // TODO Auto-generated method stub

    }

}
