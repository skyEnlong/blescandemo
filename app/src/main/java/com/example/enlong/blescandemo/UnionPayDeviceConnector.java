package com.example.enlong.blescandemo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.communication.bean.CodoonHealthDevice;
import com.communication.data.AccessoryValues;
import com.communication.data.DataUtil;
import com.communication.data.SendData;
import com.communication.gpsband.GpsBandParseUtil;
import com.communication.unionpay.ICodoonUnionDataInterfacce;
import com.communication.unionpay.UnionPayCommand;
import com.unionpay.blepayservice.CodPayConnector;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

import de.greenrobot.event.EventBus;

import static android.R.attr.action;
import static com.example.enlong.blescandemo.AccessoryConst.ACTION_BIND;
import static com.example.enlong.blescandemo.AccessoryConst.ACTION_FRIENDS_WARNING;
import static com.example.enlong.blescandemo.AccessoryConst.ACTION_GET_BATTERY;
import static com.example.enlong.blescandemo.AccessoryConst.ACTION_GET_DEVICE_INFO;
import static com.example.enlong.blescandemo.AccessoryConst.ACTION_SYNC_DATA;

/**
 * Created by enlong on 2017/2/6.
 */

public class UnionPayDeviceConnector implements ICodoonUnionDataInterfacce, UnionPayCommand {
    private CodPayConnector mCodPayConnector;
    private boolean isBusy;
    private BluetoothDevice mBleDevice = null;
    private final String TAG = "ble";
    private Context mContext ;
    private Handler connectHandler;

    private CodoonHealthDevice device;

    public UnionPayDeviceConnector(Context mContext) {

        this.mContext = mContext;
        mCodPayConnector = CodPayConnector.getInstance(mContext);

        mCodPayConnector.bindService();
        connectHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                return true;
            }
        });
    }

    public void getVersion(){
        if( mCodPayConnector.isConnect()){
            MsgEvent event = new MsgEvent();
            event.msg = "获取BTC——INFO";
            event.event_id = 1;
            EventBus.getDefault().post(event);

            mCodPayConnector.writeCmdAndDataToDevice(BTC_INFO, null);
        }else {
            MsgEvent event = new MsgEvent();
            event.msg = "连接以段开， 重新连接中";
            event.event_id = 1;
            EventBus.getDefault().post(event);
            startBindDevice(this.device);
        }
    }
    public void writeCommand(byte[] data){
       if( mCodPayConnector.isConnect()){
           MsgEvent event = new MsgEvent();
           event.msg = DataUtil.DebugPrint(data);
           event.event_id = 1;
           EventBus.getDefault().post(event);

           mCodPayConnector.writeCmdAndDataToDevice(BTC_DATA, data);
       }else {
           MsgEvent event = new MsgEvent();
           event.msg = "连接以段开， 重新连接中";
           event.event_id = 1;
           EventBus.getDefault().post(event);
           startBindDevice(this.device);
       }
    }


    @Override
    public void onResponse(String data) {
        MsgEvent event = new MsgEvent();
        event.msg = data;
        EventBus.getDefault().post(event);
    }

    //////////////codoon data protocol////////////
    @Override
    public void onDeviceUnBind(String s) {
        Log.i(TAG, "onBindService");
    }

    @Override
    public synchronized void onDeviceBind(String s) {
        Log.i(TAG, "onDeviceBind");
        MsgEvent event = new MsgEvent();
        event.msg = "绑定成功：";
        EventBus.getDefault().post(event);

    }

    @Override
    public void onConnectSuccessed() {
        Log.i(TAG, "state onConnectSuccessed");
        MsgEvent event = new MsgEvent();
        event.msg = "链接成功：";
        EventBus.getDefault().post(event);
    }

    @Override
    public void onGetVersionAndId(String version_id, String id) {
        MsgEvent event = new MsgEvent();
        String guabo = GpsBandParseUtil.getDeviceId(device.manufacturer);
        event.msg = "获取ID：" + id + " \n版本:" + version_id;
        EventBus.getDefault().post(event);
    }
    @Override
    public void onUpdateTimeSuccessed() {

        switch (action) {
            case ACTION_BIND:
            case ACTION_GET_DEVICE_INFO:
                mCodPayConnector.writeCmdAndDataToDevice(BTC_INFO, null);
                break;
            case ACTION_SYNC_DATA:

                mCodPayConnector.writeDataToDevice(SendData
                        .getPostSyncDataByFrame());
                break;
            case ACTION_GET_BATTERY:
                mCodPayConnector.writeDataToDevice(SendData
                        .getPostGetUserInfo2());
                break;
            case ACTION_FRIENDS_WARNING:
                mCodPayConnector.writeDataToDevice(SendData
                        .postBlueFriendRequst());
                break;
            default:
                MsgEvent event = new MsgEvent();
                event.msg = "更新时间成功";
                EventBus.getDefault().post(event);
                break;
        }
    }

    @Override
    public void onUpdateAlarmReminderSuccessed() {
        MsgEvent event = new MsgEvent();
        event.msg = "设置闹铃成功";
        EventBus.getDefault().post(event);
    }

    @Override
    public void onBattery(int battery) {
        MsgEvent event = new MsgEvent();
        event.msg = "获取电量：" + battery;
        EventBus.getDefault().post(event);
    }

    @Override
    public void onClearDataSuccessed() {
        MsgEvent event = new MsgEvent();
        event.msg = "擦除数据成功";
        EventBus.getDefault().post(event);
    }

    @Override
    public void onGetDeviceTime(String s) {

    }

    int lastProgress = 0;
    @Override
    public void onSyncDataProgress(int progress) {
        CLog.d(TAG, "onSyncDataProgress(): " + progress);
//        CodoonNotificationManager.getInstance(mContext).sendNotificationAccessorySyncByState(MSG_SYNC_START);
        if(lastProgress != progress){
            lastProgress = progress;
            MsgEvent event = new MsgEvent();
            event.msg = "同步数据：" + progress;
            EventBus.getDefault().post(event);
        }

    }

    @Override
    public void onUpdateUserinfoSuccessed() {
        MsgEvent event = new MsgEvent();
        event.msg = "更新用户信息成功" ;
        EventBus.getDefault().post(event);
    }

    @Override
    public void onGetUserInfo(int height, int weigh, int age,
                              int gender, int stepLength, int runLength,
                              int sportType, int goalValue) {
        // TODO Auto-generated method stub
        CLog.d(TAG, "onGetUserInfo:height " + height + " weigh: "
                + weigh + " age: " + age + " gender" + gender
                + " stepLength" + stepLength + " runLength"
                + runLength + " sportType" + sportType
                + " goalValue" + goalValue);

    }



    @Override
    public void onSyncDataOver(HashMap<String, AccessoryValues> data_map,
                               ByteArrayOutputStream mBaos) {
        MsgEvent event = new MsgEvent();
        event.msg = "同步数据成功" ;
        EventBus.getDefault().post(event);

    }


    @Override
    public void onTimeOut() {
        CLog.i(TAG, "BLE conenct outTime");

    }


    public void startBindDevice(CodoonHealthDevice device) {
        this.device = device;
        mCodPayConnector.registerSyncDataCallback(this);


//        mCodPayConnector.startDevice(
//                BluetoothAdapter.getDefaultAdapter().getRemoteDevice(device.address));
        if(mCodPayConnector.isConnect()){

            mCodPayConnector.writeDataToDevice(SendData.getPostSyncTime(System.currentTimeMillis()));
        }else{
            if (null == mBleDevice) {
                mBleDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(device.address);

            }
            mCodPayConnector.startDevice(mBleDevice);
        }
    }


    public void startSyncData(CodoonHealthDevice device) {
        CLog.i("accessory", "startSyncData device:" + device.address);
        mCodPayConnector.registerSyncDataCallback(this);
        if(mCodPayConnector.isConnect()){

            mCodPayConnector.writeDataToDevice(SendData.getPostSyncTime(System.currentTimeMillis()));
        }else{
            if (null == mBleDevice) {
                mBleDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(device.address);

            }
            mCodPayConnector.startDevice(mBleDevice);
        }


    }


    public void stop() {
        Log.i(TAG, "sync stop");
        isBusy = false;
        mCodPayConnector.unRegisterSyncDataCallback(this);

        mCodPayConnector.stop();
        mCodPayConnector.close();
//        mCodPayConnector = null;
    }


    @Override
    public void onDeviceDisconnect() {
        MsgEvent event = new MsgEvent();
        event.msg = "连接已段开";
        EventBus.getDefault().post(event);
    }


    public boolean isBusy(){
        return isBusy;
    }

    public boolean isConnect() {
        return (null == mCodPayConnector) ? false : mCodPayConnector.isConnect();
    }
}
