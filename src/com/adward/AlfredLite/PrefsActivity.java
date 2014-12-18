package com.adward.AlfredLite;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.Window;

public final class PrefsActivity extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_LEFT_ICON); 
		super.onCreate(savedInstanceState);
        getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.dialog_preferences);
        addPreferencesFromResource(R.xml.preferences);
	}
}
