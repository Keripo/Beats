package com.beatsportable.beats;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import android.app.*;
import android.content.*;
import android.os.Handler;
import android.os.Message;

public class ToolsUnzipper implements Runnable {
	
	private Activity a; // Context
	private String file, filename;
	private boolean sampleInstall;
	private boolean finishCallingActivity;
	
	private String returnMsg;
	private boolean success;
	private ProgressDialog extractingBar;
	
	// Lazy hack for ToolsUnzipper
	private void finishCallingActivity() {
		if (finishCallingActivity) {
			a.finish();
		}
	}
	
	private DialogInterface.OnClickListener cancel_action = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int id) {
			dialog.cancel();
			finishCallingActivity();
		}
	};
	
	public ToolsUnzipper(Activity a, String file, boolean sampleInstall, boolean finishCallingActivity) {
		this.a = a;
		this.file = file;
		if (file.lastIndexOf('/') != -1) {
			this.filename = file.substring(file.lastIndexOf('/') + 1);
		} else {
			this.filename = file;
		}
		this.sampleInstall = sampleInstall;
		this.finishCallingActivity = finishCallingActivity;
		
		returnMsg = "";
		success = false;
		extractingBar = null;
	}
	
	public ToolsUnzipper(Activity a, String file, boolean sampleInstall) {
		this(a, file, sampleInstall, false);
	}
	
	public ToolsUnzipper(Activity a, String file) {
		this(a, file, false, false);
	}
	
	private void checkFolder(String path) {
		if (path.indexOf('/') != -1) {
			File dir = new File(path.substring(0, path.lastIndexOf('/')));
			if (!dir.exists()) {
				dir.mkdirs();
			}
		}
	}
	
	public void unzip() {
		if (!(new File(file).canRead())){
			Tools.error(
					Tools.getString(R.string.ToolsUnzipper_unable_read) +
					filename + 
					Tools.getString(R.string.ToolsUnzipper_just_deleted),
					cancel_action
					);
			return;
		}
		
		if (sampleInstall) {
			unzipFile();
			return;
		}
		
		DialogInterface.OnClickListener unzip_action = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				ToolsTracker.info("Unzip song pack");
				unzipFileSizeCheck();
				dialog.cancel();
			}
		};
		
		Tools.alert(
				Tools.getString(R.string.Button_install),
				R.drawable.icon_zip,
				Tools.getString(R.string.ToolsUnzipper_install_ask) +
				filename +
				Tools.getString(R.string.ToolsUnzipper_install_ask_location) +
				Tools.getSongsDir(),
				Tools.getString(R.string.Button_yes),
				unzip_action,
				Tools.getString(R.string.Button_no),
				cancel_action,
				-1
				);
	}
	
	private void unzipFileSizeCheck() {
		if (!Tools.getBooleanSetting(R.string.ignoreUnzipSizeWarning, R.string.ignoreUnzipSizeWarningDefault) &&
			new File(file).length() > 100000000) { // 100 MB
			ToolsTracker.info("Unzip large song pack");
			DialogInterface.OnClickListener unzip_action = new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					unzipFile();
					dialog.cancel();
				}
			};
			
			Tools.warning(
					Tools.getString(R.string.ToolsUnzipper_songpack) + 
					filename +
					Tools.getString(R.string.ToolsUnzipper_size_ask),
					unzip_action,
					cancel_action,
					R.string.ignoreUnzipSizeWarning
					);
		} else {
			unzipFile();
		}
	}
	
	// Extraction code from http://java.sun.com/developer/technicalArticles/Programming/compression/
	private void extract(
		String path, ZipFile zipfile, BufferedInputStream is, BufferedOutputStream dest, ZipEntry entry) 
		throws IOException
	{
		
		checkFolder(path);
		if (entry.isDirectory()) {
			new File(path).mkdirs();
		} else {
			is = new BufferedInputStream(zipfile.getInputStream(entry));
			int count;
			byte data[] = new byte[Tools.BUFFER];
			FileOutputStream fos = new FileOutputStream(path);
			dest = new BufferedOutputStream(fos, Tools.BUFFER);
			while ((count = is.read(data, 0, Tools.BUFFER)) != -1) {
				dest.write(data, 0, count);
			}
			dest.flush();
			dest.close();
			is.close();
		}
	}
	
	public void run() {
		try {
			BufferedOutputStream dest = null;
			BufferedInputStream is = null;
			ZipEntry entry;
			ZipFile zipfile = new ZipFile(file);
			Enumeration<? extends ZipEntry> e;		
			
			if (sampleInstall) {
				e = zipfile.entries();
				while (e.hasMoreElements()) {
					entry = (ZipEntry) e.nextElement();
					String path = Tools.getBeatsDir() + "/" + entry.getName();
					extract(path, zipfile, is, dest, entry);	
					extractingBar.incrementProgressBy(1);
				}
				success = true;
				extracthandler.sendEmptyMessage(0);
				return;
			}
			
			boolean hasSongsFolder = false;
			boolean hasFolders = false;
			boolean hasSingleFolder = false;
			boolean hasStepfile = false;
			boolean isOSZFile = Tools.isOSZ(file);
			
			// Setup check
			e = zipfile.entries();
			extractingBar.setMax(zipfile.size());
			while (e.hasMoreElements() && !(hasSongsFolder && hasFolders && hasStepfile)) {
				entry = (ZipEntry) e.nextElement();
				String name = entry.getName();
				if (name.startsWith("Songs/")) {
					hasSongsFolder = true;
				}
				if (name.indexOf('/') != name.lastIndexOf('/')) { // has two layers of directories
					hasFolders = true;
					hasSingleFolder = true;
				} else if (name.indexOf('/') != -1) {
					hasSingleFolder = true;
				}
				if (Tools.isStepfile(name)) {
					hasStepfile = true;
				}
				if (Tools.isOSUFile(name)) {
					isOSZFile = true;
				}
			}
			if (!hasStepfile) {
				// No stepfiles ;<
				returnMsg =
					Tools.getString(R.string.ToolsUnzipper_unable_install) +
					filename +
					Tools.getString(R.string.Tools_error_msg) +
					Tools.getString(R.string.ToolsUnzipper_error_no_stepfiles)
					;
				success = false;
			} else if (hasSongsFolder) { // Yay, nice .smzip!
				e = zipfile.entries();
				while (e.hasMoreElements()) {
					entry = (ZipEntry) e.nextElement();
					if (entry.getName().startsWith("Songs/")) {
						String path = Tools.getBeatsDir() + "/" + entry.getName();
						extract(path, zipfile, is, dest, entry);						
					}
					extractingBar.incrementProgressBy(1);
				}
				returnMsg = 
					Tools.getString(R.string.ToolsUnzipper_songpack) +
					filename + 
					Tools.getString(R.string.ToolsUnzipper_installed_to) +
					Tools.getSongsDir()
					;
				success = true;
			} else if (!hasFolders || isOSZFile) { // No album or is an osz
				e = zipfile.entries();
				File dir;
				String folderName = filename;
				String singlesDir;
				if (isOSZFile) {
					singlesDir = Tools.getOSUDir();
				} else {
					singlesDir = Tools.getSinglesDir();
				}
				if (folderName.indexOf('.') != -1) {
					folderName = folderName.substring(0, folderName.lastIndexOf("."));
				}
				if (hasSingleFolder && !isOSZFile) { // Song name folder probably
					dir = new File(singlesDir);
				} else {
					dir = new File(singlesDir + "/" + folderName);
				}
				dir.mkdirs();
				while (e.hasMoreElements()) {
					entry = (ZipEntry) e.nextElement();
					String path;
					if (hasSingleFolder && !isOSZFile) {
						path = singlesDir + "/" + entry.getName();
					} else {
						path = singlesDir + "/" + folderName + "/" + entry.getName();
					}
					extract(path, zipfile, is, dest, entry);
					extractingBar.incrementProgressBy(1);
				}
				returnMsg =
					Tools.getString(R.string.ToolsUnzipper_songpack) +
					filename + 
					Tools.getString(R.string.ToolsUnzipper_installed_to) +
					singlesDir
					;
				success = true;
			} else { // hasFolders - standard non-.smzip
				e = zipfile.entries();
				while (e.hasMoreElements()) {
					entry = (ZipEntry) e.nextElement();
					String path = Tools.getSongsDir() + "/" + entry.getName();
					extract(path, zipfile, is, dest, entry);
					extractingBar.incrementProgressBy(1);
				}
				returnMsg =
					Tools.getString(R.string.ToolsUnzipper_songpack) +
					filename + 
					Tools.getString(R.string.ToolsUnzipper_installed_to) +
					Tools.getSongsDir()
					;
				success = true;
			}
		} catch(IOException e) {
			ToolsTracker.error("ToolsUnzipper.run", e, file);
			returnMsg = 
				Tools.getString(R.string.ToolsUnzipper_unable_install) +
				filename +
				Tools.getString(R.string.Tools_error_msg) +
				e.getMessage()
				;
			success = false;
		}
		extracthandler.sendEmptyMessage(0);
	}
	
	private Handler extracthandler = new Handler() {
		public void handleMessage(Message msg) {
			try {
				if (extractingBar != null && extractingBar.isShowing()) extractingBar.dismiss();
			} catch (IllegalArgumentException e) {
				ToolsTracker.error("ToolsUnzipper.handleMessage", e, file);
				if (Tools.getBooleanSetting(R.string.debugLogCat, R.string.debugLogCatDefault)) {
					Tools.toast(Tools.getString(R.string.Tools_window_error));
				}
			}
			// Finish dialog
			if (success) {
				File del = new File(file);
				try {
					if (!del.delete()) {
						throw new Exception(
								Tools.getString(R.string.Tools_permissions_error)
								);
					}
				} catch (Exception e) {
					ToolsTracker.error("ToolsUnzipper.del", e, file);
					Tools.error(
							Tools.getString(R.string.Tools_unable_delete_file) + 
							filename + 
							Tools.getString(R.string.Tools_error_msg) +
							e.getMessage(),
							cancel_action
							);
				}
			}
			if (success) {
				if (!sampleInstall) {
				Tools.alert(
						Tools.getString(R.string.Button_success),
						R.drawable.icon_success,
						returnMsg,
						Tools.getString(R.string.Button_ok),
						cancel_action,
						null,
						null,
						-1
						);
				}
			} else {
				Tools.error(
						returnMsg,
						cancel_action
						);
			}
			sampleInstall = false;
		}
	};
	
	private void unzipFile() {
		extractingBar =	new ProgressDialog(a);
		extractingBar.setCancelable(false);
		extractingBar.setMessage(
				Tools.getString(R.string.ToolsUnzipper_installing) + 
				filename + 
				Tools.getString(R.string.ToolsUnzipper_please_wait)
				);
		extractingBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		extractingBar.setProgress(0);
		extractingBar.setOwnerActivity(a);
		extractingBar.show();

		new Thread(this).start();
	}

}
