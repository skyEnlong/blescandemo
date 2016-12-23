package com.communication.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.communication.data.CLog;
import com.communication.data.ISyncCallBack;
import com.communication.data.TimeoutCheck;
import com.communication.util.MobileUtil;

/**
 * Created by workEnlong on 2016/1/20.
 * abstract class that has deal with retry timeOut act...
 */
public abstract class BaseDeviceSyncManager implements TimeoutCheck.ITimeoutCallback, IConnectCallback,
        OnBleWriteCallBack {

    public final String TAG = "gps_ble";
    public final int SEND_DATA = 0xccc1;
    public final int BLE_CONNECT = 0xccc2;
    public int TIME_OUT = 10000;
    public byte[] lastData;

    public Context mContext;
    public BaseBleManager bleManager;
    public Handler mHandler;
    protected TimeoutCheck mTimeoutCheck;
    public final int NOTIFY_SUCEESS = 0x123456;
    public BluetoothDevice device;
    public boolean isStart;
    private ISyncCallBack mBaseCallBack;

    private int EACH_FRAME_DELAY = 5;
    protected abstract boolean handMessage(Message messagge);
    protected abstract void dealResponse(byte[] res);
    protected abstract BaseBleManager initBleManager();
    /**
     *
     * @param mContext
     * @param mCallBack can't be null
     */
    public BaseDeviceSyncManager(Context mContext, ISyncCallBack mCallBack){
        if(null == mCallBack) throw new NullPointerException("call back is null");
        this.mContext = mContext;
        this.mBaseCallBack = mCallBack;
        mTimeoutCheck = new TimeoutCheck(this);
        mTimeoutCheck.setTryConnectCounts(3);
        mTimeoutCheck.setTimeout(TIME_OUT);
        bleManager = initBleManager();

        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if(!handMessage(msg)){
                    switch (msg.what) {
                        case SEND_DATA:
                            byte[] data = (byte[]) msg.obj;
                            bleManager.writeDataToDevice(data);
                            break;
                        case BLE_CONNECT:

                            bleManager.connect(device,
                                    getIsAutoConnect());
                            break;
                        case NOTIFY_SUCEESS:
                            if (null != mBaseCallBack){
                                CLog.i(TAG, "tell connect success");
                                mBaseCallBack.onConnectSuccessed();
                            }

                            break;
                        default:
                            break;
                    }
                };
            }
        };
    }

    protected boolean getIsAutoConnect() {
        return MobileUtil.isAutoConnect();
    }

    public   void getValue(){}


    public boolean isConnect() {
        return bleManager.isConnect;
    }

    public void startDevice(BluetoothDevice device) {
        if (null == device) {
            CLog.e(TAG, "device null");
            return;
        }
        this.device = device;
        CLog.i(TAG, "start device" + device.getName());
        mTimeoutCheck.setIsConnection(true);
        mTimeoutCheck.startCheckTimeout();
        mHandler.removeMessages(BLE_CONNECT);
        mHandler.sendEmptyMessageDelayed(BLE_CONNECT, 100);
        isStart = true;
    }


    /**
     * 做校验，暂时没有处理
     *
     * @param data
     * @return
     */
    public boolean checkValid(byte[] data) {

        return true;
    }

    @Override
    public void connectState(BluetoothDevice device, int status, int newState) {

    }

    @Override
    public void onConnectFailed(int tryConnectIndex) {
        if (null != mBaseCallBack) mBaseCallBack.onTimeOut();
    }



    @Override
    public void onReceivedFailed() {
        if (null != mBaseCallBack) mBaseCallBack.onTimeOut();

    }

    @Override
    public void onNotifySuccess() {
        CLog.i(TAG, "onNotifySuccess set notify success");
        mTimeoutCheck.stopCheckTimeout();
        mHandler.removeMessages(NOTIFY_SUCEESS);
        mHandler.sendEmptyMessageDelayed(NOTIFY_SUCEESS, 1200);

    }

    @Override
    public void onReConnect(int tryConnectIndex) {
        CLog.i(TAG, "onReConnect");
        bleManager.close();
        if(BluetoothAdapter.getDefaultAdapter().isEnabled()){

            mHandler.removeMessages(BLE_CONNECT);
            mHandler.sendEmptyMessageDelayed(BLE_CONNECT, 800);
        }else{
            onConnectFailed(0);
        }
    }

    @Override
    public void getValue(int value) {
        dealResponse(new byte[]{(byte) (value & 0xff)});
    }

    @Override
    public void getValues(byte[] data) {
        dealResponse(data);

    }

    public void writeDataToDevice(byte[] data) {
        if(null == data ) return;

        mTimeoutCheck.setIsConnection(false);
        mTimeoutCheck.startCheckTimeout();
        isStart = true;
        Message msg = mHandler.obtainMessage(SEND_DATA);
        msg.what = SEND_DATA;
        msg.obj = data;
        mHandler.sendMessageDelayed(msg, EACH_FRAME_DELAY);


        lastData = data;

    }

    @Override
    public void onReSend() {
        CLog.e(TAG, "onresend");
        if (!isStart) return;

        if(BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            mHandler.removeMessages(SEND_DATA);
            Message msg = new Message();
            msg.what = SEND_DATA;
            msg.obj = lastData;
            mHandler.sendMessage(msg);
        }else {
            onReceivedFailed();
        }
    }

    @Override
    public void onWriteFailed() {
        CLog.e(TAG, "onWriteFailed, throw failed result");
        onReceivedFailed();
    }


    @Override
    public void onWriteSuccess(){
        CLog.i(TAG, "baseDeviceSync onWriteSuccess");
    }



    public void stop(){
        CLog.i(TAG, "stop");

        isStart = false;
        mHandler.removeMessages(SEND_DATA);
        mHandler.removeMessages(BLE_CONNECT);

        if (null != mTimeoutCheck) {
            mTimeoutCheck.stopCheckTimeout();
        }
    }

    public void close() {
        CLog.i(TAG, "ble close");
        stop();

        bleManager.close();

        System.gc();

    }

    /**
     * 每一帧发送间隔
     * @param ms
     */
    public void setFrameDelay(int ms){
        EACH_FRAME_DELAY = ms;
    }

    public void stopTimeCheckOut(){
        mTimeoutCheck.stopCheckTimeout();
    }
}
