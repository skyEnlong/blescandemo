package com.communication.ble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.communication.data.ISyncDataCallback;
import com.communication.data.TimeoutCheck;
import com.communication.util.MobileUtil;

import java.util.ArrayList;

/**
 * Created by workEnlong on 2015/2/28.
 * just do ble connect action and nothing do more
 */
public class SimpleBleManager extends BaseCommunicationManager {
    private static final long RECONNECT_DELAY = 3000;
    private boolean isAutoConnect = false;
    private int TIME_OUT = 8000;
    private final int CONNECT_AGAIN = 2;
    private final int TIME_OUT_CALL_BACK = 0x111;
    private IConnectCallback connectBack;

    private Handler mHandler;
    private CodoonBleManager mCodoonBleManager;

    private BluetoothDevice mDevice;

    public SimpleBleManager(Context mContext, final ISyncDataCallback callback) {
        this.mContext = mContext;
        isAutoConnect = MobileUtil.isAutoConnect();
        mISyncDataCallback = callback;
        register(callback);

        mHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                if (msg.what == CONNECT_AGAIN) {


                    mCodoonBleManager.connect(mDevice, isAutoConnect);
                } else if (msg.what == TIME_OUT_CALL_BACK) {
                    if (isStart) {

                        mISyncDataCallback.onTimeOut();
                    }
                }
            }

        };

        mTimeoutCheck = new TimeoutCheck(this);
        mTimeoutCheck.setTryConnectCounts(3);
        mTimeoutCheck.setTimeout(TIME_OUT);
        mTimeoutCheck.setIsConnection(true);
        mCodoonBleManager = new CodoonBleManager(mContext);

        connectBack = new IConnectCallback() {

            @Override
            public void getValues(byte[] bytes) {
                // TODO Auto-generated method stub

            }


            @Override
            public void getValue(int value) {
                if (null != callback) {
                    callback.onSyncDataProgress(value);
                }
            }

            @Override
            public void onNotifySuccess() {
                // TODO Auto-generated method stub
//                if(null != callback){
//                    callback.onBindSucess();
//                }
            }

            @Override
            public void connectState(BluetoothDevice device, int status, int newState) {
                // TODO Auto-generated method stub
                if (newState == BluetoothGatt.STATE_CONNECTED
                        && status == BluetoothGatt.GATT_SUCCESS) {
                    if (null != callback) {
                        callback.onBindSucess();
                    }
                }
            }
        };
        mCodoonBleManager.setConnectCallBack(connectBack);
    }

    /**
     * @param clientUUID
     */
    public void setReadServiceUUID(String clientUUID) {
        mCodoonBleManager.setServiceUUID(clientUUID);
    }

    /**
     * @param characteristicUUID
     */
    public void setReadCharacteristicUUID(String characteristicUUID) {
        mCodoonBleManager.setCharacteristicUUID(characteristicUUID);
    }

    @Override
    public void start(int[] order) {

    }

    @Override
    public void start(BluetoothDevice device) {
        mCodoonBleManager.connect(device, isAutoConnect);
        mTimeoutCheck.startCheckTimeout();
    }

    @Override
    public void stop() {
        mHandler.removeMessages(CONNECT_AGAIN);
        mCodoonBleManager.close();
    }

    @Override
    public void cancel() {

        mCodoonBleManager.disconnect();

    }

    @Override
    public void SendDataToDevice(int[] data) {

    }

    @Override
    public void reSendDataToDevice(int[] data) {

    }

    @Override
    public boolean isConnect() {
        return mCodoonBleManager.isConnect;
    }

    @Override
    public void analysis(ArrayList<Integer> data) {

    }

    @Override
    public void onReConnect(int tryConnectIndex) {
        mTimeoutCheck.restartChectTime();
        mHandler.removeMessages(CONNECT_AGAIN);

        if (null != mCodoonBleManager) {
            mCodoonBleManager.disconnect();
            mHandler.removeMessages(CONNECT_AGAIN);
            mHandler.sendEmptyMessageDelayed(CONNECT_AGAIN, RECONNECT_DELAY);
        }
    }

    @Override
    public void onConnectFailed(int tryConnectIndex) {

        mISyncDataCallback.onTimeOut();
    }

    @Override
    public void onReSend() {

    }

    @Override
    public void onReceivedFailed() {

    }
}
