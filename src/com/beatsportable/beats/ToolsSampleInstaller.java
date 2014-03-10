package com.beatsportable.beats;

import java.io.FileOutputStream;
import java.io.InputStream;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Message;

public class ToolsSampleInstaller implements Runnable {
	
	private Activity a;
	private String path;
	private int raw;
	private int message;
	private ProgressDialog extractingBar;
	
	private boolean success = false;
	private String errorMsg = "";
	
	public ToolsSampleInstaller(Activity a, String path, int raw, int message) {
		this.a = a;
		this.path = path;
		this.raw = raw;
		this.message = message;
		this.extractingBar = null;
	}
	
	Handler extracthandler;
	public void run() {
		try {
			extractingBar.setProgress(0);
			extractingBar.setMax((int)a.getResources().openRawResourceFd(raw).getLength());
			
			InputStream in = a.getResources().openRawResource(raw);
			FileOutputStream out = new FileOutputStream(path);
			int count;
			int progress = 0;
			byte data[] = new byte[Tools.BUFFER];
			while ((count = in.read(data, 0, Tools.BUFFER)) != -1) {
				out.write(data, 0, count);
				progress += count;
				if (progress >= Tools.BUFFER_LARGE) {
					extractingBar.incrementProgressBy(Tools.BUFFER_LARGE);
					progress -= Tools.BUFFER_LARGE;
				}
			}
			out.flush();
			out.close();
			in.close();
			success = true;
			extracthandler.sendEmptyMessage(0);
		} catch (Exception e) {
			ToolsTracker.error("ToolsSampleInstaller.run", e, path);
			success = false;
			errorMsg = e.getMessage();
			extracthandler.sendEmptyMessage(-1);
		}		
	}
	
	public void extract() {
		/*
		DialogInterface.OnClickListener extract_action = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				Tools.track("Extract samples");
				extractSampleZip();
				dialog.cancel();
			}
		};

		Tools.alert(
				Tools.getString(R.string.Button_install),
				R.drawable.icon_zip,
				Tools.getString(R.string.ToolsUnzipper_install_ask) +
				Tools.getString(R.string.Tools_sample_zip) +
				Tools.getString(R.string.ToolsUnzipper_install_ask_location) +
				Tools.getSongsDir(),
				Tools.getString(R.string.Button_yes),
				extract_action,
				Tools.getString(R.string.Button_no),
				Tools.cancel_action,
				-1
				);
		*/
		extractingBar =	new ProgressDialog(a);
		extractingBar.setCancelable(false);
		extractingBar.setMessage(
				Tools.getString(message)
				);
		extractingBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		extractingBar.setOwnerActivity(a);
		extractingBar.show();
		
		extracthandler = new Handler() {
			public void handleMessage(Message msg) {
				try {
					if (extractingBar != null) extractingBar.dismiss();
				} catch (IllegalArgumentException e) {
					ToolsTracker.error("ToolsSampleInstaller.handleMessage", e, path);
					if (Tools.getBooleanSetting(R.string.debugLogCat, R.string.debugLogCatDefault)) {
						Tools.toast(Tools.getString(R.string.Tools_window_error));
					}
				}
				if (success) {
					new ToolsUnzipper(a, path, true).unzip();
				} else {
					Tools.error(
							errorMsg,
							Tools.cancel_action
							);
				}
			}
		};
		
		// Just extract anyway without prompt
		new Thread(this).start();
	}
}
