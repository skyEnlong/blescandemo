package com.communication.unionpay;

/**
 * Created by workEnlong on 2015/6/11.
 */
public interface IUnionPayCallback {
    public void onGetData(byte[] data);
    public void onDeviceBind();
    public void onDeviceUnBind();
}
