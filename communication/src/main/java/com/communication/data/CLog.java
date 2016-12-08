package com.communication.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.communication.gpsband.SecretKeyUtil;
import com.communication.util.FileUtil;

/**
 * create a log file save to local when apk is debugable state
 * codoon_log in sdcard dir
 * @author workEnlong
 *
 */
public class CLog {

	private static File logFile;
	public static boolean isDebug = true;

	
	public static  synchronized void v(String tag, String msg) {

		if(!isDebug){
			return;
		}
		
		Log.v(tag, msg);
		println(tag, msg);
	}
	
	public static  synchronized void e(String tag, String msg) {

		if(!isDebug){
			return;
		}
		
		Log.e(tag, msg);
		println(tag, msg);
	}
	
	
	public static  synchronized void d(String tag, String msg) {

		if(!isDebug){
			return;
		}
		
		Log.d(tag, msg);
		println(tag, msg);
	}
	
	
	public static  synchronized void i(String tag, String msg) {

		if(!isDebug){
			return;
		}
		
		Log.i(tag, msg);
		println(tag, msg);
	}
	
	
	
	private static void println(String tag, String msg){

	}
	public static  void r(String fileName, byte[] outData){

		File f = creatLogFile(fileName);
		FileUtil.saveAsFile(f.getAbsolutePath(), outData);

	}


	private static File creatLogFile(String f_name) {

		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			String SDCARD_PATH = Environment.getExternalStorageDirectory()
					.getAbsolutePath();
			File fileDir = new File(SDCARD_PATH + File.separator + "accessory_log");
			if (!fileDir.exists()) {
				fileDir.mkdir();
			}

			 logFile = new File(fileDir.getAbsolutePath() + File.separator
					+ "descypt_"
					+ f_name
					);

			if (!logFile.exists()) {
				try {
					logFile.createNewFile();

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}

				return logFile;
			}
		}
		return logFile;
	}
}
