package com.communication.ble;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Context;

import com.communication.bean.CodoonProfile;

/**
 * Created by 0H7RXL on 2016/6/21.
 */
@SuppressLint("NewApi")
public class HeartBleManager extends BaseBleManager {

    public static final int TYPE_HEART_RATE = 1;
    public static final int TYPE_BATTERY = 2;
    private int curState = TYPE_HEART_RATE;

    public HeartBleManager(Context mContext) {
        super(mContext);
        setServiceUUID(CodoonProfile.HeartServiceUUID);
        setCharacteristicUUID(CodoonProfile.HeartCharactertUUID);
    }


    @Override
    public void dealWithCharacteristicChanged(BluetoothGatt gatt,
                                              BluetoothGattCharacteristic characteristic) {

        int value = characteristic.getValue()[0];

        if (isHeartRateInUINT16(characteristic.getValue()[0])) {
//				CLog.i(TAG, "HeartRateInUINT16");
            value = characteristic.getIntValue(
                    BluetoothGattCharacteristic.FORMAT_UINT16, 1);
        } else {
//				CLog.i(TAG, "HeartRateInUINT8");
            value = characteristic.getIntValue(
                    BluetoothGattCharacteristic.FORMAT_UINT8, 1);
        }

        if (null != mConnectCallback) {

            mConnectCallback.getValue(value);
        }
    }

    private boolean isHeartRateInUINT16(byte flags) {
        if ((flags & 0x01) != 0)
            return true;
        return false;
    }


    @Override
    protected boolean writeDataToDevice(byte[] data) {
        return false;
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
}
