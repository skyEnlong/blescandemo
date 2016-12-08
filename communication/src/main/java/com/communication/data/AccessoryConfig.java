package com.communication.data;

import android.content.Context;
import android.content.SharedPreferences;

public class AccessoryConfig {

	public static String userID = "";
	private final static String mXmlFile = "accessory_config_xml";

	public static int getIntValue(Context context, String key) {
		try {
			SharedPreferences store = context.getSharedPreferences(mXmlFile, Context.MODE_PRIVATE);
			return store.getInt(key, 0);
		} catch (Exception e) {
			return 0;
		}
	}

	public static int getIntValue(Context context, String key, int defaultValue) {
		try {
			SharedPreferences store = context.getSharedPreferences(mXmlFile, Context.MODE_PRIVATE);
			return store.getInt(key, defaultValue);
		} catch (Exception e) {
			return 0;
		}
	}

	public static void setIntValue(Context context, String key, int value) {
		SharedPreferences store = context.getSharedPreferences(mXmlFile, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = store.edit();
		editor.putInt(key, value);
		editor.commit();

	}

	public static Long getLongValue(Context context, String key, long defalut) {
		SharedPreferences store = context.getSharedPreferences(mXmlFile, Context.MODE_PRIVATE);
		return store.getLong(key, defalut);
	}

	public static void setLongValue(Context context, String key, long value) {
		SharedPreferences store = context.getSharedPreferences(mXmlFile, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = store.edit();
		editor.putLong(key, value);
		editor.commit();

	}

	public static float getFloatValue(Context context, String key, float defalut) {
		SharedPreferences store = context.getSharedPreferences(mXmlFile, Context.MODE_PRIVATE);
		return store.getFloat(key, defalut);
	}

	public static void setFloatValue(Context context, String key, float value) {
		SharedPreferences store = context.getSharedPreferences(mXmlFile, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = store.edit();
		editor.putFloat(key, value);
		editor.commit();
	}

	public static String getStringValue(Context context, String key) {
		SharedPreferences store = context.getSharedPreferences(mXmlFile, Context.MODE_PRIVATE);
		return store.getString(key, "");
	}

	public static void setStringValue(Context context, String key, String value) {
		
		SharedPreferences store = context.getSharedPreferences(mXmlFile, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = store.edit();
		editor.putString(key, value);
		editor.commit();

	}

	public static boolean getBooleanValue(Context context, String key,
			boolean defalut) {
		SharedPreferences store = context.getSharedPreferences(mXmlFile, Context.MODE_PRIVATE);
		return store.getBoolean(key, defalut);
	}

	public static void setBooleanValue(Context context, String key,
			boolean value) {
		SharedPreferences store = context.getSharedPreferences(mXmlFile, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = store.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}

}
