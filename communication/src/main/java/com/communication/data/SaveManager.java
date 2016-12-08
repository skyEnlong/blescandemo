package com.communication.data;

import android.content.Context;
import android.util.Log;

import com.communication.provider.Java2Xml;

public class SaveManager {
	
	private String TAG="SaveManager";
	private Context mContext;
	private eSaveType mSaveType;
	
	
	private Java2Xml mJava2Xml;
	public SaveManager(Context context,eSaveType saveType){
		mContext=context;
		mSaveType=saveType;
		if(mSaveType==eSaveType.XML){
			mJava2Xml=new Java2Xml(mContext);
		}else if(mSaveType==eSaveType.DATABSE){
		}
	}
	
	 
	/**
	 * 
	 * @param step
	 * @param calorie
	 * @param meter
	 * @param time
	 */
	public void addASportData(int step,int calorie,int meter,long time){
		if(mSaveType==eSaveType.DATABSE){
		}else {
			mJava2Xml.addSportNode(step, calorie, meter, time);
		}
	}
	
	
	/**
	 * 
	 * @param level
	 * @param time
	 */
	public void addASleepData(int level, long time){
		if(mSaveType==eSaveType.DATABSE){
			Log.d(TAG, "addASleepData() level:"+level+",start_time:"+time);
		}else{
			mJava2Xml.addSleepNode(level, time);
		}
	}
	
	/**
	 * 
	 */
	public void save(){
		if(mSaveType==eSaveType.DATABSE){
		}else{
			mJava2Xml.save();
		}
	}
	
	public enum eSaveType{
		XML,DATABSE;
	}
}
