package ca.surveillancerights.surveillancewatch;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

public class WelcomeActivity extends Activity {

	static final int SET_PREFERENCES = 0;

	WebView welcomeHtml;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.welcome);
		
		//((TextView) findViewById(R.id.WelcomeText)).setMovementMethod(new ScrollingMovementMethod());
		welcomeHtml = (WebView) findViewById(R.id.welcome_html);
		welcomeHtml.setBackgroundColor(0x00000000);
		welcomeHtml.loadUrl("file:///android_asset/www/welcome.html");
	}

	public void onShowMapClick(View view) {
		Intent intent = new Intent(this, BrowserActivity.class);
		intent.putExtra("page", "overview-map.html");
		startActivity(intent);
	}

	public void onViewListClick(View view) {
		Intent intent = new Intent(this, BrowserActivity.class);
		intent.putExtra("page", "installations-list.html");
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.surveillancewatch, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		Log.v(this.getClass().getName(),
				"Menu item selected: " + item.toString() + " ("
						+ item.getItemId() + ")");
//		if (item.getItemId() == R.id.settings) {
//			Intent prefsActivity = new Intent(getBaseContext(),
//					SurveillanceWatchSettings.class);
//			startActivityForResult(prefsActivity,
//					SurveillanceWatchShell.SET_PREFERENCES);
//			return true;
//		} else
		if (item.getItemId() == R.id.privacypolicy) {
			loadPrivacyPolicy(item);
			return true;
		} else if (item.getItemId() == R.id.termsofuse) {
			loadTermsOfUse(item);
			return true;
		} else if (item.getItemId() == R.id.help) {
			loadHelp(item);
			return true;
		} else if (item.getItemId() == R.id.backtoapp) {
			loadWelcome(item);
			return true;
		} else {
			return false;
		}
	}

	/*
	 * @Override public boolean onKeyDown(int i,KeyEvent e){ return false; }
	 */

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		if (requestCode == SET_PREFERENCES) {
			if (resultCode == RESULT_OK) {
				super.onActivityResult(requestCode, resultCode, intent);
				//this.loadUrl(this.getVeosUrl()+"/app.html#/overview-map.html");
			}
		} else {
			super.onActivityResult(requestCode, resultCode, intent);
		}
	}

	public void reload(MenuItem item) {
		Log.d("PhoneGapShell", "Deleting cache...");
		this.getCacheDir().delete();
		//this.appView.clearCache(true);
		//this.loadUrl(this.appView.getOriginalUrl());
	}
	
	public void loadPrivacyPolicy(MenuItem item) {
		Log.d("PhoneGapShell", "Loading Privacy Policy ...");
		
		welcomeHtml.loadUrl("http://surveillancerights.ca/privacypolicy.html#app_privacy");
	}
	
	public void loadTermsOfUse(MenuItem item) {
		Log.d("PhoneGapShell", "Loading Terms of Use ...");
		
		welcomeHtml.loadUrl("http://surveillancerights.ca/termsofuse.html");
	}
	
	public void loadHelp(MenuItem item) {
		Log.d("PhoneGapShell", "Loading Help ...");
		
		welcomeHtml.loadUrl("http://surveillancerights.ca/app.html");
	}
	
	public void loadWelcome(MenuItem item) {
		Log.d("PhoneGapShell", "Loading map ...");
		
		welcomeHtml.loadUrl("file:///android_asset/www/welcome.html");
	}
	
	
}
