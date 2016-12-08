package com.communication.unionpay;

import android.bluetooth.BluetoothDevice;

import com.communication.data.ISyncDataCallback;

/**
 * Created by workEnlong on 2015/6/11.
 */
public interface UnionDeviceController {
    public void registerISyncDataCallback(ISyncDataCallback mISyncDataCallback);
    public void registerUnionPayCallback(IUnionPayCallback mIUnionPayCallback);

    public void unRegisterISyncDataCallback(ISyncDataCallback mISyncDataCallback);
    public void unRegisterUnionPayCallback(IUnionPayCallback mIUnionPayCallback);

    public void stop();
    public void close();
    public void sendDataToDevice(int[] data);

    /**
     *  connect device
     * @param device
     */
    public void start(BluetoothDevice device);

    /**
     *  unBindDevice
     * @param device
     */
    public void unBindDevice(BluetoothDevice device);

    /**
     *  bindDevice (don't need call @link{start})
     * @param device
     */

    public void bindDevice(BluetoothDevice device);


    /**
     *
     * @param command
     * @param data
     */
    public void writeUnionCmd(int command, byte[] data);

}
