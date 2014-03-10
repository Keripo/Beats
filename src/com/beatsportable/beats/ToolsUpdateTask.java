package com.beatsportable.beats;

import java.io.*;
import java.net.*;
import java.util.Scanner;

import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.os.AsyncTask;

public class ToolsUpdateTask extends AsyncTask<String, Void, Boolean> {
	
	private int newVersionNumber = -1;
	private String newVersionName = "";
	private String downloadURL = "";
	
	public void showUpdateDialog() {
		DialogInterface.OnClickListener website_action = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
				Tools.startWebsiteActivity(downloadURL);
			}
		};
		
		Tools.warning(
				String.format(
						Tools.getString(R.string.ToolsUpdateTask_update_ask),
						newVersionName,
						newVersionNumber
						),
				website_action,
				Tools.cancel_action,
				-1
				);
	}

	@Override
	protected Boolean doInBackground(String... updateURL) {
		try {
			// Get current info
			String currentPackageName = Tools.c.getPackageName();
			PackageInfo pi = Tools.c.getPackageManager().getPackageInfo(currentPackageName, 0);
			int currentVersionNumber = pi.versionCode;
			//String currentVersionName = pi.versionName; // not good for development builds
			String path = Tools.getBeatsDir() + Tools.getString(R.string.Tools_path_version);
			
			// TODO - I'm lazy so not gonna bother with update checking logic
			/*
			// Check if updating is needed
			Calendar c = Calendar.getInstance();
			int currentDate = c.get(Calendar.YEAR) * 365 + c.get(Calendar.MONTH) * 30 + c.get(Calendar.DATE); // Approximate value
			int lastUpdate = Integer.parseInt(Tools.getSetting(R.string.updateCheck, R.string.updateCheckDefault));
			if (currentDate - lastUpdate > 7) { // Check every week or so
			*/
				// Download the version check file
				HttpURLConnection urlConnection = (HttpURLConnection) new URL(updateURL[0]).openConnection();
				BufferedInputStream is = new BufferedInputStream(urlConnection.getInputStream());
				BufferedOutputStream dest = new BufferedOutputStream(new FileOutputStream(path), Tools.BUFFER);			
				
				int count;
				byte data[] = new byte[Tools.BUFFER];
				while ((count = is.read(data, 0, Tools.BUFFER)) != -1) {
					dest.write(data, 0, count);
				}
				dest.flush();
				dest.close();
				is.close();
			/*
				Tools.putSetting(R.string.updateCheck, Integer.toString(currentDate));
			}
			*/
			
			// Parse version check file
			/*
			 * Example:
			 * 10
			 * 1.5.3b
			 * market://search?q=pname:com.beatsportable.beats
			 */
			Scanner sc = new Scanner(new File(path));
			newVersionNumber = Integer.parseInt(sc.nextLine().trim());
			newVersionName = sc.nextLine().trim();
			downloadURL = sc.nextLine().trim();
			
			// Tell user to update if different version numbers
			return (currentVersionNumber != newVersionNumber);
		} catch (Exception e) {
			return false; // meh, not gonna bother
		}
	}
	protected void onPostExecute(Boolean newUpdate) {
		if (newUpdate) {
			showUpdateDialog();
		}
	}

}
