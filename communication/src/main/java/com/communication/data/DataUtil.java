package com.communication.data;

import com.communication.util.CommonUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class DataUtil {

	static String TAG = "ble_parse";
	
	public static String DebugPrint(int[] outData) {
		if (!isDebug()) {
			return null;
		}
		if(null == outData) return null;
		String str = "";
		for (int i = 0; i < outData.length; i++) {
			str += Integer.toHexString(outData[i]) + "   ";
		}
		CLog.i(TAG, str + "   lenth:" + outData.length);
		return str;
	}

	private static boolean isDebug() {
		// TODO Auto-generated method stub
//		return CLog.isDebug;
		return true;
	}

	public static String DebugPrint(byte[] outData) {
		if (!isDebug()) {
			return null;
		}
		if(null == outData) return null;
		String str = "";
		for (int i = 0; i < outData.length; i++) {

			str += CommonUtils.getHexString(outData[i]) + "   ";
			if(i % 8 == 7 ){
				str += "\n";
			}
		}

		CLog.i(TAG, str );
		return str;
	}

    public static String DebugPrint(String tag, byte[] outData) {

        if(null == outData) return null;
        String str = "";
        for (int i = 0; i < outData.length; i++) {
            str += Integer.toHexString(outData[i] & 0xff) + "   ";
        }

        CLog.i(tag, str + "   lenth:" + outData.length);
        return str;
    }

	public static void DebugPrint(List<Integer> values) {
		if (!isDebug()) {
			return;
		}
		if(null == values) return ;
		String result = "";
		for (int i : values) {
			result += Integer.toHexString(i) + "   ";
		}
		CLog.i(TAG, "receive:" + result);
	}

	
	public static void DebugPrint(String str){
		if (!isDebug()) {
			return;
		}
		
		CLog.i(TAG, str);
	}
	public static boolean equalArray(int[] data1, int[] data2){
		if(null == data1  || data2 == null){
			return false;
		}
		if(data1.length == data2.length){
			for(int i  = 0; i< data1.length; i++){
				if(data1[i] != data2[i]){
					return false;
				}
			}
			return true;
		}
		
		return false;
	}
	
	public static long getDay24time(long time){
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date(time);
		String st = format.format(date);
		try {
			Date mDate = format.parse(st);
			Calendar mCalendar = Calendar.getInstance();
			mCalendar.setTime(mDate);
			mCalendar.add(Calendar.DATE, 1);
			return mCalendar.getTimeInMillis();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return time;
	}

}
