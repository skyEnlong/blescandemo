package com.communication.provider;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class HeartRateDB extends AccessoryDataBaseHelper {
	public static final String DAY_HEART_TABLE_NAME = "day_heart_table";
	public static final String FIELD_ID_PK = "_id";
	public static final String FIELD_DATE = "date";
	public static final String FIELD_DATA = "data";
	public static final String FIELD_USER_ID = "userId";
	public static final String FIELD_PRODUCT_ID = "productID";
	
	public static final String CREAT_DAY_HEART_TABLE = "CREATE TABLE IF NOT EXISTS " + DAY_HEART_TABLE_NAME + " (" +
			FIELD_ID_PK + " INTEGER PRIMARY KEY, "+
			FIELD_DATE + " TEXT, " +
			FIELD_USER_ID + " TEXT, " +
			FIELD_PRODUCT_ID + " TEXT, " +
			FIELD_DATA + " TEXT " +");";
	
	public HeartRateDB(Context context){
		super(context);
	}
	
	public void beginTransaction() {
		db.beginTransaction();
	}

	public void setTransactionSuccessful() {
		db.setTransactionSuccessful();
	}

	public void endTransaction() {
		db.endTransaction();
	}

	
	
	public long insertData(String dayTime,String data,String userId,String productId){
		long count = 0;
		ContentValues initialValues = new ContentValues();
		initialValues.put(FIELD_DATE, dayTime);
		initialValues.put(FIELD_PRODUCT_ID, productId);
		initialValues.put(FIELD_USER_ID, userId);
		initialValues.put(FIELD_DATA, data);
		count = db.insert(DAY_HEART_TABLE_NAME, null, initialValues);
		
		return count;
	}
	
	public HeartBean getHeartRateData(String dayTime,String userId){
		String selectStr =FIELD_DATE + "='"+dayTime+"' and "+ FIELD_USER_ID+" = '"+userId+"' ";
		Cursor c = db.query(DAY_HEART_TABLE_NAME, new String[]{FIELD_USER_ID,FIELD_DATE,FIELD_DATA},selectStr, null, null, null, null);
		HeartBean mod = null;
		
		
		if (c == null) {
			return null;
        } else {
        	
        	try{
            if (c.moveToFirst()) {
            	String data = c.getString(c.getColumnIndex(FIELD_DATA));
            	mod = new HeartBean();
                mod.user_id = c.getString(c.getColumnIndex(FIELD_USER_ID));
                mod.setDayTime(c.getString(c.getColumnIndex(FIELD_DATE)));
                String[] heartData = data.split(",");
                int[] intData = new int[heartData.length];
                for(int i=0;i<heartData.length;i++){
                	intData[i] = Integer.valueOf(heartData[i]);
                }
                mod.setHeartData(intData);
                
            }
        	} catch (IllegalStateException e) {

			} finally {
				c.close();
			}

        }

        return mod;
	}
	
	
	public long updateHeartData(HeartBean bean,String userId){
		HeartBean dayHeartBean = getHeartRateData(bean.getDayTime(),userId);
		if(null!=dayHeartBean
				&&dayHeartBean.getHeartData()!=null
				&&dayHeartBean.getHeartData().length>0){
			//
			int[] getData = dayHeartBean.getHeartData();
			int[] upData = bean.getHeartData();
			
			for(int i=0;i<getData.length;i++){
				if(getData[i]>upData[i]){
					upData[i] = getData[i];
				}
			}
			String upDataString = dataToString(upData);
			ContentValues cValues = new ContentValues();
			cValues.put(FIELD_DATA, upDataString);
			String selectStr =FIELD_DATE + "='"+bean.getDayTime()+"' and "+ FIELD_USER_ID+" = '"+userId+"' and "+FIELD_PRODUCT_ID+" = '"+bean.getProductId()+"'";
			return db.update(DAY_HEART_TABLE_NAME, cValues, selectStr, null);
			
		}else{
			return insertData(bean.getDayTime(), dataToString(bean.getHeartData()), userId, bean.getProductId());
		}
	}
	
	public String dataToString(int[] heartData){
		StringBuilder sBuilder = new StringBuilder();
		for(int i=0;i<heartData.length;i++){
			sBuilder.append(heartData[i]);
			if(i!=heartData.length-1){
				sBuilder.append(",");
			}
		}
		return sBuilder.toString();
	}
}
