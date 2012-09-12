package ca.surveillancerights.surveillancewatch;

import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.cordova.DroidGap;

import ca.surveillancerights.surveillancewatch.httpd.AssetsHTTPD;
import ca.surveillancerights.surveillancewatch.httpd.NanoHTTPD;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class SurveillanceWatchShell extends DroidGap {
	
	private static final int NANO_HTTPD_PORT = 8765;
	private static final String DEFAULT_APP_URL = "http://localhost:" + NANO_HTTPD_PORT;
	
	private NanoHTTPD server;
	private Handler handler = new Handler();

	static final int SET_PREFERENCES = 0;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// it looks like this is not actually used since we override the default
		// view
		//super.setIntegerProperty("splashscreen", R.drawable.icon);
		// Time in msec to wait before triggering a timeout error when loading
		super.setIntegerProperty("loadUrlTimeoutValue", 20000); // 20 seconds
		// We should probably use this...
		// super.setStringProperty("errorUrl",
		// "file:///android_asset/www/error.html");

		setContentView(R.layout.main);
		// super.loadUrl(getAppUrl());
		
		((TextView) findViewById(R.id.WelcomeText)).setMovementMethod(new ScrollingMovementMethod());
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		try {
			server = new AssetsHTTPD(NANO_HTTPD_PORT, getAssets(), "www");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if (server != null)
			server.stop();
	}

	public void onShowMapClick(View view) {
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

		if (networkInfo != null && networkInfo.isConnected()) {
			// load URL

			// ProgressDialog dialog =
			// ProgressDialog.show(SurveillanceWatchShell.this, "",
			// "Loading map...", true);

			super.setStringProperty("loadingDialog", "Loading map...");
			super.loadUrl(getAppUrl()+"/app.html#/overview-map.html");
		} else {
			// display error
			Toast toast = Toast.makeText(getApplicationContext(),
					"No network connection", Toast.LENGTH_LONG);
			toast.show();
		}
	}

	public void onViewListClick(View view) {
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

		if (networkInfo != null && networkInfo.isConnected()) {
			// load URL

			super.setStringProperty("loadingDialog", "Loading list...");
			super.loadUrl(getAppUrl() + "/app.html#/installations-list.html");
		} else {
			// display error
			Toast toast = Toast.makeText(getApplicationContext(),
					"No network connection", Toast.LENGTH_LONG);
			toast.show();
		}
	}

	public void onAddReportClick(View view) {
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

		if (networkInfo != null && networkInfo.isConnected()) {
			// load URL

			super.setStringProperty("loadingDialog", "Preparing new report...");
			super.loadUrl(getAppUrl() + "/app.html#/report.html");
		} else {
			// display error
			Toast toast = Toast.makeText(getApplicationContext(),
					"No network connection", Toast.LENGTH_LONG);
			toast.show();
		}
	}

	public String getAppUrl() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);

		String defaultAppUrl = DEFAULT_APP_URL;
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
			privacyPolicy(item);
			return true;
		} else if (item.getItemId() == R.id.termsofuse) {
			termsOfUse(item);
			return true;
		} else if (item.getItemId() == R.id.help) {
			help(item);
			return true;
		} else if (item.getItemId() == R.id.backtoapp) {
			backToApp(item);
			return true;
		} else {
			return false;
		}
	}

	/*
	 * @Override public boolean onKeyDown(int i,KeyEvent e){ return false; }
	 */

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (appView != null && appView.canGoBack()) {
				appView.goBack();
				return true;
			} else {
				// FIXME: Unfortunately it is not possible to go back to the
				// welcome screen
				// This is because PhoneGap's loadUrl takes over the layout via
				// setContentView().
				// The right way to deal with this would probably be to make the
				// welcome screen
				// non-native (i.e. put it in assets/www/welcome.html).

				new AlertDialog.Builder(this)
						.setMessage(
								"Are you sure you want to exit SurveillanceWatch?")
						.setCancelable(false)
						.setPositiveButton("Yes",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										SurveillanceWatchShell.this.finish();
									}
								}).setNegativeButton("No", null).show();

				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		if (requestCode == SET_PREFERENCES) {
			if (resultCode == RESULT_OK) {
				super.onActivityResult(requestCode, resultCode, intent);
				this.loadUrl(this.getAppUrl()+"/app.html#/overview-map.html");
			}
		} else {
			super.onActivityResult(requestCode, resultCode, intent);
		}
	}

	public void reload(MenuItem item) {
		Log.d("PhoneGapShell", "Deleting cache...");
		this.getCacheDir().delete();
		this.appView.clearCache(true);
		this.loadUrl(this.appView.getOriginalUrl());
	}
	
	public void privacyPolicy(MenuItem item) {
		Log.d("PhoneGapShell", "Loading Privacy Policy ...");
		
		this.loadUrl("http://surveillancerights.ca/privacypolicy.html#app_privacy");
	}
	
	public void termsOfUse(MenuItem item) {
		Log.d("PhoneGapShell", "Loading Terms of Use ...");
		
		this.loadUrl("http://surveillancerights.ca/termsofuse.html");
	}
	
	public void help(MenuItem item) {
		Log.d("PhoneGapShell", "Loading Help ...");
		
		this.loadUrl("http://surveillancerights.ca/app.html");
	}
	
	public void backToApp(MenuItem item) {
		Log.d("PhoneGapShell", "Loading map ...");
		
		this.loadUrl(getAppUrl()+"/app.html#/overview-map.html");
	}
	
	
}
