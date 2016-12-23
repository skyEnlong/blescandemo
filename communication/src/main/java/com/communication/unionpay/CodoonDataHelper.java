package com.communication.unionpay;

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;

import com.communication.data.AccessoryDataParseUtil;
import com.communication.data.AccessoryValues;
import com.communication.data.CLog;
import com.communication.data.SendData;
import com.communication.data.TimeoutCheck;
import com.communication.data.TransferStatus;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by workEnlong on 2015/6/12.
 */
public class CodoonDataHelper {
    private final String TAG = "union_pay_sport";
    protected List<ICodoonUnionDataInterfacce> mISyncDataCallbacks;
    protected TimeoutCheck mTimeoutCheck;
    protected ArrayList<ArrayList<Integer>> mRecordDatas;
    protected int frameCount = 0;
    private final byte[] xorKey = new byte[] { 0x54, (byte) 0x91, 0x28, 0x15,
            0x57, 0x26 };
    protected int indexFrame = 0;
    protected ByteArrayOutputStream mBaos;
    private ICodoonProtocol mCommunication;
    private Context mContext;
    private String device_mac;
    public CodoonDataHelper(Context mContext,
                            ICodoonProtocol mCommunication,
                            List<ICodoonUnionDataInterfacce> mISyncDataCallbacks,
                            TimeoutCheck mTimeoutCheck){
        this.mISyncDataCallbacks = mISyncDataCallbacks;
        this.mTimeoutCheck = mTimeoutCheck;
        this.mCommunication = mCommunication;
        this.mContext = mContext;
    }

    public void dealByteData(byte[] data){
        ArrayList<Integer> list =  new ArrayList<Integer>();
        for (int i = 0; i < data.length; i++) {

            list.add(data[i] & 0xff);
        }
//        DataUtil.DebugPrint(TAG, data);
        try {

            analysis(list);
        }catch (RemoteException e){
            e.printStackTrace();
        }
    }

    /**
     * 将睡眠标志位0xFB 转换为0xFD
     * @param datas
     */
    private void modifySleepFlag(int start_position, List<Integer> datas){
        int flag_count = 0;
        if(null == datas || datas.size() < start_position + 6) return;
        for (int i = 0; i < 6; i++){
            if((datas.get(start_position + i) & 0xff) != 0xfb){
                break;
            }
            flag_count++;
         }

        //find 6 0xfb
        if(flag_count == 6){

           for (int i = 0; i < 6; i++){
               datas.set(start_position + i,  0xFD);
           }

            CLog.i(TAG, "find the 0xfb sleep flag, and replace it");
        }

    }
    private void analysis(List<Integer> datas) throws RemoteException{
        if (datas == null || datas.size() < 2) {
            return;
        } else {
            final int msgID = datas.get(1);
            switch (msgID) {
                case TransferStatus.RECEIVE_CONNECTION_ID:
                    mTimeoutCheck.stopCheckTimeout();
                    mTimeoutCheck.setTryConnectCounts(3);
                    mTimeoutCheck.setIsConnection(false);
                    mTimeoutCheck.setTimeout(3000);

                    for (ICodoonUnionDataInterfacce callback : mISyncDataCallbacks) {

                        callback.onConnectSuccessed();
                    }
                    break;

                case TransferStatus.RECEIVE_BINED_ID:
                    mTimeoutCheck.stopCheckTimeout();
                    for (ICodoonUnionDataInterfacce callback : mISyncDataCallbacks) {
                        callback.onDeviceBind(device_mac);
                    }
                    break;


                case TransferStatus.RECEIVE_UPDATETIME_ID:
                    mTimeoutCheck.stopCheckTimeout();
                    for (ICodoonUnionDataInterfacce callback : mISyncDataCallbacks) {
                        callback.onUpdateTimeSuccessed();
                    }
                    break;

                case TransferStatus.RECEIVE_DEVICE_TIME_ID:
                    mTimeoutCheck.stopCheckTimeout();
                    String time = countTime(datas);
                    for (ICodoonUnionDataInterfacce callback : mISyncDataCallbacks) {
                        callback.onGetDeviceTime(time);
                    }
                    break;
                case TransferStatus.RECEIVE_CLEARDATA_ID:
                    mTimeoutCheck.stopCheckTimeout();
                    for (ICodoonUnionDataInterfacce mCallback : mISyncDataCallbacks) {
                        mCallback.onClearDataSuccessed();
                    }

                    break;
                case TransferStatus.RECEIVE_FRAMECOUNT_ID:
                    mTimeoutCheck.stopCheckTimeout();
                    frameCount = (datas.get(4) << 8) + datas.get(5);     //careful the priority of << is lower than + ;
                    indexFrame = 0;
                    mBaos = new ByteArrayOutputStream();
                    Log.d(TAG, " framecount:" + frameCount);
                    // read data after get the frame count
                    if (frameCount > 0) {

                        mCommunication.writeToDevice(SendData.getPostReadSportData(indexFrame));

                        mRecordDatas = new ArrayList<ArrayList<Integer>>();
                    } else {
                        for (ICodoonUnionDataInterfacce mCallback : mISyncDataCallbacks) {
                            mCallback.onSyncDataProgress(100);
                        }

                        for (ICodoonUnionDataInterfacce mCallback : mISyncDataCallbacks) {
                            mCallback.onSyncDataOver(null, null);
                        }
                    }
                    break;
                case TransferStatus.RECEIVE_READDATA_ID:
                    mTimeoutCheck.stopCheckTimeout();
                    // upload data from device
                    final int length = datas.get(2);
                    Log.d(TAG, "receive frame:" + indexFrame);

                    /***Notice   此处并没有带每一贞的index， 而咕咚跑鞋设备带了index**/

                    ArrayList<Integer> currentDatas = new ArrayList<Integer>();

                    //the part one
                    modifySleepFlag(3, datas);

                    //the second part
                    modifySleepFlag(3 + 6, datas);

                    for (int i = 3; i < length + 3; i++) {
                        mBaos.write(encryptMyxor(datas.get(i), mBaos.size() % 6));
                        currentDatas.add(datas.get(i));
                    }
                    mRecordDatas.add(currentDatas);
                    ++indexFrame;

                    Log.d(TAG, " mISyncDataCallback size:" + mISyncDataCallbacks.size());

                    for (ICodoonUnionDataInterfacce mCallback : mISyncDataCallbacks) {
                        mCallback.onSyncDataProgress(indexFrame * 100 / frameCount);
                    }

                    if (indexFrame < frameCount) {
                        mCommunication.writeToDevice(SendData
                                .getPostReadSportData(indexFrame));

                    } else {
                        frameCount = 0;
                        indexFrame = 0;

                        for (ICodoonUnionDataInterfacce mCallback : mISyncDataCallbacks) {
                            mCallback.onSyncDataProgress(100);
                        }
                        AccessoryDataParseUtil decode = AccessoryDataParseUtil.getInstance(mContext);
                        HashMap<String, AccessoryValues> data = decode.analysisDatas(mRecordDatas);

                        for (ICodoonUnionDataInterfacce mCallback : mISyncDataCallbacks) {
                            mCallback.onSyncDataOver(data, mBaos);

                        }

                        mRecordDatas.clear();
                    }
                    break;

                case TransferStatus.RECEIVE_GETUSERINFO_ID:
                    mTimeoutCheck.stopCheckTimeout();
                    getUserInfo(datas);
                    break;

                case TransferStatus.RECEIVE_UPDATEUSERINFO_SUCCESS_ID:
                    mTimeoutCheck.stopCheckTimeout();
                    for (ICodoonUnionDataInterfacce mCallback : mISyncDataCallbacks) {
                        mCallback.onUpdateUserinfoSuccessed();
                    }
                    break;

                case TransferStatus.RECEIVE_UPDATEUSERINFO2_SUCCESS_ID:
                    mTimeoutCheck.stopCheckTimeout();
                    Log.d(TAG, "update alarm and activity remind success.");
                    for (ICodoonUnionDataInterfacce mCallback : mISyncDataCallbacks) {
                        mCallback.onUpdateAlarmReminderSuccessed();
                    }
                    String values = "";
                    for (int i : datas) {
                        values += ",0x" + Integer.toHexString(i);
                    }
                    Log.d(TAG, values);
                    break;
                case TransferStatus.RECEIVE_GETUSERINFO2_ID:
                    mTimeoutCheck.stopCheckTimeout();
                    int battery = 0;
                    if (datas.size() > 15) {
                        battery = datas.get(15);
                    } else {
                        battery = datas.get(10);
                    }

                    for (ICodoonUnionDataInterfacce mCallback : mISyncDataCallbacks) {
                        mCallback.onBattery(battery);
                    }

                    break;
                case TransferStatus.RECEIVE_FRIENDS_REQUEST_ID:
                    mCommunication.writeToDevice(SendData
                            .postBlueFriendWarning());
                    break;

//			case TransferStatus.REICEIVE_FRIENDS_SWITCH_ID:
//				mTimeoutCheck.stopCheckTimeout();
//				for(ISyncDataCallback mCallback: mISyncDataCallback){
//					mCallback.onSetFrindSwitchOver();
//				}
//				break;
                case TransferStatus.RECEIVE_GETVERSION_ID:
                    mTimeoutCheck.stopCheckTimeout();
                    String hVersion = datas.get(4) + "." + datas.get(5);
                    Log.d(TAG, "getVersion:" + hVersion);

                default:
                    Log.d(TAG, "on get other datas");

                    break;
            }
        }
    }




    /**
     * @param datas
     */
    private void getUserInfo(List<Integer> datas) throws RemoteException{
        int height = datas.get(3);
        int weigh = datas.get(4);
        int age = datas.get(5);
        int gender = datas.get(6);
        int stepLength = datas.get(7);
        int runLength = datas.get(8);
        int sportType = datas.get(11);
        int goalValue = datas.get(12) << 8;
        goalValue += datas.get(13);

        for (ICodoonUnionDataInterfacce mCallback : mISyncDataCallbacks) {
            mCallback.onGetUserInfo(height, weigh, age, gender, stepLength,
                    runLength, sportType, goalValue);
        }
    }

    /**
     * @param datas
     * @return
     */
    private String countTime( List<Integer> datas) {
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
    private String getDeviceID(List<Integer> list) {
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

    public byte encryptMyxor(int original, int n) {
        return (byte) ((original ^ xorKey[n]) & 0xFF);
    }

    public void setDeviceAdress(String device_mac) {
        this.device_mac = device_mac;
    }
}
