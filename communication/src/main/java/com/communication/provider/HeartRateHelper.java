package com.communication.provider;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;

public class HeartRateHelper {

	public static ArrayList<Integer> getOneDayToShow(Context context,String dayTime,String userId){
		HeartRateDB dao = new HeartRateDB(context);
		dao.open();
		HeartBean bean= dao.getHeartRateData(dayTime, userId);
		dao.close();
		if(null!=bean){
			return toShowHeartRateData(bean.getHeartData());
		}
		
		return null;
	}
	
	public static void saveHeartRateDataToLocal(Context context,ArrayList<HeartBean> data,String userId){
		HeartRateDB dao = new HeartRateDB(context);
		if(null!=data){
			dao.open();
			dao.beginTransaction();
			for(int i=0;i<data.size();i++){
				dao.updateHeartData(data.get(i), userId);
			}
			dao.setTransactionSuccessful();
			dao.endTransaction();
			dao.close();
		}
		
	}

	
	public static ArrayList<Integer> toShowHeartRateData(int[] heartData){
		ArrayList<Integer> showData = new ArrayList<Integer>();
		//Log.d("test","heartData="+heartData+" heartData len="+heartData.length);
		if(null!=heartData){
			for(int i=0;i<144;i++){
				int max = 0;
				for(int j=i*30;j<(i+1)*30;j++){
					
					if(max<heartData[j]){
						max = heartData[j];
					}
				}
				showData.add(max);
			}
		}
		
		return showData;
	}
	
	public static String conformToUploadHeartRateString(int[] heartData){
		StringBuilder sBuilder = new StringBuilder();
		for(int i=0;i<heartData.length;i++){
			sBuilder.append(heartData[i]);
			if(i!=heartData.length-1){
				sBuilder.append(",");
			}
		}
		return sBuilder.toString();
	}
	
	public static HeartBean getHeartRateByDate(Context mContext, String date, String userId){
		HeartRateDB dao = new HeartRateDB(mContext);
		dao.open();
		HeartBean bean= dao.getHeartRateData(date, userId);
		dao.close();
		return bean;
	}
}
