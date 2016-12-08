package com.communication.ble;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Context;

import com.communication.bean.CodoonProfile;

/**
 * Created by workEnlong on 2015/12/3.
 */
public class GpsBandBleManager  extends BaseBleManager {

    public GpsBandBleManager(Context context) {
        // TODO Auto-generated constructor stub
        super(context);
        setServiceUUID(CodoonProfile.GPSBANDWriteServiceUUID);
        setCharacteristicUUID(CodoonProfile.GPSBANDWriteCharacteristicUUID);
        setDescriptorUUID(CodoonProfile.GPSBANDDescriptorUUID);
    }


    @SuppressLint("NewApi")
    @Override
    protected byte[] getNotifyEnableValue() {
        return BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
    }

    public boolean writeDataToDevice(byte[] bytes){

        return writeIasAlertLevel(CodoonProfile.GPSBANDWriteServiceUUID,
                CodoonProfile.GPSBANDWriteCharacteristicUUID,
                bytes);
    }

    @Override
    public int getWriteType(){
        return BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE;
    }




}
