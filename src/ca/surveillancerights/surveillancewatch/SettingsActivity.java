package ca.surveillancerights.surveillancewatch;

import ca.surveillancerights.surveillancewatch.R;
import android.os.Bundle;
import android.preference.PreferenceActivity;


public class SettingsActivity extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences_screen);
		setResult(RESULT_OK);
	}

}
