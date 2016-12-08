package com.communication.ble;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;

import com.communication.bean.CodoonProfile;
import com.communication.data.CLog;

import java.util.UUID;

/**
 * Created by workEnlong on 2015/6/10.
 */
@SuppressLint("NewApi")
public class UnionPayBleManager extends BaseBleManager {

    public UnionPayBleManager(Context context) {
        // TODO Auto-generated constructor stub
        super(context);
        setServiceUUID(CodoonProfile.UnionReadServiceUUID);
        setCharacteristicUUID(CodoonProfile.UnionReadCharacteristicUUID);
        setDescriptorUUID(CodoonProfile.UnionDescriptorUUID);
    }

    @Override
    public int getWriteType() {
        return BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT;

    }


    @SuppressLint("NewApi")
    @Override
    protected byte[] getNotifyEnableValue() {
        return BluetoothGattDescriptor.ENABLE_INDICATION_VALUE;
    }

    public boolean writeDataToDevice(byte[] bytes){

       return writeIasAlertLevel(CodoonProfile.UnionWriteServiceUUID,
                CodoonProfile.UnionWriteCharacteristicUUID,
                bytes);
    }

    /**
     * @param writeServiceUUID
     * @param characteristicID
     * @param bytes
     */
    public boolean writeIasAlertLevel(String writeServiceUUID,
                                      String characteristicID, byte[] bytes) {
//        DataUtil.DebugPrint(bytes);
        if (null == mBluetoothGatt) {
            return false;
        }

        BluetoothGattService alertService = mBluetoothGatt.getService(UUID
                .fromString(writeServiceUUID));
        if (alertService == null) {
            return false;
        }
        BluetoothGattCharacteristic mCharacter = alertService
                .getCharacteristic(UUID.fromString(characteristicID));
        if (mCharacter == null) {
            // showMessage("Immediate Alert Level charateristic not found!");
            return false;
        }
        boolean status = false;
//        int writeType = mCharacter.getWriteType();
//        CLog.i(TAG, "Character writeType" + writeType);
        mCharacter.setValue(bytes);
        mCharacter.setWriteType(mCharacter.getWriteType());
        status = mBluetoothGatt.writeCharacteristic(mCharacter);
        CLog.i(TAG, "write status:" + status);
        return status;
    }

}
