package com.communication.ble;

import android.bluetooth.BluetoothDevice;

import com.communication.data.ISyncCallBack;

/**
 * Created by workEnlong on 2015/3/25.
 */
public interface ISimpleBleCallBack {

    /**
     *
     * @param value
     * @param type
     */
    public void onGetValueAndTypes(int value, int type);


    /**
     * @param device
     * @param status
     * @param newState   2: connected  0: disconnected
     * @return
     */
    public void onConnectStateChanged(BluetoothDevice device, int status,
                                      int newState);


    public void onServiceDiscovered();


    public void onNotifySuccess();


    public void onConnectFailed();
}
