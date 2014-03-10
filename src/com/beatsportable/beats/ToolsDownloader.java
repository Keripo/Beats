package com.beatsportable.beats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
 
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
 
public class ToolsDownloader extends Activity implements Runnable {
	
	private String url;
	private String path;
	private String errorMessage;
	private ProgressDialog downloadBar;
	private HttpURLConnection urlConnection;
	
	private void downloadFail() {
		DialogInterface.OnClickListener exit_action = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
				finish();
			}
		};
		Tools.error(errorMessage, exit_action);
	}
	
	private void showDownloadBar() {
		downloadBar = new ProgressDialog(this);
		downloadBar.setCancelable(false);
		downloadBar.setMessage(
				Tools.getString(R.string.ToolsDownloader_downloading) + url
				);
		downloadBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		downloadBar.setProgress(0);
		downloadBar.setOwnerActivity(this);
		downloadBar.show();
	}
	
	private void cancelDownloadBar() {
		if (downloadBar != null) downloadBar.dismiss();
	}
	
	private void installDownload() {
		new ToolsUnzipper(this, path, false, true).unzip();
	}
	
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case 0:
					cancelDownloadBar();
					installDownload();
					break;
				default: //case -1:
					cancelDownloadBar();
					downloadFail();
					break;
			}
		}
	};
	
	public void run() {
		try {
			urlConnection = (HttpURLConnection) new URL(url).openConnection();
			BufferedInputStream is = new BufferedInputStream(urlConnection.getInputStream());
			BufferedOutputStream dest = new BufferedOutputStream(new FileOutputStream(path), Tools.BUFFER);			
			
			downloadBar.setMax(urlConnection.getContentLength());
			int count;
			int progress = 0;
			byte data[] = new byte[Tools.BUFFER];
			while ((count = is.read(data, 0, Tools.BUFFER)) != -1) {
				dest.write(data, 0, count);
				progress += count;
				if (progress >= Tools.BUFFER_LARGE) {
					downloadBar.incrementProgressBy(Tools.BUFFER_LARGE);
					progress -= Tools.BUFFER_LARGE;
				}
			}
			dest.flush();
			dest.close();
			is.close();
			handler.sendEmptyMessage(0); // Downloaded
		} catch (Exception e) {
			ToolsTracker.error("ToolsDownloader.run", e, url);
			errorMessage = e.getMessage();
			handler.sendEmptyMessage(-1); // Fail
		}
	}
	
	private void downloadFile() {
		showDownloadBar();
		new Thread(this).start();
	}
	
	private void download() {
		DialogInterface.OnClickListener download_action = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
				downloadFile();
			}
		};
		DialogInterface.OnClickListener exit_action = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
				finish();
			}
		};
		
		Tools.alert(
				Tools.getString(R.string.Button_download),
				R.drawable.icon_url,
				Tools.getString(R.string.ToolsDownloader_download_ask) + url,
				Tools.getString(R.string.Button_yes),
				download_action,
				Tools.getString(R.string.Button_no),
				exit_action,
				-1
				);
	}
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.unzipper);
		Tools.setContext(this);
		
		// Download
		Intent downloadIntent = getIntent();
		Uri data = downloadIntent.getData();
		if (data != null &&
			(url = data.getScheme() + ":" + data.getSchemeSpecificPart()) != null &&
			Tools.isStepfilePack(url))
			{
			this.setTitle(
					Tools.getString(R.string.ToolsDownloader_download) + 
					downloadIntent.getData().getLastPathSegment()
					);
			path = Tools.getBeatsDir() + "/" + downloadIntent.getData().getLastPathSegment();
			ToolsTracker.data("Download song", "url", url);
			download();
		} else {
			errorMessage = Tools.getString(R.string.ToolsDownloader_unsupported);
			downloadFail();
		}
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
}