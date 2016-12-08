package com.communication.bean;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;

public class CodoonBluethoothDevice implements Parcelable {

	public String device_name = "";
	public BluetoothDevice device;
	
	public static final Parcelable.Creator<CodoonBluethoothDevice> CREATOR = new Parcelable.Creator<CodoonBluethoothDevice>(){

		@Override
		public CodoonBluethoothDevice createFromParcel(Parcel src) {
			// TODO Auto-generated method stub
			CodoonBluethoothDevice user = new CodoonBluethoothDevice();
			user.device_name = src.readString();
			user.device =  src.readParcelable(BluetoothDevice.class.getClassLoader());
			return user;
		}

		@Override
		public CodoonBluethoothDevice[] newArray(int size) {
			// TODO Auto-generated method stub
			return null;
		}
		
	};
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeString(device_name);
		dest.writeParcelable(device, 0);
	}

	public String getName() {
		// TODO Auto-generated method stub
		return device_name;
	}
	
	public BluetoothDevice getDevice(){
		return device;
	}

	@Override
	public boolean equals(Object o) {
		// TODO Auto-generated method stub
		if(null == o) return false;
		if(o instanceof CodoonBluethoothDevice){
			CodoonBluethoothDevice toCompare = (CodoonBluethoothDevice) o;
			return toCompare.getDevice().getAddress().equals(device.getAddress());
		}
		return super.equals(o);
	}

	
}
