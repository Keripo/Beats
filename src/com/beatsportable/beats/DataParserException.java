package com.beatsportable.beats;

import android.util.Log;

public class DataParserException extends Exception {
	private static final long serialVersionUID = 1L;
	
	public DataParserException(String msg) {
		super(msg);
	}
	
	public DataParserException(String msg, Throwable tr) {
		super(msg);
		Log.e("DataParserException Stack Trace", Log.getStackTraceString(tr));
	}
	
	public DataParserException(String exceptionName, String msg, Throwable tr) {
		super(exceptionName + ": " + msg);
		Log.e("DataParserException Stack Trace", Log.getStackTraceString(tr));
	}
	
	/*
	public DataParserException(String exceptionName, int index, int size, String method, int b) {
		super(exceptionName + ": " + "index " + index + " is outside range (" + (size - 1) + "), occured in method " + method + ".");
	}
	*/
	
	public String getMessage() {
		return super.getMessage();
	}
}
