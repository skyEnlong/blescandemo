package com.communication.ble;

import com.communication.bean.CodoonHealthDevice;

public interface OnDeviceSeartchCallback {
	 public boolean onSeartch(CodoonHealthDevice device, byte[] otherInfo);

     public boolean onSeartchTimeOut();
}
