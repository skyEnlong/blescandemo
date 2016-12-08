package com.communication.lenovo.ble;

import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.communication.ble.IConnectCallback;
import com.communication.data.CLog;

public class LenovoBleManager {

	private static LenovoBleManager instance;
	private final String TAG = LenovoBleManager.class.toString();

	private Context mContext;

	private BluetoothDevice mDevice;

	public static final UUID CCC = UUID
			.fromString("00002902-0000-1000-8000-00805f9b34fb");

	protected static final int NOTIFY = 5;
	protected static final int NOTIFY_BATTERY = 6;
	protected static final int DIS_NOTIFY_BATTERY = 7;
	private BluetoothGatt mBluetoothGatt;
	private BluetoothManager mBluetoothManager;
	private BluetoothAdapter mBluetoothAdapter;
	private boolean isForceDisconnect = false;
	private IConnectCallback mConnectCallback;
	private String mClicentUUID = "0000180f-0000-1000-8000-00805f9b34fb";
	private String mCharacteristicUUID = "00002a19-0000-1000-8000-00805f9b34fb";
	private String mCommandClicentUUID = "00001500-0000-1000-8000-00805F9B34FB";
	private String mCommandCharacteristicUUID = "00001520-0000-1000-8000-00805F9B34FB";

	public boolean isConnect = false;
	private BluetoothGattDescriptor mDescriptor;
	private BluetoothGattDescriptor mBatteryDescriptor;
	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if(msg.what == NOTIFY){

				// TODO Auto-generated method stub
				while(!mDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)){
					CLog.i(TAG, "Failed to set descriptor value");
					mHandler.sendEmptyMessageDelayed(NOTIFY, 100);
				}
//				mDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
				if (mBluetoothGatt != null) {
					
					boolean result = mBluetoothGatt.writeDescriptor(mDescriptor);
					CLog.i(TAG, "notify suceess ? " + result);
					if(result){
						if(null != mConnectCallback)
						mConnectCallback.onNotifySuccess();
					}
				}
			
			}else if(msg.what == NOTIFY_BATTERY){
		
				while(!mBatteryDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)){
					CLog.i(TAG, "Failed to set descriptor value");
					mHandler.sendEmptyMessageDelayed(NOTIFY_BATTERY, 100);
				}
//				mDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
				if (mBluetoothGatt != null) {
					
					boolean result = mBluetoothGatt.writeDescriptor(mBatteryDescriptor);
					CLog.i(TAG, "notify suceess ? " + result);
					
				}
			}else if(msg.what == DIS_NOTIFY_BATTERY){
		
				while(!mBatteryDescriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE)){
					CLog.i(TAG, "Failed to set descriptor value");
					mHandler.sendEmptyMessageDelayed(DIS_NOTIFY_BATTERY, 100);
				}
//				mDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
				if (mBluetoothGatt != null) {
					
					boolean result = mBluetoothGatt.writeDescriptor(mBatteryDescriptor);
					CLog.i(TAG, "notify suceess ? " + result);
					
				}
			}
		}
		
	};
	
	
	public LenovoBleManager(Context context) {
		// TODO Auto-generated constructor stub
		mContext = context;
		initialize();
	}

	public static LenovoBleManager getInstance(Context context){
		if(null == instance){
			instance = new LenovoBleManager(context);
		}
		instance.setContext(context);
		return instance;
	}

	private void setContext(Context context) {
		// TODO Auto-generated method stub
		mContext = context;
	}
	
	public void register(IConnectCallback callback){
		mConnectCallback = callback;
	}

	public void  unRegister(IConnectCallback callback){
		mConnectCallback = null;
	}
	/**
	 * 
	 * @param clientUUID
	 */
	public void setClientUUID(String clientUUID) {
		mClicentUUID = clientUUID;
	}

	/**
	 * 
	 * @param characteristicUUID
	 */
	public void setCharacteristicUUID(String characteristicUUID) {
		mCharacteristicUUID = characteristicUUID;
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

		CLog.d(TAG, "start ble service");
		return true;
	}

	private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic) {
			CLog.d(TAG, "onCharacteristicChanged()");
			String typeId = characteristic.getUuid().toString();
			
			CLog.d(TAG, "typeId="+typeId+" value ="+characteristic.getValue());
			if (typeId.equals(mCharacteristicUUID)) {
				int value = characteristic.getValue()[0];

				if (isHeartRateInUINT16(characteristic.getValue()[0])) {
//					Log.d(TAG, "HeartRateInUINT16");
					value = characteristic.getIntValue(
							BluetoothGattCharacteristic.FORMAT_UINT16, 1);
				} else {
//					Log.d(TAG, "HeartRateInUINT8");
					value = characteristic.getIntValue(
							BluetoothGattCharacteristic.FORMAT_UINT8, 1);
				}
				if (mConnectCallback != null) {
					mConnectCallback.getValue(value);
				}else{
					CLog.d(TAG, "no call back");
				}
			} else {
				byte[] bytes = characteristic.getValue();


				if (mConnectCallback != null) {
					mConnectCallback.getValues(bytes);
				}else{
					CLog.d(TAG, "no call back");
				}
			}
			
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			CLog.d(TAG, "onCharacteristicRead()");
			super.onCharacteristicRead(gatt, characteristic, status);
			if (mConnectCallback != null) {
				
				int value = characteristic.getValue()[0];
				Log.d("test1","------------onCharacteristicRead---------"+value);
				mConnectCallback.getValue(value);
			}else{
				CLog.d(TAG, "no call back");
			}
//			characteristic.getWriteType();
		}

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			CLog.d(TAG, "onCharacteristicWrite() result ?  " + (status == BluetoothGatt.GATT_SUCCESS));
			super.onCharacteristicWrite(gatt, characteristic, status);
		}

		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status,
				int newState) {
			if (status == BluetoothGatt.GATT_SUCCESS) {

				if (newState == BluetoothProfile.STATE_CONNECTED
						&& mBluetoothGatt != null) {
					isConnect = true;
					mBluetoothGatt = gatt;
					CLog.d(TAG, "onConnectionStateChange:connected");
					
					boolean flg = mBluetoothGatt.discoverServices();
					
					CLog.d(TAG, "disconver service:" + flg);
					if(null != mConnectCallback){
						
						mConnectCallback.connectState(mDevice, status, newState);
					}
				} else if (newState == BluetoothProfile.STATE_DISCONNECTED
						&& mBluetoothGatt != null) {
					CLog.d(TAG, "onConnectionStateChange:disconnected");
					if(null != mConnectCallback){
						
						mConnectCallback.connectState(mDevice, status, newState);
					}
				}
			} else {
				isConnect = false;
				if(null != mConnectCallback){
					
					mConnectCallback.connectState(mDevice, status, newState);
				}
			}
		}

		@Override
		public void onDescriptorRead(BluetoothGatt gatt,
				BluetoothGattDescriptor descriptor, int status) {
			CLog.d(TAG,
					"onDescriptorRead() status? :" + (status == BluetoothGatt.GATT_SUCCESS) );
			mHandler.removeMessages(NOTIFY);
			if(status == BluetoothGatt.GATT_SUCCESS){
				
				descriptor
				.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
				
				gatt.writeDescriptor(descriptor);
			}else{
				gatt.readDescriptor(descriptor);
			}
		}

		@Override
		public void onDescriptorWrite(BluetoothGatt gatt,
				BluetoothGattDescriptor descriptor, int status) {
			// TODO Auto-generated method stub
			CLog.d(TAG,
					"onDescriptorWrite() status ? "
							+ (status == BluetoothGatt.GATT_SUCCESS));
			
			super.onDescriptorWrite(gatt, descriptor, status);
			if (status == BluetoothGatt.GATT_SUCCESS) {
				if(null != mConnectCallback){
					
					mConnectCallback.onNotifySuccess();
				}
			} else {
				descriptor
						.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
				gatt.writeDescriptor(descriptor);
			}
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			// super.onServicesDiscovered(gatt, status);
			CLog.d(TAG, "onServicesDiscovered() ");
			if (status == BluetoothGatt.GATT_SUCCESS) {

				mHandler.removeMessages(NOTIFY);
				/*UUID serviceUUID = UUID.fromString(mClicentUUID);
				UUID characteristicUUID = UUID.fromString(mCharacteristicUUID);
			    enableNotify(gatt, serviceUUID,
						characteristicUUID, CCC);*/
			    UUID commandServiceUUID = UUID.fromString(mCommandClicentUUID);
				UUID commandCharacteristicUUID = UUID.fromString(mCommandCharacteristicUUID);
			    enableNotify(gatt, commandServiceUUID,
			    		commandCharacteristicUUID, CCC);
			} else {
				CLog.e(TAG, "err reson:" + status + " and try connect ble agin");

			}
		}
	};

	/**
	 * connect device
	 * 
	 * @param device
	 * @param autoconnect
	 */
	public void connect(BluetoothDevice device, boolean autoconnect) {
		CLog.d(TAG, "connect device");
		if(null == device) return;
		if (mBluetoothGatt == null) {
			mDevice = device;
			isForceDisconnect = true;
			mBluetoothGatt = device.connectGatt(mContext, autoconnect,
					mGattCallback);
		} else {
			mDevice = device;
			mBluetoothGatt.connect();
		}
	}

	/**
	 * close connect device
	 * 
	 */
	public void close() {
		isConnect = false;
		mHandler.removeMessages(NOTIFY);
		if (mBluetoothGatt != null) {
			isForceDisconnect = false;
			mBluetoothGatt.disconnect();
			mBluetoothGatt.close();
			mBluetoothGatt = null;
		}
	}

	public void disconnect() {
		isConnect = false; 
		mHandler.removeMessages(NOTIFY);
		if (mBluetoothGatt != null) {
			mBluetoothGatt.disconnect();
		}
	}

	/**
	 * 
	 * @return
	 */
	public boolean getConnectState() {
		return isForceDisconnect;
	}

	/**
	 * 
	 * @param serviceUUID
	 * @param characteristicUUID
	 */
	private void enableNotification(UUID serviceUUID, UUID characteristicUUID) {
		boolean result = false;
		CLog.i(TAG, "enableHRNotification ");
		BluetoothGattService gattService = mBluetoothGatt
				.getService(serviceUUID);
		if (gattService == null) {
			CLog.e(TAG, "HRP service not found! ");
			if (null != mConnectCallback) {
				mConnectCallback.connectState(mDevice, 1,
						BluetoothAdapter.STATE_DISCONNECTED);
			}
			return;
		}
		BluetoothGattCharacteristic mCharac = gattService
				.getCharacteristic(characteristicUUID);
		if (mCharac == null) {
			CLog.e(TAG, "HEART RATE MEASUREMENT charateristic not found!");
			if (null != mConnectCallback) {
				mConnectCallback.connectState(mDevice, 1,
						BluetoothAdapter.STATE_DISCONNECTED);
			}
			return;
		}
		BluetoothGattDescriptor mCcc = mCharac.getDescriptor(CCC);
		if (mCcc == null) {
			CLog.e(TAG,
					"CCC for HEART RATE MEASUREMENT charateristic not found!");
			if (null != mConnectCallback) {
				mConnectCallback.connectState(mDevice, 1,
						BluetoothAdapter.STATE_DISCONNECTED);
			}
			return;
		}
		result = mBluetoothGatt.readDescriptor(mCcc);
		if (result == false) {
			CLog.e(TAG, "readDescriptor() is failed");
			if (null != mConnectCallback) {
				mConnectCallback.connectState(mDevice, 1,
						BluetoothAdapter.STATE_DISCONNECTED);
			}
			return;
		}

	}

	/**
	 * 
	 * @param serviceUUID
	 * @param characteristicUUID
	 */
	private boolean enableNotify(BluetoothGatt gatt, UUID serviceUUID,
			UUID characteristicUUID, UUID descriptorUUID) {
		if (null == gatt) {
			return false;
		}

		CLog.d(TAG, "enableNotification mBluetoothGatt="+mBluetoothGatt+" serviceUUID="+serviceUUID);
		// List<BluetoothGattService> list = mBluetoothGatt.getServices();
		// for(BluetoothGattService service : list){
		// CLog.d(TAG, "service:" + service.getUuid());
		// }
		BluetoothGattService service = mBluetoothGatt.getService(serviceUUID);
		if (service == null) {
			CLog.e(TAG, " service not found!");
			if(null != mConnectCallback)
				mConnectCallback.connectState(mDevice, BluetoothGatt.GATT_FAILURE,
					BluetoothGatt.STATE_DISCONNECTED);
			return false;
		}
		CLog.d(TAG, "find service");
		BluetoothGattCharacteristic mHRMcharac = service
				.getCharacteristic(characteristicUUID);
		if (mHRMcharac == null) {
			CLog.e(TAG, " charateristic not found!");
			if(null != mConnectCallback)
			mConnectCallback.connectState(mDevice, BluetoothGatt.GATT_FAILURE,
					BluetoothGatt.STATE_DISCONNECTED);
			return false;
		}
		CLog.d(TAG, "find BluetoothGattCharacteristic");

		boolean isNotify = false;

		boolean result = gatt.setCharacteristicNotification(mHRMcharac, true);

		CLog.d(TAG, "setCharacteristicNotification result :" + result);

		BluetoothGattDescriptor descriptor = mHRMcharac
				.getDescriptor(descriptorUUID);
		if (null != descriptor) {
			mDescriptor = descriptor;
			mHandler.sendEmptyMessageDelayed(NOTIFY, 300);
//			gatt.readDescriptor(descriptor);
//			
//			descriptor
//					.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//			isNotify = gatt.writeDescriptor(descriptor);


		} else {
			if(null != mConnectCallback)
			mConnectCallback.connectState(mDevice, BluetoothGatt.GATT_FAILURE,
					BluetoothGatt.STATE_DISCONNECTED);
		}
		
		return isNotify;
	}

	/**
	 * 
	 * @param writeServiceUUID
	 * @param characteristicID
	 * @param bytes
	 */
	public void writeIasAlertLevel(String writeServiceUUID,
			String characteristicID, byte[] bytes) {
		String str = "write ";
        for(int i =0; i < bytes.length; i++){
       	 str +=   Integer.toHexString(bytes[i] & 0xff)+ ",";
        }
        
        CLog.i(TAG, str);
		if(null == mBluetoothGatt){
			return;
		}
		BluetoothGattService alertService = mBluetoothGatt.getService(UUID
				.fromString(writeServiceUUID));
		if (alertService == null) {
			// showMessage("Immediate Alert service not found!");
			return;
		}
		BluetoothGattCharacteristic mCharacter = alertService
				.getCharacteristic(UUID.fromString(characteristicID));
		if (mCharacter == null) {
			// showMessage("Immediate Alert Level charateristic not found!");
			return;
		}
		boolean status = false;
		int writeType = mCharacter.getWriteType();
		CLog.d(TAG, "Character writeType" + writeType);
		mCharacter.setValue(bytes);
		mCharacter.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
		status = mBluetoothGatt.writeCharacteristic(mCharacter);
		CLog.d(TAG, "write status:" + status);
	}
	
	private boolean isHeartRateInUINT16(byte flags) {
		if ((flags & 0x01) != 0)
			return true;
		return false;
	}
	
	public boolean readBatteryValue(){
		if (null == mBluetoothGatt) {
			return false;
		}
		UUID serviceUUID = UUID.fromString(mClicentUUID);
		// List<BluetoothGattService> list = mBluetoothGatt.getServices();
		// for(BluetoothGattService service : list){
		// CLog.d(TAG, "service:" + service.getUuid());
		// }
		
		BluetoothGattService service = mBluetoothGatt.getService(serviceUUID);
		if (service == null) {
			CLog.e(TAG, " service not found!");
			if(null != mConnectCallback)
				mConnectCallback.connectState(mDevice, BluetoothGatt.GATT_FAILURE,
					BluetoothGatt.STATE_DISCONNECTED);
			return false;
		}
		CLog.d(TAG, "find service");
		UUID characteristicUUID = UUID.fromString(mCharacteristicUUID);
		BluetoothGattCharacteristic mHRMcharac = service
				.getCharacteristic(characteristicUUID);
		if (mHRMcharac == null) {
			CLog.e(TAG, " charateristic not found!");
			if(null != mConnectCallback)
			mConnectCallback.connectState(mDevice, BluetoothGatt.GATT_FAILURE,
					BluetoothGatt.STATE_DISCONNECTED);
			return false;
		}
		//byte[] battery= mHRMcharac.getValue();
		//Log.d("test1","battery="+battery);
		return mBluetoothGatt.readCharacteristic(mHRMcharac);
		//enableNotifyBattery(true);
	}

	
	
	
	
}
