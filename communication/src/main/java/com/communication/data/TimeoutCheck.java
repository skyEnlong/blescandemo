package com.communication.data;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class TimeoutCheck {
    private int TIMEOUT = 10000;

    private boolean isConnecting=false;

    private int mTryConnectCounts = 0;

    private ITimeoutCallback mCallback;

    private int mTryConnectIndex = 0;

    public TimeoutCheck(ITimeoutCallback callback) {
        mCallback = callback;
        mTryConnectIndex = 0;
    }

    /**
     * 
     * @param timeout
     **/
    public void setTimeout(int timeout) {
        TIMEOUT = timeout;
    }

    /**
     * 
     * @param isConnecting
     **/
    public void setIsConnection(boolean isConnecting) {
        this.isConnecting = isConnecting;
    }

    /**
     * 
     * @param counts
     **/
    public void setTryConnectCounts(int counts) {
        mTryConnectCounts = counts;
    }

    /**
     *
     **/
    public void startCheckTimeout() {
        mHandler.removeCallbacks(mRunnable);
        mTryConnectIndex=0;
        mHandler.postDelayed(mRunnable, TIMEOUT);
    }
    
    public void restartChectTime(){
    	mHandler.removeCallbacks(mRunnable);
        mHandler.postDelayed(mRunnable, TIMEOUT);
    }


    public void startNextCheckTime(){
//        mTryConnectIndex++;
//        restartChectTime();
    	mHandler.removeCallbacks(mRunnable);
    	mHandler.post(mRunnable);
    }
    /**
     *
     **/
    public void stopCheckTimeout() {
        mHandler.removeCallbacks(mRunnable);
    }

	private Handler mHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    private Runnable mRunnable = new Runnable() {

        @Override
        public void run() {
            mTryConnectIndex++;
            if (isConnecting) {
                if (mTryConnectIndex > (mTryConnectCounts) ) {
                    mCallback.onConnectFailed(mTryConnectIndex);
                } else {
                    mCallback.onReConnect(mTryConnectIndex);
                    mHandler.postDelayed(mRunnable, TIMEOUT);
                }
            } else {
                if (mTryConnectIndex > mTryConnectCounts ) {
                    mCallback.onReceivedFailed();
                } else {
                    mCallback.onReSend();
                    mHandler.postDelayed(mRunnable, TIMEOUT);
                }
            }
        }
    };

    public interface ITimeoutCallback {
        public void onReConnect(int tryConnectIndex);

        public void onConnectFailed(int tryConnectIndex);

        public void onReSend();


        public void onReceivedFailed();
    };
}
