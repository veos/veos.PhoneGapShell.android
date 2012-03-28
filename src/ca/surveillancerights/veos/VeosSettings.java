package ca.surveillancerights.veos;

import android.os.Bundle;
import android.preference.PreferenceActivity;


public class VeosSettings extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences_screen);
		setResult(RESULT_OK);
	}

}
