package com.beatsportable.beats;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

public class ToolsUnzipperActivity extends Activity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.unzipper);
		Tools.setContext(this);
		
		// Extract
		String filePath;
		Intent extractIntent = getIntent();
		if (extractIntent.getData() != null &&
			(filePath = extractIntent.getData().getPath()) != null// &&
			//Tools.isStepfilePack(filePath)) {
			) {
			this.setTitle(
					Tools.getString(R.string.ToolsUnzipperActivity_install) + 
					extractIntent.getData().getLastPathSegment()
					);
			// Create with finishCallingActivity = true
			new ToolsUnzipper(this, filePath, false, true).unzip();
		} else {
			Tools.toast(
					Tools.getString(R.string.ToolsUnzipperActivity_unsupported)
					);
			finish();
		}
		
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
}