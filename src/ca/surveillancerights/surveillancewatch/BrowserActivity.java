package ca.surveillancerights.surveillancewatch;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class BrowserActivity extends Activity {
	
	final static String DEFAULT_VEOS_URL = "http://mobile.watch.surveillancerights.ca/";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.browser);
		
		//((TextView) findViewById(R.id.WelcomeText)).setMovementMethod(new ScrollingMovementMethod());
		WebView browser = (WebView) findViewById(R.id.browser);
		//browser.setBackgroundColor(0x00000000);
		
		WebSettings browserSettings = browser.getSettings();
		browserSettings.setJavaScriptEnabled(true);
		browserSettings.setGeolocationEnabled(true);
		
		String url = getVeosUrl()+"/app.html#/overview-map.html";
		Log.v(BrowserActivity.class.getName(), "Loading: "+url);
		browser.loadUrl(url);
	}
	
	public String getVeosUrl() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);

		String defaultAppUrl = DEFAULT_VEOS_URL;
		//String defaultAppUrl = "http://mobile.dev.surveillancerights.ca";

		String appUrl = prefs.getString("app_url", defaultAppUrl);
		if (appUrl.length() == 0) // make sure that the URL isn't blank
			appUrl = defaultAppUrl;

		if (appUrl.length() == 0) // make sure that it still isn't blank (in
									// case defaultSailAppUrl was hosed)
			appUrl = "file:///android_asset/www/index.html";

		if (appUrl.endsWith("/"))
			appUrl = appUrl.replaceFirst("/$", "");
		
		return appUrl;
	}
}
