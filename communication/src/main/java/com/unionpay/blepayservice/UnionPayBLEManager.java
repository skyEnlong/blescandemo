package com.unionpay.blepayservice;

import com.communication.unionpay.ICodPayCallback;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
/**
 * @deprecated
 * @author workEnlong
 *
 */
public class UnionPayBLEManager {
	
	private Context mContext;
	public static String ACTION_UNION_PAY_CMD = "com.unionpay.blepayservice.UnionPayBLEManager.cmd";
	public static String ACRION_UNION_RESULT = "com.unionpay.blepayservice.UnionPayBLEManager.result";
	
	public String KEY_DATA = "data";
	public String KEY_CMD = "cmd";
 
	private ICodPayCallback mICodPayCallback;

	public UnionPayBLEManager(Context mContext){
		
		this.mContext = mContext;
		mContext.registerReceiver(mReceiver, new IntentFilter(ACRION_UNION_RESULT));
	}

	public void transApduDataToDevice(int cmd, byte[] data,
			ICodPayCallback iCodPayCallback) {
		// TODO Auto-generated method stub
		mICodPayCallback = iCodPayCallback;
		Intent intent = new Intent(ACTION_UNION_PAY_CMD);
		Bundle bundle  = new Bundle();
		bundle.putByteArray(KEY_DATA, data);
		bundle.putInt(KEY_CMD, cmd);
		intent.putExtras(bundle);
		mContext.sendBroadcast(intent);
	}

	
	public void close(){
		mContext.unregisterReceiver(mReceiver);
	}
	
	private BroadcastReceiver mReceiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if(null != intent && ACRION_UNION_RESULT.equals(intent.getAction())){
				if(null != mICodPayCallback){
					mICodPayCallback.onResult(intent.getExtras());
				}
			}
		}
		
	};

}
