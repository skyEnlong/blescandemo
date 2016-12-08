package com.unionpay.blepayservice;

import java.util.List;

import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.communication.unionpay.ICodoonUnionDataInterfacce;
import com.unionpay.blepayservice.PayService.UnionPayService;

public class CodPayConnector {
	private UnionPayService mSyncManager;
	private ServiceConnection mServiceConnection;

	public interface OnServiceConnectCallback {
		public void onServiceConnect();

		public void onServiceDisconnect();
	}

	private List<OnServiceConnectCallback> mOnServiceConnectCallback;
	private Context mContext;

	private static CodPayConnector mCodPayConnector;
	
	public static CodPayConnector getInstance(Context mContext){
		if(null == mCodPayConnector){
			mCodPayConnector = new CodPayConnector(mContext);
		}
		mCodPayConnector.mContext = mContext;
		return mCodPayConnector;
	}
	private CodPayConnector(Context mContext) {
		this.mContext = mContext;
	}

	public void addConnectListener(OnServiceConnectCallback mcallback) {
		if (null != mcallback) {

			mOnServiceConnectCallback.add(mcallback);
		}
	}

	public void unBindService() {
		
		try{
			if(null != mSyncManager)
 			mContext.unbindService(mServiceConnection);
		}catch(Exception e){
			e.printStackTrace();
		}
		mCodPayConnector = null;
	}

	public void bindService() {
		if(null != mSyncManager) return; // has bind
		mServiceConnection = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
 				// binder = IBLEService.Stub.asInterface(service);
				mSyncManager = (UnionPayService) service;
				
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
 
				mSyncManager = null;
				if(null != mOnServiceConnectCallback)
				mOnServiceConnectCallback.clear();
			}
		};

		Intent intent = new Intent("com.unionpay.blepayservice.UPBLEPayService");
		ComponentName componet = new ComponentName(mContext.getPackageName(),
				"com.unionpay.blepayservice.PayService");
		intent.setComponent(componet);
		mContext.bindService(intent, mServiceConnection,
				Context.BIND_AUTO_CREATE);
	}
	
	public void writeDataToDevice(int[] data){
		if(null == mSyncManager) return;
		mSyncManager.writeDataToDevice(data);
	}
	
	public void writeCmdAndDataToDevice(int cmd, byte[] data){
		if(null == mSyncManager) return;
		mSyncManager.writeCmdAndDataToDevice(cmd, data);
	}
	
	
	public void registerSyncDataCallback(ICodoonUnionDataInterfacce callback){
		if(null == mSyncManager) return;
		mSyncManager.registerSyncDataCallback(callback);
	}
	
	public void unRegisterSyncDataCallback(ICodoonUnionDataInterfacce callback){
		if(null == mSyncManager) return;
		mSyncManager.unRegisterSyncDataCallback(callback);
	}
	
	public void stop(){
		if(null == mSyncManager) return;
		mSyncManager.stop();
	}
	
	public void close(){
		if(null == mSyncManager) return;
		mSyncManager.close();
	}
	
	
	public boolean isConnect(){
		if(null == mSyncManager) return false;
		return mSyncManager.isConnect();
	}
	
	public boolean isATR(){
		if(null == mSyncManager) return false;
		return mSyncManager.isSEATR();
	}
	
	public void startDevice(BluetoothDevice device){
		if(null == mSyncManager) return;
		mSyncManager.startDevice(device);
	}

}
