package com.communication.data;

/**
 * Created by 0H7RXL on 2016/6/21.
 */
public interface IHeartCallBack extends ISyncCallBack {
    public void onConnectStateChanged(int state, int newState);

    public void onBindSucess();

    public void onGetValue(int value);
}
