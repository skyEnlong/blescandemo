package com.communication.data;

public class TransferStatus {
	/**
	 *  bind success id
	 */
	public static final int RECEIVE_BINED_ID = 0xC1;
	
	/**
	 *  connect sucess id
	 */
	public static final int RECEIVE_CONNECTION_ID = 0x81;
	
	/**
	 *  receive device software version
	 */
	public static final int RECEIVE_GETVERSION_ID = 0x82;
	
	/**
	 *  update start_time success id
	 */
	public static final int RECEIVE_UPDATETIME_ID = 0x8A;
	
	/**
	 *  get user info from device id
	 */
	public static final int RECEIVE_GETUSERINFO_ID = 0x87;
	
	/**
	 *  get user clock info from device
	 */
	public static final int RECEIVE_GETUSERINFO2_ID = 0x88;
	
	/**
	 *  update user info success
	 */
	public static final int RECEIVE_UPDATEUSERINFO_SUCCESS_ID = 0x85;
	
	/**
	 *  update user clock info success
	 */
	public static final int RECEIVE_UPDATEUSERINFO2_SUCCESS_ID = 0x86;
	
	/**
	 *  clear device data success
	 */
	public static final int RECEIVE_CLEARDATA_ID = 0x94;
	
	/**
	 *  receive device data
	 */
	public static final int RECEIVE_READDATA_ID = 0x91;
	
	/** 
	 *   receive the data frame count
	 */
	public static final int RECEIVE_FRAMECOUNT_ID = 0x8C;
	
	/**
	 *  receive device id success
	 */
	public static final int RECEIVE_DEVICE_ID = 0x84;
	
	/**
	 *  get device start_time
	 */
	public static final int RECEIVE_DEVICE_TIME_ID = 0x8B;

	/**
	 *  get boot state
	 */
	public static final int RECEIVE_BOOT_STATE_ID = 0xF0;
	
	/** 
	 *  connecct to boot mode
	 */
	public static final int RECEIVE_BOOT_CONNECT_ID = 0xF1;
	
	/**
	 *  the version of boot mode
	 */
	public static final int RECEIVE_BOOT_VERSION_ID = 0xF2;
	
	/**
	 *  write data to device sucess
	 */
	public static final int RECEIVE_BOOT_UPGRADE_ID = 0xF3;
	
	/**
	 *  upgrade success
	 */
	public static final int RECEIVE_UPGRADE_OVER_ID = 0xF4;
	
	
	/**
	 *  friends request success
	 */
	public static final int RECEIVE_FRIENDS_REQUEST_ID = 0xD1;
	
	/**
	 *  friends warning success
	 */
	public static final int RECEIVE_FRIENDS_WARNING_ID = 0xD2;
	
	
	/**
	 *  the offset of friends
	 */
	public static final int REICEIVE_FRIENDS_SWITCH_ID = 0xD3;
	
	
}
