package com.beatsportable.beats;

import android.os.Bundle;
import android.preference.*;
	
public class MenuSettings extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Tools.setContext(this);
		ToolsTracker.info("Opened settings");
		addPreferencesFromResource(R.xml.settings);
		this.setTitle(Tools.getString(R.string.MenuSettings_title));
	}
}