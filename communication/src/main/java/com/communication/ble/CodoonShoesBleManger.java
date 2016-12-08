package com.communication.ble;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Context;

import com.communication.bean.CodoonProfile;

/**
 * Created by enlong on 2016/12/7.
 */

public class CodoonShoesBleManger extends BaseBleManager{

    public CodoonShoesBleManger(Context mContext) {
        super(mContext);
    }

    @SuppressLint("NewApi")
    @Override
    protected byte[] getNotifyEnableValue() {
        return BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
    }

    public boolean writeDataToDevice(byte[] bytes){

        return writeIasAlertLevel(CodoonProfile.CodoonWriteServiceUUID,
                CodoonProfile.CodoonWriteCharacteristicUUID,
                bytes);
    }

    @Override
    public int getWriteType(){
        return BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE;
    }


}
