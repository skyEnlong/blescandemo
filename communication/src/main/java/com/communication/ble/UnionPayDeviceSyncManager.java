package com.communication.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.communication.data.AccessoryConfig;
import com.communication.data.CLog;
import com.communication.data.DataUtil;
import com.communication.data.TimeoutCheck;
import com.communication.provider.KeyConstants;
import com.communication.unionpay.CodoonDataHelper;
import com.communication.unionpay.ICodPayCallback;
import com.communication.unionpay.ICodoonProtocol;
import com.communication.unionpay.ICodoonUnionDataInterfacce;
import com.communication.unionpay.IUnionPayResultCode;
import com.communication.unionpay.UnionBundleKey;
import com.communication.unionpay.UnionPayCommand;
import com.communication.unionpay.UnionPayCommandHelper;
import com.communication.unionpay.UnionPayResponseHelper;
import com.communication.util.CommonUtils;
import com.communication.util.MobileUtil;
import com.communication.util.UnionPayConstant;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by workEnlong on 2015/6/11.
 */
public class UnionPayDeviceSyncManager implements UnionPayCommand,
		IUnionPayResultCode, TimeoutCheck.ITimeoutCallback, IConnectCallback,
		UnionBundleKey, OnBleWriteCallBack {
	private   int RES_DATA_TIME = 10000;
	private UnionPayBleManager bleManager;

	protected TimeoutCheck mTimeoutCheck;

	private Context mContext;
	private boolean isSEATR;
	private boolean isResUnion;
	private boolean isStart;
	private boolean isReceiveSportData = true;
	protected List<ICodoonUnionDataInterfacce> mISyncDataCallbacks;

	private UnionPayCommandHelper curCommandHelper;
	private UnionPayResponseHelper responseHelper;
	private CodoonDataHelper mCodoonDataHelper;

	private int lastCmd;
	private byte[] lastData;
	private int curSsc = 0;
	private final int MAX_SSC = 4096;

	private Handler mHandler;

	private final int SEND_DATA = 0xccc1;
	private final int BLE_CONNECT = 0xccc2;
	private final int SEND_BUFFER_ORDER = 0xccc3;
	private final int SEND_AUTH = 0xccc4;
//	private boolean isStart;
	private int TIME_OUT = 8000;
	private final int EVERY_DATA_SEND_DELAY = 200;
	private ICodPayCallback codPayCallback;

	private BluetoothDevice device;

	private boolean isBusyWring;

	private List<Bundle> bufferOder;

	public static UnionPayDeviceSyncManager mInstance;

	public static String ACTION_UNION_PAY_CMD = "com.unionpay.blepayservice.UnionPayBLEManager.cmd";
	public static String ACRION_UNION_RESULT = "com.unionpay.blepayservice.UnionPayBLEManager.result";

	public String KEY_DATA = "data";
	public String KEY_CMD = "cmd";

	private int sendDataIndex;
	

	public static UnionPayDeviceSyncManager getInstance(Context mContext) {
		if (null == mInstance) {
			mInstance = new UnionPayDeviceSyncManager(
					mContext.getApplicationContext());
		}

		return mInstance;
	}

	public UnionPayDeviceSyncManager(Context mContext) {
		RES_DATA_TIME = MobileUtil.getTimeOutDelay();
		this.mContext = mContext;
		mISyncDataCallbacks = new ArrayList<ICodoonUnionDataInterfacce>();
		bleManager = new UnionPayBleManager(mContext);
		bleManager.setWriteCallback(this);

		bufferOder = new ArrayList<Bundle>();
		bleManager.setConnectCallBack(this);

		curCommandHelper = new UnionPayCommandHelper();
		responseHelper = new UnionPayResponseHelper(
				new UnionPayResponseHelper.OnResponseListener() {
					@Override
					public void onResponse(byte[] data, int ssc, int status) {

						mTimeoutCheck.stopCheckTimeout();

						curSsc = UnionPayResponseHelper.dealSsc(ssc);

						responseShow(data);

						if (status == BTC_IO_OK) {


							if (lastCmd == BTC_DATA) {
								// for codoon data parse
								if (isReceiveSportData) {

									mCodoonDataHelper.dealByteData(data);
								} else {
									CLog.i(TAG, "isReceiveSportData false");
								}

							} else if (lastCmd == BTC_TEST) {
								// onReSend();
							} else {// for union data

								dealUnionResponse(lastCmd, status, data);
							}

						} else {
							Log.e("union_pay", "err ssc send:" + curSsc
									+ " but get " + ssc + " status:" + status);
							responseUnionCallback(BTC_SSC_ERROR, null);
							stop();
						}
					}

					@Override
					public void onErr(int err_code) {

						responseUnionCallback(BTC_SSC_ERROR, null);

					}
					
					@Override
					public void onResend(){
						CLog.i(TAG, "send again");
						mTimeoutCheck.startNextCheckTime();
					}
				});

		mHandler = new Handler() {

			public void handleMessage(Message msg) {

				switch (msg.what) {
				case SEND_DATA:
					byte[] data = (byte[]) msg.obj;
					bleManager.writeDataToDevice(data);
					break;
				case BLE_CONNECT:
					Log.i(TAG, "BLE_CONNECT");
					bleManager.connect(device,
							MobileUtil.isUnionPayAutoConnect());
					break;
				case SEND_BUFFER_ORDER:
					if (null != bufferOder && bufferOder.size() > 0) {
						Bundle data_map = bufferOder.get(0);
						bufferOder.clear();
						int key = data_map.getInt(KEY_CMD, 0);
						byte[] value = data_map.getByteArray(KEY_DATA);
						writeUnionPayCmdToDevice(key, value);

					} else {
						Log.i(TAG, "no buffer oder");
					}
					break;
				case SEND_AUTH:
					bindDevice(device);
					break;
				}

				return;
			}
		};

		mTimeoutCheck = new TimeoutCheck(this);
		mTimeoutCheck.setTryConnectCounts(3);
		mTimeoutCheck.setTimeout(TIME_OUT);
		mCodoonDataHelper = new CodoonDataHelper(mContext,
				new ICodoonProtocol() {
					@Override
					public boolean writeToDevice(int[] data) {

						return writeDataToDevice(data);
					}
				}, mISyncDataCallbacks, mTimeoutCheck);
	}

	@Override
	public void onReConnect(int tryConnectIndex) {
		Log.e(TAG, "onReConnect");

		bleManager.close();
		if(BluetoothAdapter.getDefaultAdapter().isEnabled()){

			mHandler.removeMessages(BLE_CONNECT);
			mHandler.sendEmptyMessageDelayed(BLE_CONNECT, 800);
		}else{
			onConnectFailed(0);
		}
	}

	@Override
	public void onConnectFailed(int tryConnectIndex) {
		Log.e(TAG, "onConnectFailed");
		isBusyWring = false;
		isSEATR = false;

		if (lastCmd != BTC_DATA) {
			responseUnionCallback(BTC_IO_TIMEOUT, null);
		}

		try {
			for (int i = 0; i < mISyncDataCallbacks.size(); i++) {
				mISyncDataCallbacks.get(i).onTimeOut();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		close();


	}

	@Override
	public void onReSend() {
		// sendUnionDataToDevice();
		if(!isStart) return;

		Log.e("union_pay", "onReSend");

		if(BluetoothAdapter.getDefaultAdapter().isEnabled()){
			isBusyWring = true;
			mHandler.removeMessages(SEND_DATA);
			responseHelper.clear();
			curSsc = UnionPayResponseHelper.dealSsc(curSsc);
			curCommandHelper.setCommand(curSsc, lastCmd, lastData);

//		 int frameCount = curCommandHelper.getFrameCount();
//		 for (int i = 0; i < frameCount; i++) {
//		 Message msg = new Message();
//		 msg.what = SEND_DATA;
//		 msg.obj = curCommandHelper.getFrameByIndex(i);
//		 mHandler.sendMessage(msg);
//
//		 }
//		 isBusyWring = false;


			sendDataIndex = 0;
			Message msg = new Message();
			msg.what = SEND_DATA;
			msg.obj = curCommandHelper.getFrameByIndex(sendDataIndex++);
			mHandler.sendMessage(msg);

		}else{
			onReceivedFailed();
		}

	}

	@Override
	public void onReceivedFailed() {
		isBusyWring = false;
		Log.e(TAG, "onReceivedFailed");
		if (lastCmd != BTC_DATA) {
			responseUnionCallback(BTC_IO_TIMEOUT, null);
		}
		try {

			for (int i = 0; i < mISyncDataCallbacks.size(); i++) {
				mISyncDataCallbacks.get(i).onTimeOut();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		close();
	}

	/**
	 * send cmd to device directly
	 * 
	 * @param cmd
	 * @param byte_data
	 * @return
	 */
	public boolean writeCmdAndDataToDevice(int cmd, byte[] byte_data) {
		if(cmd == BTC_DATA){
			isReceiveSportData = true;
		}
		if (!isBusyWring) {
			bufferOder.clear();
			codPayCallback = null;
			if (bleManager.isConnect) {
				lastCmd = cmd;
				lastData = byte_data;
				curCommandHelper.setCommand(curSsc, cmd, byte_data);
				sendUnionDataToDevice();
			} else if (cmd != BTC_AUTH) {
				Log.i(TAG, "not connect, connect first");
				Bundle data_map = new Bundle();
				data_map.putInt(KEY_CMD, cmd);
				data_map.putByteArray(KEY_DATA,  byte_data);
				bufferOder.add(data_map);
				startDevice(device);
			}
		} else {
			Log.e(TAG, "sync busy now");
		}
		return !isBusyWring;
	}

	/**
	 * codoon write command data
	 * 
	 * @param data
	 * @return
	 */
	public boolean writeDataToDevice(int[] data) {
		isResUnion = false;
		codPayCallback = null;

		return writeCmdAndDataToDevice(BTC_DATA, CommonUtils.intToByte(data));
	}

	public boolean writeDataToDevice(byte[] data) {
		isResUnion = false;
		codPayCallback = null;
		return writeCmdAndDataToDevice(BTC_DATA, data);
	}
	/**
	 * 透传银联命令
	 * 
	 * @param data
	 * @param callback
	 * @return
	 */
	public synchronized boolean transApduDataToDevice(int cmd, byte[] data,
			ICodPayCallback callback) {
		isResUnion = true;
		codPayCallback = callback;
		writeUnionPayCmdToDevice(cmd, data);

		return false;
	}

	/**
	 * for union pay cmd
	 * 
	 * @param data
	 * @return
	 */
	private synchronized boolean writeUnionPayCmdToDevice(int cmd, byte[] data) {
		if (!isBusyWring) {
			bufferOder.clear();

			if (bleManager.isConnect) {
				lastCmd = cmd;
				lastData = data;
				curCommandHelper.setCommand(curSsc, cmd, data);
				sendUnionDataToDevice();
			} else if (cmd != BTC_AUTH) {
				CLog.i(TAG, "not connect, connect first");
				Bundle data_map = new Bundle();
				data_map.putInt(KEY_CMD, cmd);
				data_map.putByteArray(KEY_DATA,  data);
				bufferOder.add(data_map);
				startDevice(device);
			}

		} else {
			Log.e(TAG, "sync busy now");
		}

		return !isBusyWring;
	}

	private synchronized boolean sendUnionDataToDevice() {
		isBusyWring = true;
		responseHelper.clear();
		System.gc();


		mTimeoutCheck.stopCheckTimeout();
		mTimeoutCheck.setIsConnection(false);
		mTimeoutCheck.setTryConnectCounts(3);
		mTimeoutCheck.setTimeout(RES_DATA_TIME);
		mTimeoutCheck.startCheckTimeout();

		sendDataIndex = 0;
		bleManager.writeDataToDevice(curCommandHelper.getFrameByIndex(sendDataIndex++));
//		Message msg = new Message();
//		msg.what = SEND_DATA;
//		msg.obj = curCommandHelper.getFrameByIndex(sendDataIndex++);
//		mHandler.sendMessage(msg); // careful

//		int frameCount = curCommandHelper.getFrameCount();
//		 for (int i = 0; i < frameCount; i++) {
//		 Message msg = new Message();
//		 msg.what = SEND_DATA;
//		 msg.obj = curCommandHelper.getFrameByIndex(i);
//		 mHandler.sendMessage(msg); //
// 		 }
//		 isBusyWring = false;
		return true;
	}

	public void startDevice(BluetoothDevice device) {
		if (null == device) {
			Log.e(TAG, "device null");
			return;
		}
		isSEATR = false;
		isStart = true;
		isReceiveSportData = true;
		isBusyWring = false;
		this.device = device;
		mTimeoutCheck.setIsConnection(true);
		mTimeoutCheck.startCheckTimeout();
		mCodoonDataHelper.setDeviceAdress(device.getAddress());
		Log.i(TAG, "begin connect");
		// bleManager.connect(device, MobileUtil.isAutoConnect());
		mHandler.removeMessages(BLE_CONNECT);
		mHandler.sendEmptyMessageDelayed(BLE_CONNECT, 100);

		// try{
		// mContext.registerReceiver(mReceiver, new
		// IntentFilter(ACTION_UNION_PAY_CMD));
		//
		// }catch(Exception e){
		//
		// }

	}

	public void bindDevice(BluetoothDevice device) {
		this.device = device;
		Log.i(TAG, "bind device");
		isStart = true;
		byte[] data = new byte[6];
		Random random = new Random();
		for (int i = 0; i < data.length; i++) {
			data[i] = (byte) (random.nextInt(10) & 0xff);
		}
		if (bleManager.isConnect) {
			lastCmd = BTC_AUTH;
			lastData = UnionPayCommandHelper.getBindOrderData(mContext, data);
			curCommandHelper.setCommand(curSsc, BTC_AUTH, lastData);
			sendUnionDataToDevice();
		} else {
			startDevice(device);
		}

	}

	public void unBindDevice(BluetoothDevice device) {	
//		if (BluetoothAdapter.getDefaultAdapter().isEnabled()) {
//
//			if (bleManager.isConnect) {
//				writeUnionPayCmdToDevice(BTC_UNBIND,
//						curCommandHelper.getUnBindOrderData(new byte[6]));
//
//			} else {
//				Map<Integer, byte[]> data_map = new HashMap<Integer, byte[]>();
//				data_map.put(BTC_UNBIND,
//						curCommandHelper.getUnBindOrderData(new byte[6]));
//				bufferOder.add(data_map);
//				startDevice(device);
//			}
//		}
	}

	public void stop() {
		
		bufferOder.clear();
		isBusyWring = false;
		
		isReceiveSportData = false;
		sendDataIndex = 0;
		responseHelper.clear();
		mHandler.removeMessages(SEND_DATA);
		mHandler.removeMessages(BLE_CONNECT);
		mHandler.removeMessages(SEND_AUTH);
		mHandler.removeMessages(SEND_BUFFER_ORDER);
		if (null != mTimeoutCheck) {
			mTimeoutCheck.stopCheckTimeout();
		}
	}

	public void close() {
		CLog.i(TAG, "ble close");
		stop();
		isStart = false;
		lastData = null;
		isSEATR = false;
		isResUnion = false;
		bleManager.close();
		mISyncDataCallbacks.clear();
		bufferOder.clear();
		curSsc = 0;
		System.gc();
		if(null != codPayCallback){
//			CLog.i(TAG, "tell tsm close, so give null result");
//			codPayCallback.onResult(new Bundle());
			codPayCallback = null;
		}
		 
	}

	public void registerSyncDataCallback(ICodoonUnionDataInterfacce callback) {
		if (null != callback && null != mISyncDataCallbacks
				&& !mISyncDataCallbacks.contains(callback)) {
			mISyncDataCallbacks.add(callback);
		}
	}

	public void unRegisterSyncDataCallback(ICodoonUnionDataInterfacce callback) {
		mISyncDataCallbacks.remove(callback);
	}

	/**
	 * deal with response data and do last
	 * 
	 * @param lastCmd
	 * @param data
	 */
	private void dealUnionResponse(int lastCmd, int status, byte[] data) {
		switch (lastCmd) {
		case BTC_AUTH:
			CLog.i(TAG, "has BTC_AUTH");

			mHandler.removeMessages(SEND_BUFFER_ORDER);
			mHandler.sendEmptyMessage(SEND_BUFFER_ORDER);
			try {
				for (int i = 0; i < mISyncDataCallbacks.size(); i++) {
					mISyncDataCallbacks.get(i)
							.onDeviceBind(device.getAddress());
				}

				Log.i(TAG,
						"mISyncDataCallback size:"
								+ mISyncDataCallbacks.size());
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case BTC_CONNECT:
			CLog.i(TAG, "receive BTC_CONNECT, BIGAN TO ATR");

			if (null != data && data.length == 2) {

				curSsc = ((data[0] & 0xff) << 8) + (data[1] & 0xff);
				curSsc = UnionPayResponseHelper.dealSsc(curSsc);
				writeUnionPayCmdToDevice(BTC_ATR, null);
			} else {
				Log.e(TAG, "err get ssc");
				responseUnionCallback(BTC_SSC_ERROR, null);
			}
			break;
		case BTC_ATR:

			isSEATR = true;
			CLog.i(TAG, "receive BTC_ATR res");
			updateSESyncTime();
			stop();
			responseUnionCallback(BTC_IO_OK, data);
			break;

		case BTC_APDU:
			CLog.i(TAG, "receive BTC_APDU res");
			// writeUnionPayCmdToDevice(BTC_DISCONNECT, null);
			stop();
			updateSESyncTime();
			responseUnionCallback(status, data);
			break;
		case BTC_DISCONNECT:
			CLog.i(TAG, "has BTC_DISCONNECT");
			setSEATR(false);
			stop();
			responseUnionCallback(status, data);
			break;
		case BTC_UNBIND:
			Log.i(TAG, "has BTC_UNBIND");
			stop();
			try {
				for (int i = 0; i < mISyncDataCallbacks.size(); i++) {
					mISyncDataCallbacks.get(i).onDeviceUnBind(
							device.getAddress());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		case BTC_INFO:
			stop();
			responseUnionCallback(BTC_IO_OK, data);

			if (null != data && data.length > 12) {
				int offset = 2;
				String mainVersion = CommonUtils.getHexString(data[offset]);

				String secondVersion = CommonUtils
						.getHexString(data[offset + 1]);

				String version = mainVersion + "." + secondVersion;

				offset += 2;

				String manuCode = CommonUtils.getHexString(data[offset]);

				offset += 1;

				String product_num = CommonUtils.getHexString(data[offset])
						+ CommonUtils.getHexString(data[offset + 1])
						+ CommonUtils.getHexString(data[offset + 2]);

				offset += 3;
				String liushui_num = CommonUtils.getHexString(data[offset])
						+ CommonUtils.getHexString(data[offset + 1])
						+ CommonUtils.getHexString(data[offset + 2])
						+ CommonUtils.getHexString(data[offset + 3]);

				String id = manuCode + "-" + product_num + "-" + liushui_num;
				try {

					for (int i = 0; i < mISyncDataCallbacks.size(); i++) {
						mISyncDataCallbacks.get(i).onGetVersionAndId(version,
								id);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
			break;

		case BTC_IDLE:
			CLog.d(TAG, "BTC_IDLE OK");
			stop();
			break;
		}
	}

	/**
	 * 更新SE的最后使用时间
	 */
	private void updateSESyncTime() {
		AccessoryConfig.setLongValue(mContext, KeyConstants.KEY_LAST_ATR,
				System.currentTimeMillis());

	}

	/**
	 * 处理银联端的回调
	 * 
	 * @param status
	 * @param data
	 */
	private void responseUnionCallback(int status, byte[] data) {
		CLog.i(TAG, "isResUnion:" + isResUnion);
		if (isResUnion) {
			Bundle mBundle = new Bundle();
			if (status == BTC_IO_OK) {
				mBundle.putInt(KEY_RESULT_CODE,
						UnionPayConstant.ResultCodeForUnionPay.SUCCESS);

				if (null != data) {

					mBundle.putByteArray(
							UnionPayConstant.UnionBundleKey.KEY_RESULT, data);
				}

			} else {
				

				int err_code = UnionPayConstant.ResultCodeForUnionPay.ERR_BLE;
				switch (status) {
				case BTC_ILLEGAL_CMD:
					Log.e(TAG, "response code BTC_ILLEGAL_CMD");
					err_code = UnionPayConstant.ResultCodeForUnionPay.ERR_SE;
					break;
				case BTC_ILLEGAL_STATUS:
					Log.e(TAG, "response code BTC_ILLEGAL_STATUS");
					err_code = UnionPayConstant.ResultCodeForUnionPay.ERR_SE;
					break;
				case BTC_SSC_ERROR:
					Log.e(TAG, "response code BTC_SSC_ERROR");
					err_code = UnionPayConstant.ResultCodeForUnionPay.ERR_SE;
					break;
				default:
					Log.e(TAG, "response errcode:" + status);

					err_code = UnionPayConstant.ResultCodeForUnionPay.ERR_BLE;
					break;
				}

				mBundle.putInt(KEY_RESULT_CODE, err_code);

			}


			if (null != codPayCallback) {
				codPayCallback.onResult(mBundle);
			}
		}
	}

	@Override
	public void connectState(BluetoothDevice device, int status, int newState) {
		if (status == BluetoothGatt.GATT_SUCCESS) {

			if (newState == BluetoothGatt.STATE_CONNECTED) {
				mTimeoutCheck.stopCheckTimeout();

			}else{
				isSEATR = false;

			}
		} else {
//			mTimeoutCheck.startNextCheckTime();
			isSEATR = false;

		}
	}

	@Override
	public void getValue(int value) {

	}

	@Override
	public void getValues(byte[] data) {
		if(isStart){
			responseHelper.dealResponseFrameData(data);
		}else{
			CLog.e(TAG, "not start");
		}
	}

	@Override
	public void onNotifySuccess() {
		// begin to send order
		 if(!isStart) {
			 CLog.e(TAG, "not start");
			 return;
		 }
		try {

			for (int i = 0; i < mISyncDataCallbacks.size(); i++) {
				mISyncDataCallbacks.get(i).onConnectSuccessed();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		mHandler.removeMessages(SEND_AUTH);
		mHandler.sendEmptyMessageDelayed(SEND_AUTH, 1200);
	}

	public boolean isSEATR() {
		 

		return isSEATR && bleManager.isConnect;
	}

	public void setSEATR(boolean isSEATR) {
		this.isSEATR = isSEATR;
	}

	public boolean isConnect() {
		return bleManager.isConnect;
	}

	public void beforeUnionCmd() {
		stop();
		mISyncDataCallbacks.clear();
	}

	@Override
	public void onWriteSuccess() {
		// TODO Auto-generated method stub
		 
		CLog.i(TAG, "write next ");
		
		try{
			if (isBusyWring) {
				if (sendDataIndex < curCommandHelper.getFrameCount()) {

					Message msg = mHandler.obtainMessage();
					msg.what = SEND_DATA;
					msg.obj = curCommandHelper.getFrameByIndex(sendDataIndex++);
					mHandler.sendMessageDelayed(msg, 5);

				} else {

					sendDataIndex = 0;
					isBusyWring = false;

				}

			} else {
				CLog.i(TAG, "not write state");

			}	
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}

	@Override
	public void onWriteFailed() {
		// TODO Auto-generated method stub
		CLog.e(TAG, "onWriteFailed, throw failed result");
		onReceivedFailed();
		// if(isBusyWring){
		// Message msg = new Message();
		// msg.what = SEND_DATA;
		// msg.obj = curCommandHelper.getFrameByIndex(sendDataIndex-1);
		// mHandler.removeMessages(SEND_DATA);
		// mHandler.sendMessageDelayed(msg, EVERY_DATA_SEND_DELAY); // careful
		// }
	}

	private void responseShow(byte[] data){
		for (ICodoonUnionDataInterfacce callback : mISyncDataCallbacks){
			callback.onResponse(DataUtil.DebugPrint(data));
		}
	}
}
