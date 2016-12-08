package com.communication.fsk;

import java.util.ArrayList;

import android.util.Log;

public class ReceiveManager {

	private String TAG=ReceiveManager.class.toString();
	private int index = 0;
	private boolean isStart = false;
	private int length = 0;
	private int ID = 0;
	private ArrayList<Integer> list = new ArrayList<Integer>();

	private Recorder mRecorder;
	
	private FSKDeviceDataManager mSyncDeviceDataManager;
	
	public ReceiveManager(FSKDeviceDataManager syncDeviceDataManager) {
		mSyncDeviceDataManager=syncDeviceDataManager;
		mRecorder=new Recorder(mCallback);
	}
	
	/**
	 * 
	 */
	public void start(){
		mRecorder.setRecording(true);
		mRecorder.start();
	}
	
	/**
	 * 
	 */
	public void stop(){
		mRecorder.stop();
	}
	
	/**
	 * 
	 * @param result
	 */
	private void receive(int result) {
		if (result == 0xAA) {
			isStart = true;
		} else if (result == -1 || result == -2) { //越界
			if (index != 0) {
				list.add(result);
			}
			if (list.size() > 2) {
				checkData(ID, list);
			}

			ID = 0;
			index = 0;
			length = 0;
			isStart = false;
			list = new ArrayList<Integer>();
		}

		if (isStart == true) {
			index++;
			if (index == 2) {
				ID = result;
			} else if (index == 3) {
				length = result;
			}

			// end
			if (index - length == 4) {
				list.add(result);
				// read data over ,check the data valid whether or not
				checkData(ID, list);

				isStart = false;
				ID = 0;
				index = 0;
				length = 0;
				list = new ArrayList<Integer>();
			} else {
				list.add(result);
			}

		}
	}
	
	  /**
     * 
     * @param msgId
     * @param result
     * @param state
     */
    private void checkData(int msgId, ArrayList<Integer> list) {
    	String value="";
    	for(int i:list){
    		value+=" ,0x"+Integer.toHexString(i);
    	}
    	Log.d(TAG, value);
        boolean flg = isValid(list);
        if (flg ) {
			mSyncDeviceDataManager.analysis(list);
		} else {
			Log.d(TAG, "value is invalid");
			mSyncDeviceDataManager.redoLastAction();
		}

	}

	/**
	 * 
	 * @param list
	 * @return
	 */
	private boolean isValid(ArrayList<Integer> list) {
		int length = list.size() - 1;
		if (length > 2) {
			if (length - 3 != list.get(2)) {
				return false;
			}
		}
		int count = 0;
		for (int i = 0; i < length; i++) {
			count += list.get(i);
		}
		if ((count & 0xFF) == list.get(length)) {
			return true;
		} else {
			return false;
		}
	}

	private IFSKNumberCallback mCallback = new IFSKNumberCallback() {

		@Override
		public void getNumber(int number) {
			 receive(number);
			
		}
	};
}
