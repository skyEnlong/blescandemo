package com.example.enlong.blescandemo;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;

import com.communication.bean.ShoseDataDetail;
import com.communication.bean.ShoseDataSummary;
import com.communication.ble.ShoseBleSyncManager;
import com.communication.common.BaseCommandHelper;
import com.communication.data.AccessoryValues;
import com.communication.data.IShoesSyncCallBack;
import com.communication.shoes.ShoseCommand;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by enlong on 16/10/18.
 */
public class ShoesConnector implements IShoesSyncCallBack {
    ShoseBleSyncManager mBLESyncManager;
    Context mContext;


    public ShoesConnector(Context mContext){
        this.mContext = mContext;
        init();
    }

    public void init() {
        // TODO Auto-generated method stub

        mBLESyncManager = new ShoseBleSyncManager(mContext, this);
    }


    @Override
    public void onGetShoseSummary(ShoseDataSummary summary) {

    }

    @Override
    public void onGetShoseDetail(List<ShoseDataDetail> details) {

    }

    @Override
    public void onGetShoseClear() {

    }

    @Override
    public void onGetShoseTotal(int dis) {

    }

    @Override
    public void onNullData() {

    }

    @Override
    public void onBindSucess() {
        MsgEvent event = new MsgEvent();
        event.event_id = 2;
        event.msg = "发送指令并且接收成功";
        EventBus.getDefault().post(event);


    }

    @Override
    public void onGetVersion(String version) {

    }

    @Override
    public void onGetDeviceID(String deviceID) {

    }

    @Override
    public void onUpdateTimeSuccessed() {

    }

    @Override
    public void onUpdateAlarmReminderSuccessed() {

    }

    @Override
    public void onBattery(int battery) {

    }

    @Override
    public void onClearDataSuccessed() {

    }

    @Override
    public void onGetDeviceTime(String time) {

    }

    @Override
    public void onSyncDataProgress(int progress) {

    }

    @Override
    public void onUpdateUserinfoSuccessed() {

    }

    @Override
    public void onGetUserInfo(int height, int weigh, int age, int gender, int stepLength, int runLength, int sportType, int goalValue) {

    }

    @Override
    public void onUpdateHeartWarningSuccess() {

    }

    @Override
    public void onSetTargetStepOver() {

    }

    @Override
    public void onSyncDataOver(HashMap<String, AccessoryValues> data, ByteArrayOutputStream baos) {

    }

    @Override
    public void onGetOtherDatas(List<Integer> datas) {

    }

    @Override
    public void onFriedWarningSuccess() {

    }

    @Override
    public void onSetFrindSwitchOver() {

    }

    @Override
    public void onDeviceDisconnect() {

    }

    @Override
    public void onConnectSuccessed() {
        MsgEvent event = new MsgEvent();
        event.event_id = 2;
        event.msg = "连接成功,发送指令";
        EventBus.getDefault().post(event);
        mBLESyncManager.writeDataToDevice(new BaseCommandHelper().getCommand(
                ShoseCommand.CODE_CONNECT));
    }

    @Override
    public void onTimeOut() {
        disconnect();
        MsgEvent event = new MsgEvent();
        event.event_id = -1;
        event.msg = "连接超时";
        EventBus.getDefault().post(event);

    }

    public void startDevice(String address) {
        mBLESyncManager.startDevice(BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address));
    }

    public void disconnect() {
        mBLESyncManager.close();
    }
}
