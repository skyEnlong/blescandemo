package com.communication.ble;

import android.bluetooth.BluetoothDevice;

public interface IConnectCallback {
  
  public void connectState(BluetoothDevice device, int status,
                           int newState);
 public void getValue(int value);
 public void getValues(byte[] data);
 public void onNotifySuccess();
}
