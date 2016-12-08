package com.communication.lenovo.ble;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.R.integer;

public class LenovoSendData {
	private static String TAG="LenovoSendData";
	
	
	 /**
    *0. connect to device to get version deviceId
    * 
    * @return
    */
	public static int[] getPostConnect(){
		int[] connect = new int[20];
		connect[0] = 0xEE;
		connect[1] = 0xEE;
		for(int i=2;i<20;i++){
			connect[i] = 0x00;
		}
		return connect;
	}
	//44 41 49 4C 59 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FA 
	public static int[] getDailySportData(){
		int[] connect = new int[20];
		connect[0] = 0x44;
		connect[1] = 0x41;
		connect[2] = 0x49;
		connect[3] = 0x4C;
		connect[4] = 0x59;
		for(int i=5;i<19;i++){
			connect[i] = 0;
		}
		connect[19] = 0xFA;
		
		return connect;
	}
	/*public static int[] getSettingCurrentTime(){
		int[] setting = new int[20];
		setting[0] = 0x53;
		setting[1] = 0x45;
		setting[2] = 0x54;
		long start_time = System.currentTimeMillis();
		String timeStr = sdf.format(new Date(start_time));
		String[] timeA = timeStr.split(" ");
		int year = Integer.valueOf(String.valueOf(Integer.valueOf(timeA[0]) - 2000),16);
		
		setting[3] = year & 0xFF;
		for(int j=1;j<6;j++){
			setting[j+3] = Integer.valueOf(timeA[j], 16);
		}
		setting[9] = 0x01;
		setting[10] = LenovoCommandStatus.settingPakcet1.get(7);
		setting[11] = 0x01;
		if(null!=LenovoCommandStatus.settingPakcet1&&LenovoCommandStatus.settingPakcet1.size()==20){
			for(int i=12;i<18;i++){
				setting[i] = LenovoCommandStatus.settingPakcet1.get(i-3);
			}
		}
		setting[18] = 0;
		setting[19] = 0xFF;
		return setting;
	}*/
	/**
	 * update userinfo
	 * and start_time
	 * @param sex
	 * @param age
	 * @param height
	 * @param weight
	 * @return
	 */
	public static int[] getUpdateUserInfo(int sex,int age,int height,int weight){
		int[] setting = new int[20];
		setting[0] = 0x53;
		setting[1] = 0x45;
		setting[2] = 0x54;
		long time = System.currentTimeMillis();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy MM dd HH:mm:ss");
		String timeStr = sdf.format(new Date(time));
		String[] timeA = timeStr.split(" ");
//		int year = Integer.valueOf(String.valueOf(Integer.valueOf(timeA[0]) - 2000),16);
		int year = Integer.valueOf(timeA[0]) - 2000;
		
		setting[3] = year & 0xFF;
		setting[4] = Integer.valueOf(timeA[1]) & 0xFF;
		setting[5] = Integer.valueOf(timeA[2]) & 0xFF;
		String[] timeB = timeA[3].split(":");
		setting[6] = Integer.valueOf(timeB[0]) & 0xFF;
		setting[7] = Integer.valueOf(timeB[1]) & 0xFF;
		setting[8] = Integer.valueOf(timeB[2]) & 0xFF;
		
		setting[9] = 0x01;
		setting[10] = 0x20;
		setting[11] = 0x01;
		setting[12] = sex;
		setting[13] = Integer.valueOf(String.valueOf(getCorrectAge(age)), 16);
		setting[14] = getCorrectHexHeight(height);
		setting[15] = getCorrectWeight(weight) & 0xFF;
		setting[16] = getCorrectWeight(weight) >> 8;
		setting[17] = 0x00;
		setting[18] = 0;
		setting[19] = 0xFF;
		return setting;
	}
	/**
	 * update goalStep
	 * @param goalStep
	 * @return
	 */
	public static int[] getUpdateGoalStep(int goalStep){
		int[] setting = new int[20];
		setting[0] = 0x53;
		setting[1] = 0x45;
		setting[2] = 0x54;
		setting[3] = 0xE0;
		setting[4] = 0x16;
		setting[5] = 0x00;
		setting[6] = 0x07;
		setting[7] = 0x1E;
		setting[8] = 0xA0;
		setting[9] = 0x08;
		setting[10] = 0x00;
		
		
		
		int step = getCorrectGoalStep(goalStep);
		setting[11] = step & 0xFF;
		setting[12] = step >> 8;
		setting[13] = step >> 16;
		
		setting[14] = 0x00;
		setting[15] = 0x00;
		setting[16] = 0x03;
		setting[17] = 0x22;
		setting[18] = 1;
		setting[19] = 0xFF;
		return setting;
	}
	
	
	
	
	/**
	 * get setting values 
	 * will get 3 package
	 * @return
	 */
	public static int[] getSettingValueCommand(){
		int[] setting = new int[20];
		for(int i=0;i<20-2;i++){
			setting[i] = 0x00;
		}
		setting[18] = 0xEE;
		setting[19] = 0xEE;
		return setting;
	}
	
	public static int[] getSportDataCommandCommand(){
		int[] command = new int[20];
		command[0] = 0x52;
		command[1] = 0x55;
		command[2] = 0x4E;
		for(int i=3;i<20-3;i++){
			command[i] = 0x00;
		}
		command[17] = 0xFF;
		command[18] = 0xFF;
		command[19] = 0xFB;
		return command;
	}

	
	public static int[] getSleepDataCommandCommand(){
		int[] command = new int[20];
		command[0] = 0x53;
		command[1] = 0x4C;
		command[2] = 0x45;
		command[3] = 0x45;
		command[4] = 0x50;
		for(int i=5;i<20-1;i++){
			command[i] = 0x00;
		}
		command[19] = 0xFC;
		return command;
	}
	
	
	/**
	 * disconnect device
	 * @return
	 */
	public static int[] getEndConnectCommand(){
		int[] command = new int[20];
		command[0] = 0x45;
		command[1] = 0xEE;
		command[2] = 0x44;
		for(int i=3;i<20;i++){
			command[i] = 0x00;
		}
		return command;
	}
	
	public static int[] getClearDataCommandCommand(){
		int[] command = new int[20];
		for(int i=0;i<20-3;i++){
			command[i] = 0x00;
		}
		command[17] = 0xFF;
		command[18] = 0xFF;
		command[19] = 0xFD;
		return command;
	}
	//53 48 52 5A B0 B0 B1 B2 B3 B4 B5 B6 B7 B8 B9 B10 B11 B12 B13 B14 B15
	public static int[] setHeartRateMaxMinData(int maxData,int minData){
		int[] command = new int[20];
		command[0] = 0x53;
		command[1] = 0x48;
		command[2] = 0x52;
		command[3] = 0x5A;
		command[4] = 0xB0;
		command[5] = maxData;
		command[6] = minData;
		command[7] = maxData;
		command[8] = minData;
		command[9] = maxData;
		command[10] = minData;
		command[11] = 0;
		for(int i=12;i<20;i++){
			command[i] = 0;
		}
		return command;
	}
	public static int getCorrectAge(int age){
		if(age>=5&&age<=0x63){
			return age;
		}
		if(age < 5){
			return 5;
		}
		if(age > 0x63){
			return 99;
		}
		return 5;
	}
	
	public static int getCorrectHexHeight(int height){
		if(height>=0x5B&&height<=0xF1){
			return intToHex(height);
		}
		if(height < 0x5B){
			return 0x5B;
		}
		if(height > 0xF1){
			return 0xF1;
		}
		return 0x5B;
	}
	
	public static int getCorrectWeight(int height){
		if(height>=0x14&&height<=0xFB){
			return height;
		}
		if(height < 0x14){
			return 20;
		}
		if(height > 0xFB){
			return 251;
		}
		return 20;
	}
	
	public static int getCorrectGoalStep(int step){
		if(step>=0x00&&step<=0x15F90){
			return step;
		}
		if(step < 0x00){
			return 0;
		}
		if(step > 0x15F90){
			return Integer.valueOf(String.valueOf(0x15F90), 16);
		}
		return 0;
	}
	
	
	private static int intToHex(int data){
		return Integer.valueOf(String.valueOf(data), 16);
	}
}
