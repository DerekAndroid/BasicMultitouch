package com.example.android.common.logger;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DKLog {
	public static int displayLevel = Log.VERBOSE;
	public static final boolean isCompilerLog = true;
	public static final boolean isAppendLog = false;
	public static File logFile = new File("/sdcard/touchlog");
	public static final SimpleDateFormat mDateFormat = new SimpleDateFormat("[MM/dd HH:mm:ss.SSS] ");

	public static void v(String tag, String msg) {
		if (isCompilerLog && Log.VERBOSE >= displayLevel) {
			Log.v(tag, msg);			
		}
		
		if (isAppendLog) {
			appendLog(msg);
		}

	}

	public static void d(String tag, String msg) {
		if (isCompilerLog && Log.DEBUG >= displayLevel) {
			Log.d(tag, msg);
		}
		
		if (isAppendLog) {
			appendLog(msg);
		}		

	}

	public static void i(String tag, String msg) {
		if (isCompilerLog && Log.INFO >= displayLevel) {
			Log.i(tag, msg);
		}
		
		if (isAppendLog) {
			appendLog(msg);
		}		

	}

	public static void w(String tag, String msg) {
		if (isCompilerLog && Log.WARN >= displayLevel) {
			Log.w(tag, msg);
		}
		
		if (isAppendLog) {
			appendLog(msg);
		}		

	}

	public static void e(String tag, String msg) {
		if (isCompilerLog && Log.ERROR >= displayLevel) {
			Log.e(tag, msg);
		}
		
		if (isAppendLog) {
			appendLog(msg);
		}		

	}

	public static void appendLog(String text) {
		if (!logFile.exists()) {
			try {
				logFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			// BufferedWriter for performance, true to set append to file flag
			BufferedWriter buf = new BufferedWriter(
					new FileWriter(logFile,true));
			Date time = new Date();
			buf.append(mDateFormat.format(time) + text);
			buf.newLine();
			buf.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
