package com.communication.ble;

import com.communication.bean.CodoonBluethoothDevice;

public interface OnSeartchCallback {
	public boolean onSeartch(CodoonBluethoothDevice device, byte[] otherInfo);
	
	public boolean onSeartchTimeOut();
}
