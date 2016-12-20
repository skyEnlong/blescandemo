package com.communication.ble;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.communication.bean.CodoonProfile;
import com.communication.data.CLog;
import com.communication.data.DataUtil;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

/**
 * Created by workEnlong on 2015/6/10.
 */
@SuppressLint("NewApi")
public abstract class BaseBleManager {

    protected final String TAG = "ble";
    private Context mContext;

    protected BluetoothDevice mDevice;

    public static final UUID CCC = UUID
            .fromString(CodoonProfile.CodoonDescriptorUUID);

    protected static final int NOTIFY = 0x05050505;
    protected static final int NOTIFY_SUCCESS = 0x05050507;
    protected static final int DISCOVER_SERVICE = 0x05050506;

    protected BluetoothGatt mBluetoothGatt;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    protected IConnectCallback mConnectCallback;

    private OnBleWriteCallBack mWriteCallback;
    private String mServiceUUID = CodoonProfile.CodoonReadServiceUUID;
    private String mCharacteristicUUID = CodoonProfile.CodoonReadCharacteristicUUID;

    private String mDescriptorUUID = CodoonProfile.CodoonDescriptorUUID;

    public boolean isConnect = false;
    private BluetoothGattDescriptor mDescriptor;
    protected BluetoothGattCallback mGattCallback;
    private boolean hasRegisterBoundReceiver;

    private boolean hasNotifySuccess;

    protected Handler mHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what) {
                case DISCOVER_SERVICE:
                    mBluetoothGatt.discoverServices();
                    break;
                case NOTIFY:

                {
                    CLog.i(TAG, "write descriptor");
                    // TODO Auto-generated method stub
                    if (null != mDescriptor &&
                            !mDescriptor.setValue(getNotifyEnableValue())) {
                        CLog.i(TAG, "Failed to set descriptor value");
                        mHandler.sendEmptyMessageDelayed(NOTIFY, 100);
                        return;
                    }
                    if (mBluetoothGatt != null) {
                        hasNotifySuccess = false;
                        boolean result = mBluetoothGatt.writeDescriptor(mDescriptor);
                        CLog.i(TAG, "write desriptor suceess ? " + result);

                        if (!result && null != mWriteCallback) {
                            mWriteCallback.onWriteFailed();
                        }
                        if(result){
                            sendEmptyMessageDelayed(NOTIFY_SUCCESS, 2000);
                        }

                    }

                }

                break;

                case NOTIFY_SUCCESS:

                    if(null != mConnectCallback) {
                        CLog.e(TAG, "2 seconds after not receive write descriptor call back");

                        hasNotifySuccess = true;
                        mConnectCallback.onNotifySuccess();
                    }else {
                        Log.e(TAG, "no mConnectCallback");
                    }
                    break;
            }
        }

    };




    public void setConnectCallBack(IConnectCallback callback) {
        mConnectCallback = callback;
    }

    protected abstract byte[] getNotifyEnableValue();

    protected abstract boolean writeDataToDevice(byte[] data);

    protected abstract int getWriteType();

    public void dealWithCharacteristicChanged(BluetoothGatt gatt,
                                              BluetoothGattCharacteristic characteristic){
        CLog.i(TAG, "onCharacteristicChanged()");
        byte[] bytes = characteristic.getValue();

        DataUtil.DebugPrint(TAG + "_receive: ", bytes);
        if (mConnectCallback != null) {
            mConnectCallback.getValues(bytes);
        } else {
            CLog.i(TAG, "no call back");
        }
    }

    protected void initGattCallback() {
        mGattCallback = new BluetoothGattCallback() {

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt,
                                                BluetoothGattCharacteristic characteristic) {
               dealWithCharacteristicChanged(gatt, characteristic);
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt,
                                             BluetoothGattCharacteristic characteristic, int status) {
                CLog.i(TAG, "onCharacteristicRead()");
                super.onCharacteristicRead(gatt, characteristic, status);
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt,
                                              BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicWrite(gatt, characteristic, status);

                if (status == BluetoothGatt.GATT_SUCCESS) {
                    CLog.i(TAG, "onCharacteristicWrite() success ");

                    if (null != mWriteCallback) mWriteCallback.onWriteSuccess();

                } else {
                    CLog.e(TAG, "onCharacteristicWrite() failed ");

                    if (null != mWriteCallback) mWriteCallback.onWriteFailed();

                }
            }

            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                                int newState) {
                try {
                    mBluetoothGatt = gatt;
                    if (status == BluetoothGatt.GATT_SUCCESS) {

                        if (newState == BluetoothProfile.STATE_CONNECTED
                                && mBluetoothGatt != null) {
                            if (!isConnect) {

                                isConnect = true;

                                CLog.i(TAG, "onConnectionStateChange:connected");

                                mHandler.sendEmptyMessage(DISCOVER_SERVICE);
                                if (null != mConnectCallback) {

                                    mConnectCallback.connectState(mDevice, status, newState);
                                }
                            }

                        } else if (newState == BluetoothProfile.STATE_DISCONNECTED
                                && mBluetoothGatt != null) {
                            CLog.i(TAG, "onConnectionStateChange:disconnected");
                            isConnect = false;
                            if (null != mConnectCallback) {

                                mConnectCallback.connectState(mDevice, status, newState);
                            }
                        }
                    } else {
                        isConnect = false;
                        if (null != mConnectCallback) {
                            CLog.i(TAG, "onConnectionStateChange:disconnecct ");
                            mConnectCallback.connectState(mDevice, status, newState);
                        }
                    }
                } catch (Exception e) {
                    CLog.e(TAG, "err:" + e.getMessage());
                }
            }

            @Override
            public void onDescriptorRead(BluetoothGatt gatt,
                                         BluetoothGattDescriptor descriptor, int status) {
                CLog.i(TAG,
                        "onDescriptorRead() status? :" + (status == BluetoothGatt.GATT_SUCCESS));
//                mHandler.removeMessages(NOTIFY);
//                if (status == BluetoothGatt.GATT_SUCCESS) {
//                    mBluetoothGatt = gatt;
//                    descriptor
//                            .setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//
//                    gatt.writeDescriptor(descriptor);
//                }
            }

            @Override
            public void onDescriptorWrite(BluetoothGatt gatt,
                                          BluetoothGattDescriptor descriptor, int status) {
                // TODO Auto-generated method stub
                CLog.i(TAG,
                        "onDescriptorWrite() status ? "
                                + (status == BluetoothGatt.GATT_SUCCESS));

                super.onDescriptorWrite(gatt, descriptor, status);
                mHandler.removeMessages(NOTIFY_SUCCESS);

                if (status == BluetoothGatt.GATT_SUCCESS &&
                        null != mConnectCallback) {

                    unRegisterBoundBroadcast();
                    if(!hasNotifySuccess){
                        mConnectCallback.onNotifySuccess();
                    }

                }


            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                // super.onServicesDiscovered(gatt, status);
                CLog.i(TAG, "onServicesDiscovered()");
                hasNotifySuccess = false;
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    mBluetoothGatt = gatt;
                    mHandler.removeMessages(NOTIFY);
                    UUID serviceUUID = UUID.fromString(getServiceUUID());
                    UUID characteristicUUID = UUID.fromString(getCharacteristicUUID());
                    UUID descriptorUUID = UUID.fromString(getDescriptorUUID());

                    enableNotify(gatt, serviceUUID,
                            characteristicUUID, descriptorUUID);
                } else {
                    CLog.e(TAG, "err reson:" + status + " and try connect ble agin");

                }
            }
        };
    }

    public BaseBleManager(Context mContext) {
        this.mContext = mContext;
        initialize();
        initGattCallback();
    }

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    private boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter
        // through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) mContext
                    .getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                CLog.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();

        if (mBluetoothAdapter == null) {
            CLog.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        CLog.i(TAG, "start ble service");
        return true;
    }

    protected boolean isInUINT16(byte flags) {
        if ((flags & 0x01) != 0)
            return true;
        return false;
    }


    /**
     * connect device
     *
     * @param device
     * @param autoconnect
     */
    public void connect(BluetoothDevice device, boolean autoconnect) {
        CLog.i(TAG, "connect device");
        if (null == device) return;
        hasNotifySuccess = false;

        if (mBluetoothGatt == null) {
           registerBoundReceiver();
            mBluetoothGatt = device.connectGatt(mContext, autoconnect,
                    mGattCallback);
            if (Build.VERSION.SDK_INT >= 21) {
                mBluetoothGatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH);
            }
        } else {
            mBluetoothGatt.connect();
        }
    }

    private void registerBoundReceiver() {
        final IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        mContext.registerReceiver(mBondingBroadcastReceiver, filter);

        hasRegisterBoundReceiver = true;

    }



    public void unpairDevice(BluetoothDevice device) {
        try {
            Method m = device.getClass()
                    .getMethod("removeBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
        } catch (Exception e) {
            CLog.e(TAG, e.getMessage());
        }
    }

    /**
     * close connect device
     */
    public void close() {
        isConnect = false;
        mHandler.removeMessages(NOTIFY_SUCCESS);

        mHandler.removeMessages(NOTIFY);
        unRegisterBoundBroadcast();
        mHandler.removeMessages(DISCOVER_SERVICE);
        if (mBluetoothGatt != null) {
//			mBluetoothGatt.disconnect();
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
    }

    public void disconnect() {
        isConnect = false;
        mHandler.removeMessages(NOTIFY_SUCCESS);
        mHandler.removeMessages(DISCOVER_SERVICE);
        mHandler.removeMessages(NOTIFY);
        unRegisterBoundBroadcast();
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
        }
    }


    /**
     * @param serviceUUID
     * @param characteristicUUID
     */
    protected boolean enableNotify(BluetoothGatt gatt, UUID serviceUUID,
                                   UUID characteristicUUID, UUID descriptorUUID) {
        if (null == gatt) {
            return false;
        }

        CLog.i(TAG, "enableNotification ");
        List<BluetoothGattService> list = mBluetoothGatt.getServices();
        for (BluetoothGattService service : list) {
            CLog.i(TAG, "service:" + service.getUuid());
        }
        BluetoothGattService service = mBluetoothGatt.getService(serviceUUID);
        if (service == null) {
            CLog.e(TAG, " service not found:" + serviceUUID.toString());
            if (null != mConnectCallback)
                mConnectCallback.connectState(mDevice, BluetoothGatt.GATT_FAILURE,
                        BluetoothGatt.STATE_DISCONNECTED);
            return false;
        }
        CLog.i(TAG, "find service");
        BluetoothGattCharacteristic mHRMcharac = service
                .getCharacteristic(characteristicUUID);
        if (mHRMcharac == null) {
            CLog.e(TAG, " charateristic not found!");
            if (null != mConnectCallback)
                mConnectCallback.connectState(mDevice, BluetoothGatt.GATT_FAILURE,
                        BluetoothGatt.STATE_DISCONNECTED);
            return false;
        }
        CLog.i(TAG, "find BluetoothGattCharacteristic");

        boolean isNotify = false;

        boolean result = gatt.setCharacteristicNotification(mHRMcharac, true);

        CLog.i(TAG, "setCharacteristicNotification result :" + result + " Propertie: " + mHRMcharac.getProperties());

        BluetoothGattDescriptor descriptor = mHRMcharac
                .getDescriptor(descriptorUUID);
        if (null != descriptor) {
            mDescriptor = descriptor;

            doWriteDescriptor(gatt, descriptor);
//			gatt.readDescriptor(descriptor);
//
//			descriptor
//					.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//			isNotify = gatt.writeDescriptor(descriptor);


        } else {
            mHRMcharac.setValue(getNotifyEnableValue());
            boolean r = gatt.writeCharacteristic(mHRMcharac);

            if (r && null != mConnectCallback)
                mConnectCallback.onNotifySuccess();
        }

        return isNotify;
    }

    public void doWriteDescriptor(BluetoothGatt gatt, BluetoothGattDescriptor descriptor) {
        mHandler.sendEmptyMessageDelayed(NOTIFY, 500);
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
        DataUtil.DebugPrint(TAG + "_write", bytes);

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
        mCharacter.setWriteType(getWriteType());
        status = mBluetoothGatt.writeCharacteristic(mCharacter);
        CLog.i(TAG, "write status:" + status);
        return status;
    }


    public String getServiceUUID() {
        return mServiceUUID;
    }

    public void setServiceUUID(String mServiceUUID) {
        this.mServiceUUID = mServiceUUID;
    }

    public String getCharacteristicUUID() {
        return mCharacteristicUUID;
    }

    public void setCharacteristicUUID(String mCharacteristicUUID) {
        this.mCharacteristicUUID = mCharacteristicUUID;
    }

    public void setDescriptorUUID(String mDescriptorUUID) {
        this.mDescriptorUUID = mDescriptorUUID;
    }


    public String getDescriptorUUID() {
        return mDescriptorUUID;
    }

    public OnBleWriteCallBack getWriteCallback() {
        return mWriteCallback;
    }

    public void setWriteCallback(OnBleWriteCallBack mWriteCallback) {
        this.mWriteCallback = mWriteCallback;
    }


    private BroadcastReceiver mBondingBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            final int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);
            final int previousBondState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, -1);

            CLog.i(TAG, "Bond state changed for: " + device.getAddress() + " new state: " + bondState + " previous: " + previousBondState);

            // skip other devices
            if (null == device || !device.getAddress().equals(mBluetoothGatt.getDevice().getAddress()))
                return;

            if (bondState == BluetoothDevice.BOND_BONDED) {
                // Continue to do what you've started before
                UUID serviceUUID = UUID.fromString(getServiceUUID());
                UUID characteristicUUID = UUID.fromString(getCharacteristicUUID());
                UUID descriptorUUID = UUID.fromString(getDescriptorUUID());

                unRegisterBoundBroadcast();

                enableNotify(mBluetoothGatt, serviceUUID,
                        characteristicUUID, descriptorUUID);


            }
        }
    };

    private void unRegisterBoundBroadcast(){
        if(!hasRegisterBoundReceiver) return;
        hasRegisterBoundReceiver = false;
        try {
            mContext.unregisterReceiver(mBondingBroadcastReceiver);
        }catch (Exception e){

        }
    }
}
