package com.beatsportable.beats;

import java.util.HashMap;

import android.util.Log;

import com.localytics.android.*;

public class ToolsTracker {
	private static String appVersion = "";
	private static LocalyticsSession localyticsSession = null;
	
	private static final String sanitize(String s) {
		return s.replace(".", " ").replace("-", " ").replace("_", " ");
	}
	
	// Called by Tools.setContext
	public static void setupTracker() {
		try {
			if (localyticsSession == null) {
				localyticsSession = new LocalyticsSession(Tools.c, Tools.res.getString(R.string.Localytics_key));
				localyticsSession.open();
				appVersion = Tools.res.getString(R.string.App_version);
				appVersion += " ";
				appVersion = sanitize(appVersion);
			}
			localyticsSession.upload();
		} catch (Exception e) {}
	}
	
	// Called by MenuHome.onDestroy
	public static void stopTracking() {
		try {
			if (localyticsSession != null) {
				localyticsSession.close();
			}
		} catch (Exception e) {}
	}
	
	private static void track(String event) {
		try {
			if (localyticsSession != null) {
				localyticsSession.tagEvent(sanitize(event));
			}
		} catch (Exception e) {}
	}
	
	private static void trackAttributes(String event, HashMap<String,String> attributes) {
		try {
			if (localyticsSession != null) {
				localyticsSession.tagEvent(sanitize(event), attributes);
			}
		} catch (Exception e) {}
	}
	
	private static void trackAttribute(String event, String attribute, String value) {
		HashMap<String,String> attributes = new HashMap<String,String>();
		attributes.put(attribute, value);
		trackAttributes(event, attributes);
	}

	public static void info(String event) {
		track(appVersion + "Info: " + event);
	}
	
	public static void error(String event, Throwable e, String value) {
		// logcat this
		String ev = appVersion + "Error: " + event;
		String atr = e.getMessage();
		String val = value;
		trackAttribute(ev, atr, val);
		Log.e("Beats exception:", ev + " / " + atr + " / " + val);
	}
	
	public static void data(String event, String attribute, String value) {
		trackAttribute(appVersion + "Data: " + event, attribute, value);
	}
	
	public static void data(String event, HashMap<String,String> attributes) {
		trackAttributes(appVersion + "Data: " + event, attributes);
	}
	
}
