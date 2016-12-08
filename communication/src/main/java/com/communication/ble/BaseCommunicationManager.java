package com.communication.ble;

import android.content.Context;

import com.communication.bean.CodoonProfile;
import com.communication.data.ISyncDataCallback;
import com.communication.data.SaveManager;
import com.communication.data.TimeoutCheck;
import com.communication.util.CommonUtils;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseCommunicationManager implements CommunicationMethod,
        TimeoutCheck.ITimeoutCallback {

    protected int frameCount = 0;

    protected int indexFrame = 0;

    protected int[] mLastSendData;

    protected ArrayList<ArrayList<Integer>> mRecordDatas;

    protected Context mContext;
    private SaveManager.eSaveType mSaveType = SaveManager.eSaveType.DATABSE;

    protected TimeoutCheck mTimeoutCheck;

    protected String mClicentUUID, mCharacteristicUUID;

    protected String mWriteClicentUUID = CodoonProfile.CodoonWriteServiceUUID,
            mWriteCharacteristicUUID = CodoonProfile.CodoonWriteCharacteristicUUID;

    protected ByteArrayOutputStream mBaos;
    protected ISyncDataCallback mISyncDataCallback;

    protected boolean isStart;
    private final byte[] xorKey = new byte[]{0x54, (byte) (0x91 & 0xff), 0x28, 0x15,
            0x57, 0x26};

    protected ISimpleBleCallBack mSimpleCallback;


    public void register(ISimpleBleCallBack callback) {
        mSimpleCallback = (callback);
    }


    /**
     * @param datas
     * @return
     */
    protected byte[] intToByte(int[] datas) {

        return CommonUtils.intToByte(datas);
    }


    /**
     * @param clientUUID
     */
    public void setReadServiceUUID(String clientUUID) {
        mClicentUUID = clientUUID;
    }

    /**
     * @param characteristicUUID
     */
    public void setReadCharacteristicUUID(String characteristicUUID) {
        mCharacteristicUUID = characteristicUUID;
    }

    /**
     * @param clientUUID
     */
    public void setWriteServiceUUID(String clientUUID) {
        mWriteClicentUUID = clientUUID;
    }

    /**
     * @param characteristicUUID
     */
    public void setWriteCharacteristicUUID(String characteristicUUID) {
        mWriteCharacteristicUUID = characteristicUUID;
    }


    public void register(ISyncDataCallback callback) {
        mISyncDataCallback = callback;
    }

    public byte encryptMyxor(int original, int n) {
        return (byte) ((original ^ xorKey[n]) & 0xFF);
    }


    /**
     * @param count default count is 3 times
     */
    public void setTryConnectCounts(int count) {
        mTimeoutCheck.setTryConnectCounts(count);
    }

    /**
     * @param saveType default save type is database
     */
    public void setSaveType(SaveManager.eSaveType saveType) {
        mSaveType = saveType;
    }


    @Override
    public void cancel() {
        if (null != mTimeoutCheck) {
            mTimeoutCheck.stopCheckTimeout();
        }
    }

}
