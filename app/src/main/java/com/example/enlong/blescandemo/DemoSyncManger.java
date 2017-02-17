package com.example.enlong.blescandemo;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;

import com.communication.bean.CodoonHealthDevice;
import com.communication.bean.CodoonShoesMinuteModel;
import com.communication.bean.CodoonShoesModel;
import com.communication.bean.CodoonShoesState;
import com.communication.ble.CodoonShoesSyncManager;
import com.communication.ble.ICodoonShoesCallBack;
import com.communication.data.AccessoryValues;
import com.communication.data.DataUtil;
import com.communication.data.DeviceUpgradeCallback;
import com.communication.gpsband.GpsBandParseUtil;
import com.example.enlong.blescandemo.logic.TextToSpeecher;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.greenrobot.event.EventBus;

/**
 * Created by enlong on 2016/12/8.
 */

public class DemoSyncManger implements ICodoonShoesCallBack, DeviceUpgradeCallback {

    private CodoonShoesSyncManager syncManager;
    private CodoonHealthDevice device;
    private Context mContext;
    public DemoSyncManger(Context mContext) {
        this.mContext = mContext;
        syncManager = new CodoonShoesSyncManager(mContext, this);
    }

    @Override
    public void onResponse(String str) {

        MsgEvent event = new MsgEvent();
        event.msg = str;
        EventBus.getDefault().post(event);
    }

    @Override
    public void onShoesDataSyncRedy() {
        MsgEvent event = new MsgEvent();
        event.msg = "同步数据准备成功";
        EventBus.getDefault().post(event);
    }

    @Override
    public void onAccessoryBDSuccess(int i) {
        MsgEvent event = new MsgEvent();
        event.msg = "标定成功";
        EventBus.getDefault().post(event);
    }

    @Override
    public void onStartRunResult(int i) {
        MsgEvent event = new MsgEvent();
        event.msg = "开始跑步：res =" + i;
        EventBus.getDefault().post(event);
    }

    @Override
    public void onStopRun() {
        MsgEvent event = new MsgEvent();
        event.msg = "结束跑步。";
        EventBus.getDefault().post(event);
    }

    @Override
    public void onGetShoesState(CodoonShoesState codoonShoesState) {
        MsgEvent event = new MsgEvent();
        event.msg = "跑鞋状态：" + codoonShoesState.toString();
        EventBus.getDefault().post(event);
    }

    @Override
    public void onGetTotalRun(int totalRun) {
        MsgEvent event = new MsgEvent();
        event.msg = "跑鞋总距离：" + totalRun;
        EventBus.getDefault().post(event);
    }

    @Override
    public void onGetRunSports(List<CodoonShoesModel> ls) {
        StringBuffer str = new StringBuffer();
        if(null != ls){
            for(CodoonShoesModel model : ls){
                str.append(model.toString());
                str.append("\n");
            }

            MsgEvent event = new MsgEvent();
            event.msg = "解析数据：" + str.toString();
            EventBus.getDefault().post(event);
        }
    }

    @Override
    public void onGetRunState(CodoonShoesMinuteModel model) {
        MsgEvent event = new MsgEvent();
        event.msg = "跑步姿态数据：" + model.toString();
        EventBus.getDefault().post(event);
    }

    @Override
    public void onGetOriginData(String origin) {
        MsgEvent event = new MsgEvent();
        event.msg = "三轴数据：" + origin;
        EventBus.getDefault().post(event);
    }

    @Override
    public void onNullData() {

    }

    @Override
    public void onBindSucess() {
        MsgEvent event = new MsgEvent();
        event.msg = "绑定成功：";
        EventBus.getDefault().post(event);
    }

    @Override
    public void onGetVersion(String version) {
        MsgEvent event = new MsgEvent();
        event.msg = "获取版本：" + version;
        EventBus.getDefault().post(event);
    }

    @Override
    public void onGetDeviceID(String deviceID) {
        MsgEvent event = new MsgEvent();
        String guabo = GpsBandParseUtil.getDeviceId(device.manufacturer);
        event.msg = "获取ID：" + deviceID + " 广播ID：" + guabo
                + " equal ? " + deviceID.equals(guabo);
        EventBus.getDefault().post(event);
    }

    @Override
    public void onUpdateTimeSuccessed() {
        MsgEvent event = new MsgEvent();
        event.msg = "更新时间成功";
        EventBus.getDefault().post(event);
    }

    @Override
    public void onUpdateAlarmReminderSuccessed() {

    }

    @Override
    public void onBattery(int battery) {

    }

    @Override
    public void onClearDataSuccessed() {
        MsgEvent event = new MsgEvent();
        event.msg = "清除数据成功";
        EventBus.getDefault().post(event);
    }

    @Override
    public void onGetDeviceTime(String time) {

    }

    @Override
    public void onSyncDataProgress(int progress) {

    }

    @Override
    public void onUpdateUserinfoSuccessed() {
        MsgEvent event = new MsgEvent();
        event.msg = "更新用户信息成功";
        EventBus.getDefault().post(event);
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
        if(null != data){
            Set<Map.Entry<String, AccessoryValues>> kv = data.entrySet();
            for (Map.Entry<String, AccessoryValues>  v : kv){
                MsgEvent event = new MsgEvent();
                event.msg =  v.getValue().toString();
                EventBus.getDefault().post(event);

            }

        }
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
        TextToSpeecher.getInstance(mContext).speechBluetoothLose();
        MsgEvent event = new MsgEvent();
        event.msg = "连接已断开";
        EventBus.getDefault().post(event);
    }

    @Override
    public void onConnectSuccessed() {
        MsgEvent event = new MsgEvent();
        event.msg = "连接成功";
        EventBus.getDefault().post(event);
    }

    @Override
    public void onChangeToBootMode() {
        MsgEvent event = new MsgEvent();
        event.msg = "onChangeToBootMode";
        EventBus.getDefault().post(event);

    }

    @Override
    public void onGetBootVersion(String version) {
        MsgEvent event = new MsgEvent();
        event.msg = "onGetBootVersion" + version;
        EventBus.getDefault().post(event);
    }

    @Override
    public void onConnectBootSuccess() {
        MsgEvent event = new MsgEvent();
        event.msg = "onConnectBootSuccess" ;
        EventBus.getDefault().post(event);
    }

    private int lastProgress = 0;
    @Override
    public void onWriteFrame(int frame, int total) {
        int pro = frame * 100 / total;
        if(lastProgress != pro){
            lastProgress = pro;
            MsgEvent event = new MsgEvent();
            event.msg = "onWriteFrame progress percents " + lastProgress ;
            EventBus.getDefault().post(event);

        }

    }

    @Override
    public void onCheckBootResult(boolean isSuccess, int retryCount) {
        MsgEvent event = new MsgEvent();
        event.msg = "onCheckBootResult  " + isSuccess  + " retryCount:"  + retryCount ;
        EventBus.getDefault().post(event);
    }

    @Override
    public void onTimeOut() {

    }

    public void start(com.communication.bean.CodoonHealthDevice device) {
        this.device = device;
        syncManager.startDevice(BluetoothAdapter.getDefaultAdapter().getRemoteDevice(device.address));
    }

    public void writeCommand(byte[] data){

        if(syncManager.isConnect()){
            MsgEvent event = new MsgEvent();
            event.msg = DataUtil.DebugPrint(data);
            event.event_id = 1;
            EventBus.getDefault().post(event);

            syncManager.writeDataToDevice(data);
        }else {
            MsgEvent event = new MsgEvent();
            event.msg = "连接已段开， 重新连接中";
            event.event_id = 1;
            EventBus.getDefault().post(event);

            start(this.device);
        }

    }

    public void startUpgrade(String fs) {

        if(syncManager.isConnect()){
            MsgEvent event = new MsgEvent();
            event.msg = "开始升级";
            event.event_id = 1;
            EventBus.getDefault().post(event);
            syncManager.startUpgrade(BluetoothAdapter.getDefaultAdapter().getRemoteDevice(device.address),
                    fs,
                    this);
        }else {
            MsgEvent event = new MsgEvent();
            event.msg = "连接以段开， 重新连接中";
            event.event_id = 1;
            EventBus.getDefault().post(event);

            start(this.device);
        }
    }

    public void disConnect() {
        syncManager.close();
    }
}
