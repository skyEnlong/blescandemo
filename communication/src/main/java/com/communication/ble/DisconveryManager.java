package com.communication.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;

import com.communication.bean.CodoonBluethoothDevice;
import com.communication.data.AccessoryConfig;
import com.communication.data.CLog;
import com.communication.data.TimeoutCheck;
import com.communication.data.TimeoutCheck.ITimeoutCallback;

public class DisconveryManager {
	private Context mContext;
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothAdapter.LeScanCallback mLeScanCallback;

	private OnSeartchCallback mOnSeartchCallback;

	private static final int SEARTCH_BY_CLASSIC = 0x1111;
	private static final String TAG = "DisconveryManager";

	private TimeoutCheck mTimeoutCheck;
	
	private int time_out = 15000;
	private boolean isScanBLEStart;
	private boolean isScanClassic;
	private Handler mSeartchChangeHander = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if (msg.what == SEARTCH_BY_CLASSIC) {
				startClassicSeartch();
			}
		}

	};

	public DisconveryManager(Context context, OnSeartchCallback onScanCallback) {
		mContext = context;
		this.mOnSeartchCallback = onScanCallback;
		mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

			@Override
			public void onLeScan(BluetoothDevice device, int rssi,
					byte[] scanRecord) {
				// TODO Auto-generated method stub
				boolean isInterrupt = false;
				
				if(null ==device ){
					return;
				}
				
				String deviceName = device.getName();
				if(null == deviceName){
					deviceName = parseDeviceName(device, scanRecord);
					
					if(null != deviceName){
						AccessoryConfig.setStringValue(mContext, device.getAddress(), deviceName);
					}

				}
				
				if(null == deviceName || deviceName.equals("unknown")) return ;
				CLog.i(TAG, "find device:" + deviceName);
				if (null != mOnSeartchCallback) {
//					isInterrupt = mOnSeartchCallback.onSeartch(device, scanRecord);
					CodoonBluethoothDevice mDevice = new CodoonBluethoothDevice();
					mDevice.device_name = deviceName;
					mDevice.device = device;
					isInterrupt =  mOnSeartchCallback.onSeartch(mDevice, scanRecord);
					
				}
				
				if (!isInterrupt) {
						if (null == deviceName) {
							isScanBLEStart = false;
							mBluetoothAdapter.stopLeScan(mLeScanCallback);
							seartchByClassic();
						}
				}
			}
		};

		final BluetoothManager bluetoothManager = (BluetoothManager) mContext
				.getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();
		
		mTimeoutCheck = new TimeoutCheck(new ITimeoutCallback() {
			
			@Override
			public void onReceivedFailed() {
				// TODO Auto-generated method stub
				timeOutAction();
			}
			
			@Override
			public void onReSend() {
				// TODO Auto-generated method stub
				timeOutAction();
			}
			
			@Override
			public void onReConnect(int tryConnectIndex) {
				// TODO Auto-generated method stub
				timeOutAction();
			}
			
			@Override
			public void onConnectFailed(int tryConnectIndex) {
				// TODO Auto-generated method stub
				timeOutAction();
			}
		});
		
		mTimeoutCheck.setTryConnectCounts(1);
		mTimeoutCheck.setIsConnection(false);
		mTimeoutCheck.setTimeout(time_out);   
	}

	/**
	 * startSearch
	 */
	public boolean startSearch() {
		if(mBluetoothAdapter.isEnabled() && !isScanBLEStart){
			
			mTimeoutCheck.startCheckTimeout();
			isScanBLEStart = mBluetoothAdapter.startLeScan(mLeScanCallback);
		}
		return isScanBLEStart;
	}

	/**
	 * stopSearch
	 */
	public void stopSearch() {
		
		mTimeoutCheck.stopCheckTimeout();
		
		try {

			mContext.unregisterReceiver(mReceiver);
		} catch (Exception e) {

		}
		
		try{
			if(isScanBLEStart) {
				isScanBLEStart = false;
				mBluetoothAdapter.stopLeScan(mLeScanCallback);
			}
		}catch(Exception e){
			e.printStackTrace();
		}

		try{
			if(isScanClassic){
				isScanClassic = false;
				mBluetoothAdapter.cancelDiscovery();
			}
		}catch(Exception e){
			
		}
		mSeartchChangeHander.removeMessages(SEARTCH_BY_CLASSIC);
		
		
	}

	private String parseDeviceName(BluetoothDevice device, byte[] infos){
		if(null == infos || infos.length < 4) return null; 
		int index = 3;
		int length = infos[index] & 0xff; // service(may many)

		index++;
		String name = null;
		try{
			int data_type = infos[index] & 0xff; // service type

			while (data_type != 9) { // find name

				index += length;
				
				if(index >= infos.length) return "unknown";
				
				length = infos[index] & 0xff; // name_lenght
				
				index++; // local_name
				
				if(index >= infos.length) return "unknown";
				
				data_type = infos[index] & 0xff;
			}

			index++; // name_begin

			 name = new String(infos, index, length - 1);
			CLog.i(TAG, "parse name:" + name);
		}catch(Exception  e){
			e.printStackTrace();
		}
		
		return name;
	}
	/**
	 */
	private void seartchByClassic() {
		mSeartchChangeHander.removeMessages(SEARTCH_BY_CLASSIC);
		mSeartchChangeHander.sendEmptyMessageDelayed(SEARTCH_BY_CLASSIC, 500);
	}

	/**
	 */
	private void startClassicSeartch() {
		// TODO Auto-generated method stub
		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		mContext.registerReceiver(mReceiver, filter);
		 isScanClassic = mBluetoothAdapter.startDiscovery();
		if (!isScanClassic) {
			timeOutAction();
		}
	}

	
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (null != mOnSeartchCallback) {
					CodoonBluethoothDevice mDevice = new CodoonBluethoothDevice();
					mDevice.device = device;
					mOnSeartchCallback.onSeartch(mDevice, null);
				}

			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
					.equals(action)) {
				
				timeOutAction();
			}
		}
	};
	
	
	public void timeOutAction(){
		boolean isinterrupt = false;
		if (null != mOnSeartchCallback) {
			isinterrupt = mOnSeartchCallback.onSeartchTimeOut();
		}
		
		if(!isinterrupt){
			
			stopSearch();
		}
	}

	public int getTime_out() {
		return time_out;
	}

	public void setTime_out(int time_out) {
		this.time_out = time_out;
	}


}
