package kr.co.netseason.myclebot.Logger;

import android.content.Context;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import kr.co.netseason.myclebot.openwebrtc.Config;

public class Logger {
	
	public static final String PREF_LOG = "PREF_LOG";
	
	private static boolean isVerboseEnable = true;
	private static boolean isDebugEnable = true;
	private static boolean isInfoEnable = true;
	private static boolean isWarnEnable = true;
	private static boolean isErrorEnable = true;

	public static void setVerboseEnabled(boolean isEnable) {
		isVerboseEnable = isEnable;
	}
	
	public static void setDebugEnable(boolean isEnable) {
		isDebugEnable = isEnable;
	}
	
	public static void setInfoEnable(boolean isEnable) {
		isInfoEnable = isEnable;
	}
	
	public static void setWarnEnable(boolean isEnable) {
		isWarnEnable = isEnable;
	}
	
	public static void setErrorEnable(boolean isEnable) {
		isErrorEnable = isEnable;
	}
	
	public static void setAllLogEnable(Context context, boolean isEnable) {
		isVerboseEnable = isEnable;
		isDebugEnable = isEnable;
		isInfoEnable = isEnable;
		isWarnEnable = isEnable;
		isErrorEnable = isEnable;
	}

	public static void logFileCreate() {
		try{
			File filepath = new File(strFilePath);

			if (!filepath.exists()) {
				Log.e("!!!", "filepath.mkdirs()");
				filepath.mkdirs();
			} else {
				Log.e("!!!", "filepath.mkdirs() is not exe");
			}
			File fileCacheItem = new File(strFilePath + strFileName);

			FileWriter fw = new FileWriter(fileCacheItem);
			fw.write("~~~~~~~~~~~~~   Log Start   ~~~~~~~~~~~\r\n");
			fw.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	private static String strFilePath = Config.getSaveImageFileExternalDirectory()+"Log/";
	private static String strFileName = "log.txt";
	public static void writeLogFile(String str) {
//		try{
//			File fileCacheItem = new File(strFilePath + strFileName);
//			BufferedWriter fw = new BufferedWriter(new FileWriter(fileCacheItem, true));
//
//			Date now = new Date(System.currentTimeMillis());
//			SimpleDateFormat df = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
//			String formattedDate = df.format(now);
//
//			fw.write(formattedDate+"] "+str+"\r\n");
//			fw.flush();
//			fw.close();
//		}catch(Exception e){
//			e.printStackTrace();
//		}
	}
	public static void v(String tag, String log) {
		if (isVerboseEnable) {;
			Log.v(tag, log);
			writeLogFile(tag+"] = "+log);
		}
	}
	
	public static void v(String tag, String log, Object... param) {
		if (isVerboseEnable) {
			Log.v(tag, String.format(log, param));
		}
	}
	
	public static void v(String tag, String log, Throwable t) {
		if (isVerboseEnable) {
			Log.v(tag, log, t);
		}
	}
	
	public static void d(String tag, String log) {
		if (isDebugEnable) {
			Log.d(tag, log);
			writeLogFile(tag + "] = " + log);
		}
	}
	
	public static void d(String tag, String log, Object... param) {
		if (isDebugEnable) {
			Log.d(tag, String.format(log, param));
		}
	}
	
	public static void d(String tag, String log, Throwable t) {
		if (isDebugEnable) {
			Log.d(tag, log, t);
		}
	}
	
	public static void i(String tag, String log) {
		if (isInfoEnable) {
			Log.i(tag, log);
			writeLogFile(tag + "] = " + log);
		}
	}
	
	public static void i(String tag, String log, Object... param) {
		if (isInfoEnable) {
			Log.i(tag, String.format(log, param));
		}
	}
	
	public static void i(String tag, String log, Throwable t) {
		if (isInfoEnable) {
			Log.i(tag, log, t);
		}
	}
	
	public static void w(String tag, String log) {
		if (isWarnEnable) {
			Log.w(tag, log);
			writeLogFile(tag + "] = " + log);
		}
	}
	
	public static void w(String tag, String log, Object... param) {
		if (isWarnEnable) {
			Log.w(tag, String.format(log, param));
		}
	}
	
	public static void w(String tag, String log, Throwable t) {
		if (isWarnEnable) {
			Log.w(tag, log, t);
		}
	}
	
	public static void e(String tag, String log) {
		if (isErrorEnable) {
			Log.e(tag, log);
			writeLogFile(tag + "] = " + log);
		}
	}
	
	public static void e(String tag, String log, Object... param) {
		if (isErrorEnable) {
			Log.e(tag, String.format(log, param));
		}
	}
	
	public static void e(String tag, String log, Throwable t) {
		if (isErrorEnable) {
			Log.e(tag, log, t);
		}
	}
}
