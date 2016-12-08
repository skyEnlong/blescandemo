package com.example.enlong.blescandemo;

import android.bluetooth.BluetoothDevice;

/**
 * Created by enlong on 16/10/12.
 */
public interface IConnectCallback {
    public void connectState(BluetoothDevice device, int status,
                             int newState);

    public void getValues(byte[] data);
    public void onNotifySuccess();
}
