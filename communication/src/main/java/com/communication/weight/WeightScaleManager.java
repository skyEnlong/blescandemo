package com.communication.weight;

import java.util.ArrayList;
import java.util.List;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.communication.bean.WeightInfo;
import com.communication.bean.WeightScaleType;
import com.communication.ble.CodoonBleManager;
import com.communication.ble.IConnectCallback;
import com.communication.data.CLog;
import com.communication.data.DataUtil;
import com.communication.data.ErrInfo;
import com.communication.data.SendData;
import com.communication.data.TimeoutCheck;
import com.communication.util.MobileUtil;

public class WeightScaleManager {

    protected static final String TAG = "WeightScaleManager";

    private String mWriteClicentUUID = "0000180f-0000-1000-8000-00805f9b34fb",
            mWriteCharacteristicUUID = "00002a19-0000-1000-8000-00805f9b34fb";

    private Context mContext;
    private CodoonBleManager mCodoonBleManager;
    private Handler mHandler;
    private BluetoothDevice mDevice;
    private int[] mLastSendData;
    private OnWeightListener mOnWeightListener;
    private TimeoutCheck mTimeoutCheck;

    private final int TIME_OUT = 15000;
    private final int CONNECT_AGAIN = 2;
    private final int ORDER_CONNECT = 1;
    private final int ORDER_GET_WEIGHT = 0x05;
    private final int TIME_OUT_CALL_BACK = 0x111;
    private int connect_again_delay = 1200;
    private boolean isStart;

    private int errCount = 0;

    private int connectTime = 1;
    private IConnectCallback mIConnectCallback;

    private int CONNECT_DELAY = 1200;
    private boolean isHuawei = false;

    public WeightScaleManager(Context mContext, OnWeightListener listener) {
        this.mContext = mContext;
        this.mOnWeightListener = listener;
        mCodoonBleManager = new CodoonBleManager(mContext);
//		CLog.d(TAG, android.os.Build.BRAND);

        isHuawei = MobileUtil.isCurMobileManuConnect();
        if (isHuawei) {
            CONNECT_DELAY = 1300;
            connect_again_delay = 2500;
        }

        mHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                switch (msg.what) {
                    case ORDER_CONNECT:
//					mOnWeightListener.onConnect();
                        CLog.i(TAG, "begin to send oder");
                        sendConnectOrder(SendData.postWeightScaleConnect());
                        break;
                    case CONNECT_AGAIN:
                        CLog.i(TAG, "reconnect");
                        connectDevice();
                        break;

                    case ORDER_GET_WEIGHT:
                        sendDataToService(SendData.postWeightInfo(mOnWeightListener.onLoadPersonInfo()));
                        sendEmptyMessageDelayed(ORDER_GET_WEIGHT, 5000);
                        break;
                    case TIME_OUT_CALL_BACK:
                        doConnectAgain();

                        break;
                }
            }

        };
        mIConnectCallback = new IConnectCallback() {

            @Override
            public void connectState(BluetoothDevice device, int status,
                                     int newState) {
                // TODO Auto-generated method stub
                if (newState == BluetoothAdapter.STATE_CONNECTED) {
                    mTimeoutCheck.setTimeout(TIME_OUT / 2);
                    mTimeoutCheck.restartChectTime();

                    mDevice = device;
                    mHandler.removeMessages(CONNECT_AGAIN);
                } else if (newState == BluetoothAdapter.STATE_DISCONNECTED) {

                    if (isStart && status != BluetoothGatt.GATT_SUCCESS) {
                        CLog.i(TAG, "disconnect by device");
                        doConnectAgain();
                    } else {
                        if (null != mCodoonBleManager) {

                            mCodoonBleManager.close();
                        }
                    }
                }

            }

            @Override
            public void onNotifySuccess() {
                // TODO Auto-generated method stub
                if (isStart) {

                    mHandler.removeMessages(ORDER_CONNECT);
                    mHandler.sendEmptyMessageDelayed(ORDER_CONNECT, CONNECT_DELAY);
                }
            }

            @Override
            public void getValues(byte[] bytes) {
                // TODO Auto-generated method stub
                if (isStart) {
                    int length = bytes.length;
                    ArrayList<Integer> values = new ArrayList<Integer>();
                    for (int i = 0; i < length; i++) {

                        values.add(bytes[i] & 0xff);
                    }


                    analysis(values);
                }
            }

            @Override
            public void getValue(int value) {
                // TODO Auto-generated method stub

            }

        };

        mCodoonBleManager.setConnectCallBack(mIConnectCallback);


        mTimeoutCheck = new TimeoutCheck(mTimeoutCallback);
        mTimeoutCheck.setTryConnectCounts(1);
        mTimeoutCheck.setTimeout(TIME_OUT);
    }

    protected void connectDevice() {
        // TODO Auto-generated method stub
        if (isHuawei) {
            mCodoonBleManager.connect(mDevice, false);
        } else {

            mCodoonBleManager.connect(mDevice, true);
        }
    }

    protected void doConnectAgain() {
        // TODO Auto-generated method stub
        connectTime--;
        if (connectTime <= 0) {
            stop();
            mOnWeightListener.onTimeOut(ErrInfo.ERR_UNKOWN);
            return;
        }
        reConnectBle();
    }

    private TimeoutCheck.ITimeoutCallback mTimeoutCallback = new TimeoutCheck.ITimeoutCallback() {

        @Override
        public void onReceivedFailed() {
            mHandler.sendEmptyMessage(TIME_OUT_CALL_BACK);
        }

        @Override
        public void onReSend() {
            if (isStart) {
                CLog.i(TAG, "onReSend");
                reSendDataToDevice(mLastSendData);
            }

        }

        @Override
        public void onReConnect(int tryConnectIndex) {
            CLog.i(TAG, "onReConnect");
            reConnectBle();
        }

        @Override
        public void onConnectFailed(int tryConnectIndex) {
            CLog.i(TAG, "onConnectFailed");
            mHandler.sendEmptyMessage(TIME_OUT_CALL_BACK);
        }
    };

    protected void reConnectBle() {
        // TODO Auto-generated method stub
        mTimeoutCheck.restartChectTime();
        mHandler.removeMessages(ORDER_CONNECT);
        mHandler.removeMessages(CONNECT_AGAIN);
        if (null != mCodoonBleManager) {
            mCodoonBleManager.disconnect();
            mHandler.removeMessages(CONNECT_AGAIN);
            mHandler.sendEmptyMessageDelayed(CONNECT_AGAIN, connect_again_delay);
        }


    }

    public void sendDataToService(int[] order) {
        mTimeoutCheck.setTryConnectCounts(1);
        mTimeoutCheck.setTimeout(TIME_OUT * 5);
        mTimeoutCheck.startCheckTimeout();

        if (mCodoonBleManager != null) {
            mLastSendData = order;
            mCodoonBleManager.writeIasAlertLevel(mWriteClicentUUID,
                    mWriteCharacteristicUUID, intToByte(order));
        }
    }

    public void sendConnectOrder(int[] order) {
        mTimeoutCheck.setIsConnection(false);
        mTimeoutCheck.setTimeout(TIME_OUT / 3);
        mTimeoutCheck.setTryConnectCounts(3);
        mTimeoutCheck.startCheckTimeout();
        if (mCodoonBleManager != null) {
            mLastSendData = order;
            mCodoonBleManager.writeIasAlertLevel(mWriteClicentUUID,
                    mWriteCharacteristicUUID, intToByte(order));
        }
    }

    /**
     *
     */
    public void start(BluetoothDevice device) {
        isStart = true;
        errCount = 0;
        connectTime = 1;
        mDevice = device;
        isStart = true;
        connectDevice();

        mTimeoutCheck.setIsConnection(true);
        mTimeoutCheck.setTryConnectCounts(1);
        if (isHuawei) {
            mTimeoutCheck.setTimeout(TIME_OUT / 4);
        } else {

            mTimeoutCheck.setTimeout(TIME_OUT * 2 / 3);
        }
        mTimeoutCheck.startCheckTimeout();
    }

    /**
     *
     */
    public void stop() {
        isStart = false;
        errCount = 0;
        mLastSendData = null;
        mHandler.removeMessages(ORDER_CONNECT);
        mHandler.removeMessages(CONNECT_AGAIN);
        mHandler.removeMessages(TIME_OUT_CALL_BACK);
        mHandler.removeMessages(ORDER_GET_WEIGHT);
        if (null != mTimeoutCheck) {
            mTimeoutCheck.stopCheckTimeout();
        }
        if (mCodoonBleManager != null) {
            mCodoonBleManager.close();
        }
        mDevice = null;
    }

    /**
     * @param datas
     * @return
     */
    private byte[] intToByte(int[] datas) {
        int size = datas.length;
        byte[] bytes = new byte[20];
        for (int i = 0; i < size; i++) {
            bytes[i] = (byte) (datas[i] & 0x000000ff);
        }

        for (int i = size; i < 20; i++) {
            bytes[i] = 0;
        }
        return bytes;
    }

    /**
     * @param datas
     */
    private void reSendDataToDevice(final int[] datas) {
        if (mCodoonBleManager != null) {
            // mLastSendData = datas;
            mCodoonBleManager.writeIasAlertLevel(mWriteClicentUUID,
                    mWriteCharacteristicUUID, intToByte(datas));
        }
    }

    public void analysis(List<Integer> org_list) {
        // FD 31 00 00 00 00 00 31
        if (null != org_list && org_list.size() > 0) {
            mTimeoutCheck.stopCheckTimeout();

            if (isConnectWeightScale(org_list)) {

                mOnWeightListener.onConnect();
                CLog.i(TAG, "begint to send person info");
//				sendDataToService(SendData.postWeightInfo(mOnWeightListener.onLoadPersonInfo()));
                mHandler.removeMessages(ORDER_GET_WEIGHT);
                mHandler.sendEmptyMessage(ORDER_GET_WEIGHT);
            } else if (org_list.get(1) == 0x85) {

                List<Integer> list = new ArrayList<Integer>();

                int length = org_list.get(2);
                for (int i = 3; i < length + 3; i++) {
                    list.add(org_list.get(i));
                }

                if (isErrBluetoothInfo(list)) {
                    if (errCount < 3) {

                        errCount++;
                        mTimeoutCheck.startCheckTimeout();
                        mHandler.removeMessages(ORDER_GET_WEIGHT);
                        mHandler.sendEmptyMessage(ORDER_GET_WEIGHT);
                    } else {
                        CLog.i(TAG, "err count has up to 3");
                        mOnWeightListener.onTimeOut(ErrInfo.ERR_BLUETOOTH);
                    }
                } else if (isErrFatInfo(list)) {
                    mOnWeightListener.onTimeOut(ErrInfo.ERR_FAT);

                } else {
                    mHandler.removeMessages(ORDER_GET_WEIGHT);
                    WeightScaleType type = WeightScaleType.getValue(list.get(0));
                    if (type.ordinal() != WeightScaleType.NONE.ordinal()) {

                        try {
                            WeightInfo info = new WeightInfo();
                            info.level = (list.get(1) >> 4) & 0x0f;
                            info.group = list.get(1) & 0x0f;
                            info.sex = (list.get(2) >> 7) & 0x01;
                            info.age = list.get(2) & 0x7f;
                            info.height = list.get(3);
                            info.weight = ((list.get(4) << 8) + list.get(5))
                                    * WeightScaleType.getResolutionByType(type);
                            info.fatRate = ((list.get(6) << 8) + list.get(7)) * 0.1f;
//							info.boneRate = list.get(8) * 0.1f / info.weight * 100;
                            info.boneRate = list.get(8) * 0.1f;
                            info.muscleRate = ((list.get(9) << 8) + list.get(10)) * 0.1f;
                            info.fatLevel = list.get(11);
                            info.waterRate = ((list.get(12) << 8) + list.get(13)) * 0.1f;
                            info.BMR = (list.get(14) << 8) + list.get(15);

                            if (null != mOnWeightListener) {
                                mOnWeightListener.onGetDeiveId(type.ordinal());
                                mOnWeightListener.onGetWeightInfo(info);
                            }
                        } catch (Exception e) {
                            mOnWeightListener.onTimeOut(ErrInfo.ERR_UNKOWN);
                        }


                    } else {
                        mOnWeightListener.onTimeOut(ErrInfo.ERR_UNKOWN);
                    }
                }

            }
        }
    }


    private boolean isConnectWeightScale(List<Integer> list) {
        // TODO Auto-generated method stub
        if (list.get(1) == 0x81) {

            return true;
        }
        return false;
    }

    private boolean isErrFatInfo(List<Integer> list) {
        // TODO Auto-generated method stub
        DataUtil.DebugPrint(list);
        int[] err = new int[]{0x00fd, 0x31, 0x0, 0x0, 0x0, 0x0, 0x0, 0x33};
        try {

            for (int i = 0; i < err.length; i++) {
                if ((list.get(i).intValue() & 0x00ff) != err[i]) {
                    return false;
                }
            }
        } catch (Exception e) {
            return false;
        }

        CLog.i(TAG, "fat err");
        return true;
    }

    private boolean isErrBluetoothInfo(List<Integer> list) {
        // TODO Auto-generated method stub
        DataUtil.DebugPrint(list);
        int[] err = new int[]{0x00fd, 0x31, 0x00, 0x00, 0x00, 0x00, 0x00, 0x31};
        try {

            for (int i = 0; i < err.length; i++) {
                if ((list.get(i).intValue() & 0x00ff) != err[i]) {
                    return false;
                }
            }
        } catch (Exception e) {
            return false;
        }

        CLog.i(TAG, "Bluetooth connect err");
        return true;
    }
}
