package com.communication.data;

/**
 * Created by workEnlong on 2016/1/20.
 */
public interface ISyncCallBack {
    public void onDeviceDisconnect();
    public void onConnectSuccessed();
    public void onTimeOut();
}
