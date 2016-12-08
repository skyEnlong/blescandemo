package com.example.enlong.blescandemo;

import android.os.Environment;
import android.util.Log;

import com.communication.util.FileUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

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

	public static void recordAction(String action) {

		SimpleDateFormat	timeFormat = new SimpleDateFormat("HH:mm:ss");
		File file = new File(FileUtil.getDataFilePath(null) + File.separator+"test.txt");
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		Date d = new Date(System.currentTimeMillis());

		String content = timeFormat.format(d) + " " + action;

		try {
			FileWriter fw = new FileWriter(file, true);
//            content = Base64.encode(content.getBytes("utf-8"), 0, content.length());

			PrintWriter pw = new PrintWriter(fw);
			pw.println(content);
			pw.close();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}catch (Exception e){
			e.printStackTrace();
		}
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
