package com.communication.ble;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Context;

import com.communication.bean.CodoonProfile;

public class CodoonBleManager extends BaseBleManager {


    public CodoonBleManager(Context context) {
        // TODO Auto-generated constructor stub
        super(context);
        setServiceUUID(CodoonProfile.CodoonReadServiceUUID);
        setCharacteristicUUID(CodoonProfile.CodoonReadCharacteristicUUID);
    }

    @Override
    public int getWriteType() {
        return BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT;
    }


    @SuppressLint("NewApi")
    @Override
    protected byte[] getNotifyEnableValue() {
        return BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
    }

    @Override
    protected boolean writeDataToDevice(byte[] data) {
        return writeIasAlertLevel(CodoonProfile.CodoonWriteServiceUUID,
                CodoonProfile.CodoonWriteCharacteristicUUID, data);
    }

}
