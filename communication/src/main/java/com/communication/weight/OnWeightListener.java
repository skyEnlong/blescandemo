package com.communication.weight;

import com.communication.bean.Person;
import com.communication.bean.WeightInfo;

public interface OnWeightListener {
	
	public void onGetWeightInfo(WeightInfo info);
	
	public void onGetDeiveId(int id);
	
	public Person onLoadPersonInfo();
	
	public void onTimeOut(int errinfo);
	
	public void onConnect();
}
