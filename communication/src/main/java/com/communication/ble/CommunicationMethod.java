package com.communication.ble;

import android.bluetooth.BluetoothDevice;

import com.communication.data.ISyncDataCallback;
import com.communication.data.SaveManager;

import java.util.ArrayList;

public interface CommunicationMethod {
    public void start(int[] order);

    public void start(BluetoothDevice device);

    public void stop();

    public void cancel();

    public void SendDataToDevice(final int[] data);

    public void reSendDataToDevice(final int[] data);

    public boolean isConnect();

    public void register(ISyncDataCallback callback);

    public void analysis(ArrayList<Integer> data);

    public void setReadServiceUUID(String clientUUID);

    public void setReadCharacteristicUUID(String characteristicUUID);

    public void setWriteServiceUUID(String clientUUID);

    public void setWriteCharacteristicUUID(String characteristicUUID);

    public void setSaveType(SaveManager.eSaveType saveType);

}
