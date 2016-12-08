package com.communication.ble;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Message;

import com.communication.data.IHeartCallBack;

/**
 * Created by 0H7RXL on 2016/6/21.
 * just do connect and receive notify acitons
 */

public class HeartDeviceSyncManager extends BaseDeviceSyncManager{
    protected IHeartCallBack mISyncDataCallback;

    /**
     * @param mContext
     * @param mCallBack can't be null
     */
    public HeartDeviceSyncManager(Context mContext, IHeartCallBack mCallBack) {
        super(mContext, mCallBack);
        mISyncDataCallback = mCallBack;
    }


    @Override
    public void connectState(BluetoothDevice device, int status, int newState) {
        if(null != mISyncDataCallback){
            mISyncDataCallback.onConnectStateChanged(status, newState);
        }
    }

    @Override
    protected boolean handMessage(Message messagge) {
        return false;
    }

    @Override
    protected void dealResponse(byte[] res) {
        if (null != mISyncDataCallback) {
            mISyncDataCallback.onGetValue(res[0] & 0xff);
        }
    }

    @Override
    protected BaseBleManager initBleManager() {
        bleManager = new HeartBleManager(mContext);
        bleManager.setWriteCallback(this);
        bleManager.setConnectCallBack(this);
        return bleManager;
    }

    @Override
    protected boolean getIsAutoConnect() {
        return true;
    }



}
