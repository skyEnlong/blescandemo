package com.unionpay.blepayservice;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import com.communication.ble.UnionPayDeviceSyncManager;
import com.communication.data.CLog;
import com.communication.unionpay.ICodPayCallback;
import com.communication.unionpay.ICodoonUnionDataInterfacce;
import com.communication.unionpay.UnionBundleKey;
import com.communication.unionpay.UnionPayCommand;
import com.communication.unionpay.UnionPayCommandHelper;
import com.communication.unionpay.UnionPayResponseHelper;
import com.communication.util.CommonUtils;
import com.communication.util.MobileUtil;

/**
 * Created by workEnlong on 2015/6/11.
 */
public class PayService extends Service implements UnionPayCommand,
		UnionBundleKey {
	private final String TAG = "union_pay_service";

	private UnionPayService mBinder;
	private UnionPayDeviceSyncManager mSyncManager;

	boolean isResponse;
	Object lock;

	@Override
	public void onCreate() {
		super.onCreate();
		CLog.i(TAG, "onCreate()");
		lock = new Object();
		if (MobileUtil.isSupportBLE(this)) {

//			mSyncManager = UnionPayDeviceSyncManager.getInstance(this);
			mSyncManager = new UnionPayDeviceSyncManager(this);
		}

		mBinder = new UnionPayService();

	}

	private void notifyReturn() {
		synchronized (lock) {
			isResponse = true;
			lock.notify();
		}
	}

	private void waitBLE() {
		synchronized (lock) {
			while (!isResponse) {

				try {

					lock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

	}

	@Override
	public IBinder onBind(Intent intent) {
		CLog.i(TAG, "onBind()");
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		CLog.i(TAG, "onUnbind");
		return super.onUnbind(intent);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		CLog.i(TAG, "codPayservice onDestroy");
	}

	
	
	public class UnionPayService extends IBLEService.Stub {

		String btc_info;
		Channel curChannel = null;
		int result_code = -1;
		String	result = "0100";
		String cplcInfo;
		
		@Override
		public int init() throws RemoteException {
			CLog.i(TAG, "init");
			if("main".equals(Thread.currentThread().getName())){
				CLog.e(TAG, "init in the mainThread");
				return 0;
			}
			isResponse = false;
			result_code = -1;
			mSyncManager.beforeUnionCmd();
			mSyncManager.transApduDataToDevice(BTC_CONNECT, null,
					new ICodPayCallback() {
						@Override
						public void onResult(Bundle bundle) {
							result_code = bundle.getInt(KEY_RESULT_CODE, -1);

							notifyReturn();

						}
					});

			waitBLE();

			throwExceptionByResCode(result_code);
			CLog.i(TAG, "init over");

			return result_code;
		}

		@Override
		public Channel openLogicChannel(byte[] bytes) throws RemoteException {
			CLog.i(TAG, "openLogicChannel");

			isResponse = false;
			curChannel = null;
				mSyncManager.transApduDataToDevice(BTC_APDU, UnionPayCommandHelper.getOpenLogicCmd(bytes),
						new ICodPayCallback() {
							@Override
							public void onResult(Bundle bundle) {
								byte[] data = bundle.getByteArray(KEY_RESULT);
								String str = "";
								if(null != data && data.length > 0){
									str = CommonUtils.convertByteToHexString(bundle.getByteArray(KEY_RESULT));
								}
								CLog.i(TAG, "channel res:" + str);
								
								result_code = bundle.getInt(KEY_RESULT_CODE, -1);
								
								if(result_code == 0){
									
									curChannel = new Channel(
											0,
											str,
											new BLETransService());
								}
								notifyReturn();

							}

						});

				waitBLE();
				throwExceptionByResCode(result_code);

		 
			return curChannel;
		}


		@Override
		public String bleSEStatus() throws RemoteException {
			BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
			if (adapter.isEnabled()) {
				isResponse = false;
				result_code = -1;
				result = "0100";

				if(mSyncManager.isSEATR()){
					result = "0101";

				}
				
				 
			} else {
				result = "0000";

			}

			CLog.i(TAG, "get bleSEStatus " + result);

 			return result;
		}

		@Override
		public String getBTCInfo() throws RemoteException {
			CLog.i(TAG, "get btc info");
			isResponse = false;
			btc_info = "";
			mSyncManager.transApduDataToDevice(BTC_INFO, null,
					new ICodPayCallback() {
						@Override
						public void onResult(Bundle bundle) {
							result_code = bundle.getInt(KEY_RESULT_CODE, -1);
							byte[] data = bundle.getByteArray(KEY_RESULT);
							if(null != data){
								btc_info = CommonUtils.convertByteToHexString(data);
							}
							
							notifyReturn();
						}
					});

			waitBLE();

			throwExceptionByResCode(result_code);

			return btc_info;
		}

		@Override
		public String getCPLCInfo() throws RemoteException {
			isResponse = false;
//			"00C00000";							
			CLog.i(TAG, "get cplc");

			final byte[] cmd = UnionPayCommandHelper.getCPLCCmd();
			mSyncManager.transApduDataToDevice(BTC_APDU, cmd,
					new ICodPayCallback() {
						@Override
						public void onResult(Bundle bundle) {
							
						
							result_code = bundle.getInt(KEY_RESULT_CODE, -1);
							byte[] data = bundle.getByteArray(KEY_RESULT);
							if(null != data && data.length > 1){
								
								if(data.length == 2){
									/**
									 * （1）如果eSE返回61XX，则发出00C00000XX指令
										（2）如果eSE返回6CXX，则将原APDU指令中的Le替换成XX重发

									 */
										if(data[0] == (0x61 & 0xff)){
											byte[] cmd2 = UnionPayCommandHelper.getCPLCCmd2(data[1]);
											mSyncManager.transApduDataToDevice(BTC_APDU, cmd2, this);
										}else if(data[0] == (0x6c & 0xff)){
											cmd[cmd.length -1] = (byte)(data[1] & 0xff);
											mSyncManager.transApduDataToDevice(BTC_APDU, cmd, this);

										}
								}else{
									
									cplcInfo = CommonUtils.convertByteToHexString(UnionPayResponseHelper.parseCPLCInfo(data));
									CLog.i(TAG, "get cplc result");
									Intent intent = new Intent(ACTION_GET_CPLC);
									intent.putExtra(KEY_CPLC, cplcInfo);
									sendBroadcast(intent);
								}
								
							}
							notifyReturn();
						}

						
					});

			waitBLE();

			throwExceptionByResCode(result_code);

			return cplcInfo;
		}
		
		
		/////////////////for sport///////////////
		public void writeDataToDevice(int[] data){
			mSyncManager.writeDataToDevice(data);
		}

		public void writeDataToDevice(byte[] data){
			mSyncManager.writeDataToDevice(data);
		}
		
		public void writeCmdAndDataToDevice(int cmd , byte[] data){
			mSyncManager.writeCmdAndDataToDevice(cmd, data);
		}
		
		public void registerSyncDataCallback(ICodoonUnionDataInterfacce callback){
			mSyncManager.registerSyncDataCallback(callback);
		}
		
		public void unRegisterSyncDataCallback(ICodoonUnionDataInterfacce callback){
			mSyncManager.unRegisterSyncDataCallback(callback);
		}
		
		public void stop(){
			mSyncManager.stop();
		}
		
		public void close(){
			mSyncManager.close();
		}
		
		
		public boolean isConnect(){
			return mSyncManager.isConnect();
		}
		
		public void startDevice(BluetoothDevice device){
			mSyncManager.startDevice(device);
		}
		
		public boolean isSEATR(){
			return mSyncManager.isSEATR();
		}
	}
	
	
	class BLETransService extends IBLETransCMDService.Stub {
		byte[] data_res;
		int resCode = -1;

		@Override
		public int close() throws RemoteException {
//			mSyncManager.close();
			return 0;
		}

		@Override
		public boolean isClosed() throws RemoteException {
			return false;
		}

		@Override
		public byte[] transmit(byte[] bytes) throws RemoteException {
			data_res = null;
			isResponse = false;
			resCode = -1;
			CLog.i(TAG, "begin trans data byte");
			mSyncManager.transApduDataToDevice(BTC_APDU, bytes,
					new ICodPayCallback() {
						@Override
						public void onResult(Bundle bundle) {
							data_res = bundle.getByteArray(KEY_RESULT);
							resCode = bundle.getInt(KEY_RESULT_CODE, -1);
							notifyReturn();
						}
					});

			waitBLE();
			throwExceptionByResCode(resCode);

 			return data_res;
		}

	};
	
	public void throwExceptionByResCode(int resccode) throws RemoteException{
		if (resccode != 0) {
			CLog.e(TAG, "throwExceptionByResCode: " + resccode );
//			throw new RemoteException();
		} 
	}
}
