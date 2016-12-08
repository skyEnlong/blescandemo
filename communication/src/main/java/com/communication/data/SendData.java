package com.communication.data;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.communication.bean.Person;

import android.util.Log;

public class SendData {

	private static String TAG="SendData";
	
	
	 /**
     *0. mmand of bind the device
     * 
     * @return
     */
	public static int[] getPostBindOrder(){
		return new int[]{ 0xAA, 0x41, 0x00, 0xEB};
	}
	
    /**
     *1. command of connection the device
     * 
     * @return
     */
    public static int[] getPostConnection() {
        return new int[] { 0xAA, 0x01, 0x0, 0xAB };
    }

    /** 
     *2. command of get device type and version
     * 
     * @return
     */
    public static int[] getPostDeviceTypeVersion() {
        return new int[] { 0xAA, 0x02, 0x00, 0xAC };
    }

    /**
     *3. command of write device id ------------
     * @param type deviceID
     * @return
     */
    public static int[] getPostWriteDeviceID(int type, int[] deviceID) {
        int result = 0;
        int[] outData = new int[4 + deviceID.length + 1];
        outData[0] = 0xAA;
        outData[1] = 0x03;
        outData[2] = 0x0D;
        outData[3] = type;
        result += 0xAA + 0x03 + 0x0D + type;
        for (int i = 0; i < deviceID.length; i++) {
            outData[i + 4] = deviceID[i];
            result += deviceID[i];
        }
        outData[outData.length - 1] = result & 0xFF;
        return outData;
    }

    /**
     *4. command of get device ID
     * 
     * @return
     */
    public static int[] getPostDeviceID() {
        return new int[] { 0xAA, 0x04, 0x00, 0xAE };
    }

    /**
     * command of update SportInfo(target)
     * @param sportinfo
     * 5. update user info
     */
    public static int[] getPostUpdateSportInfo(int[] sportinfo) {
        int result = 0;
        int[] outData = new int[3 + sportinfo.length + 1];
        outData[0] = 0xAA;
        outData[1] = 0x05;
        outData[2] = 0x0E;
        result += 0xAA + 0x05 + 0x0E;
        for (int i = 0; i < sportinfo.length; i++) {
            outData[i + 3] = sportinfo[i];
            result += sportinfo[i];
        }
        outData[outData.length - 1] = result & 0xFF;
        return outData;
    }
    
    /**
     * *@param sportinfo
     * @param userInfo
     * @return
     */
    public static int[] getPostUpdateUserInfoAll(int[] userInfo){
        int result = 0;
        int[] outData = new int[3 + userInfo.length + 1];
        outData[0] = 0xAA;
        outData[1] = 0x05;
        outData[2] = 0x0E;
        result += 0xAA + 0x05 + 0x0E;
        for (int i = 0; i < userInfo.length; i++) {
            outData[i + 3] = userInfo[i];
            result += userInfo[i];
        }
        outData[outData.length - 1] = result & 0xFF;
        return outData;
    }
   
    
	/**
	 * 6. update user info ------------
	 * 
	 * @return
	 */
	public static int[] getPostUpdateUser2(int[] userinfo) {
		int[] outData = new int[userinfo.length + 4];
		outData[0] = 0xAA;
		outData[1] = 0x06;
		outData[2] = userinfo.length;
		
		for(int i = 0; i < userinfo.length; i++){
			outData[i + 3] = userinfo[i];
		}
		
		for(int i = 0; i < outData.length -1; i++){
        	outData[outData.length -1] += outData[i];
        }
    	outData[outData.length -1] = outData[outData.length -1] & 0x00FF;
		return outData;
	}

    /**
     * 7. get user info
     * 
     * @return
     */
    public static int[] getPostGetUserInfo() {
        return new int[] { 0xAA, 0x07, 0x00, 0xB1 };
    }

    /**
     *8. get device battery
     * 
     * @return
     */
    public static int[] getPostGetUserInfo2() {
        return new int[] { 0xAA, 0x08, 0x00, 0xB2 };
    }

    /**
     * 10. update start_time
     * @param currentTime
     */
    public static int[] getPostSyncTime(long currentTime) {
        int result = 0;
        int[] outData = new int[11];
        outData[0] = 0xAA;
        outData[1] = 0x0A;
        outData[2] = 0x07;
        result += 0xAA + 0x07 + 0x0A;

        SimpleDateFormat format = new SimpleDateFormat("yy MM dd HH mm ss");
//        format.setTimeZone(TimeZone.getDefault());
        Date date=new Date(currentTime);
        String time = format.format(date);
         
        String[] times = time.split(" ");

        for (int i = 0; i < 6; i++) {
            outData[i + 3] = Integer.valueOf(times[i], 16);
            result += outData[i + 3];
        }
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        outData[9] = (cal.get(Calendar.DAY_OF_WEEK) - 2 + 7) % 7;
        outData[outData.length - 1] = (outData[9] + result) & 0xFF;
        return outData;
    }

    /**
     * 11. get device start_time
     */
    public static int[] getPostDeviceTime() {
        return new int[] { 0xAA, 0x0B, 0x0, 0xB5 };
    }

    /**
     *12. get device frame count`
     * 
     * @return
     */
    public static int[] getPostSyncDataByFrame() {
        return new int[] { 0xAA, 0x0C, 0x0, 0xB6 };
    }
    
    /**
     * get the data of frame 
     * @param frame
     * @return
     */
    public static int[] getPostReadSportData(int frame){
        Log.d(TAG, "Frame:"+frame);
        int high=frame >> 8;
        int low=frame & 0xFF;
        return new int[]{0xAA, 0x11, 0x02, high, low, (0xBD + high + low)&0xFF};
    }

    /**
     * 
     * 20.command clear sport data
     */
    public static int[] getPostClearSportData() {
        return new int[] { 0xAA, 0x14, 0, 0xBE };
    }
    
       
    /**
     * @return
     */
    public static int[] postBootMode(){
    	 int result = 0;
         int[] outData = new int[4];
         outData[0] = 0xAA;
         outData[1] = 0x70;
         outData[2] = 0x00;
         result += 0xAA + 0x70 + 0x00;
         outData[3] = result & 0x00FF;
         return outData;
    }
    
    /**
     *
     * @return
     */
    public static int[] postConnectBootOrder(){
    	 int result = 0;
         int[] outData = new int[4];
         outData[0] = 0xAA;
         outData[1] = 0x71;
         outData[2] = 0x00;
         result += 0xAA + 0x71 + 0x00;
         outData[3] = result & 0x00FF;
         return outData;
    }
    
    

    /**
     *
     * @return
     */
    public static int[] postConnectBootVersion(){
    	 int result = 0;
         int[] outData = new int[4];
         outData[0] = 0xAA;
         outData[1] = 0x72;
         outData[2] = 0x00;
         result += 0xAA + 0x72 + 0x00;
         outData[3] = result & 0x00FF;
         return outData;
    }
    
    /**
     *
     * @param data
     * @return
     */
    public static int[] postBootUploadData(int index, byte[] data){
    	 int result = 0;
         int[] outData = new int[6 + data.length];
         outData[0] = 0xAA;
         outData[1] = 0x73;
         outData[2] = 0x0e;
         outData[3] = index >> 8;
         outData[4] = index & 0x00FF;
         
         for(int i = 0; i < data.length; i++){
        	 outData[i + 5] = data[i] & 0x000000ff;
         }
         String str = "";
         for(int i =0; i < outData.length -1; i++){
        	 result += outData[i];
         }
         result &= 0x000000ff;
         outData[outData.length -1] = result;
         
         for(int i =0; i < outData.length; i++){
        	 result += outData[i];
        	 str += "," + Integer.toHexString(outData[i]);
         }
         
          return outData;
    }
    
    /**
      * @param data
     * @return
     */
    public static int[] postBootUploadData(int index, byte[] data, int length){
    	 int result = 0;
         int[] outData = new int[6 + length];
         outData[0] = 0xAA;
         outData[1] = 0x73;
         outData[2] = 0x02 + length;
         outData[3] = (index >> 8) & 0xff;
         outData[4] = index & 0x00FF;
         
         for(int i = 0; i < length; i++){
        	 outData[i + 5] = data[i] & 0x000000ff;
         }
         for(int i =0; i < outData.length -1; i++){
        	 result += outData[i];
         }
         result &= 0x000000ff;
         outData[outData.length -1] = result;
         
         DataUtil.DebugPrint(outData);
         return outData; 
    }
    
    /**
     *
     * @param checkData
     * @return
     */
    public static int[] postBootEnd(int checkData){
        int[] outData = new int[6];
        outData[0] = 0xAA;
        outData[1] = 0x74;
        outData[2] = 0x02;
        outData[3] = checkData >> 8;
        outData[4] = checkData & 0x000000FF;
        for(int i = 0; i < outData.length -1; i++){
        	outData[5] += outData[i];
        }
        outData[5] = outData[5] & 0x000000FF;
        
        DataUtil.DebugPrint(outData);
        
        return outData;
    }
    
    /**
     *  the order to weight scale connect 
     * @return
     */
    public static int[] postWeightScaleConnect(){
    	int[] outData = new int[4];
    	outData[0] = 0x68;
    	outData[1] = 0x01;
    	outData[2] = 0x00;
   
    	for(int i = 0; i < outData.length -1; i++){
        	outData[outData.length -1] += outData[i];
        }
    	outData[outData.length -1] = outData[outData.length -1] & 0x00FF;
    	return outData;
    }
    
    /**
     *  the order set weight info to scale
     * @param person
     * @return
     */
    public static int[] postWeightInfo(Person person){
    	if(null == person) return null;
    	
    	int[] outData = new int[18];
    	
    	for(int i = 0; i< outData.length; i++){
    		outData[i] = 0;
    	}
    	
    	outData[0] = 0x68;
    	outData[1] = 0x05;
    	outData[2] = 0x0E;
    	outData[3] = person.group;
    	outData[4] = person.sex;
    	outData[5] = person.level;
    	outData[6] = person.height;
    	outData[7] = person.age;
    	outData[8] = 1;       // dan wei mo ren wei 1
    	
    	for(int i = 0; i < outData.length -1; i++){
        	outData[outData.length -1] += outData[i];
        }
    	outData[outData.length -1] = outData[outData.length -1] & 0x00FF;
    	return outData;
    }
    
    public static int[] postBlueFriendWarning(){
    	int[] outData = new int[4];
    	outData[0] = 0xAA;
    	outData[1] = 0x52;
    	outData[2] = 0x00;
    	
    	for(int i = 0; i < outData.length -1; i++){
        	outData[outData.length -1] += outData[i];
        }
    	outData[outData.length -1] = outData[outData.length -1] & 0x00FF;
    	return outData;
    }
    
    
    public static int[] postBlueFriendRequst(){

    	int[] outData = new int[4];
    	outData[0] = 0xAA;
    	outData[1] = 0x51;
    	outData[2] = 0x00;
    	
    	for(int i = 0; i < outData.length -1; i++){
        	outData[outData.length -1] += outData[i];
        }
    	outData[outData.length -1] = outData[outData.length -1] & 0x00FF;
    	return outData;
    
    }
    
    /**
     *
     * @param offOrOpen
     * @return
     */
    public static int[] postBlueFriendsSwitch(int offOrOpen){

    	int[] outData = new int[4];
    	outData[0] = 0xAA;
    	outData[1] = 0x52;
    	outData[2] = 0x00;
    	
    	for(int i = 0; i < outData.length -1; i++){
        	outData[outData.length -1] += outData[i];
        }
    	outData[outData.length -1] = outData[outData.length -1] & 0x00FF;
    	return outData;
    
    }
    
    
    /**
     *
     * @param outData
     */
    private static void setCheckValue(int[] outData){
    	if(null == outData) return;
    	for(int i = 0; i < outData.length -1; i++){
        	outData[outData.length -1] += outData[i];
        }
    	outData[outData.length -1] = outData[outData.length -1] & 0x00FF;
    }
    
}
