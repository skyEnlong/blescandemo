package com.communication.provider;

import java.io.Serializable;
import java.util.ArrayList;

import org.json.JSONObject;

import android.R.integer;
import android.util.Log;

public class HeartBean implements Serializable{
	public String product_id;
	public int[] content;
	public String user_id;
	public String the_day;
	
	
	public String getDayTime() {
		return the_day;
	}
	public void setDayTime(String dayTime) {
		this.the_day = dayTime;
	}
	public int[] getHeartData() {
		return content;
	}
	public void setHeartData(int[] heartData) {
		this.content = heartData;
	}
	
	
	
	public String getProductId() {
		return product_id;
	}
	public void setProductId(String productId) {
		this.product_id = productId;
	}
	
	
	
	
	
}
