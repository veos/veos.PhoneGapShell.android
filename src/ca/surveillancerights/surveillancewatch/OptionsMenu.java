package ca.surveillancerights.surveillancewatch;

import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class OptionsMenu {
	// we use this to send the user back when they select on 'backtoapp' menu option
	public static String originalAppUrl;
	
	public static boolean create(WebViewActivity act, Menu menu) {
		MenuInflater inflater = act.getMenuInflater();
		inflater.inflate(R.menu.surveillancewatch, menu);
		return true;
	}
	
	public static boolean selectItem(WebViewActivity act, int featureId, MenuItem item) {
		Log.v(act.getClass().getName(),
				"Menu item selected: " + item.toString() + " ("
						+ item.getItemId() + ")");
//		if (item.getItemId() == R.id.settings) {
//			Intent prefsActivity = new Intent(getBaseContext(),
//					SurveillanceWatchSettings.class);
//			startActivityForResult(prefsActivity,
//					SurveillanceWatchShell.SET_PREFERENCES);
//			return true;
//		} else
		
		switch (item.getItemId()) {
//		case R.id.map:
//			loadMap(act);
//			return true;
//		case R.id.listOfInstallations:
//			loadListOfInstallations(act);
//			return true;
		case R.id.privacypolicy:
			if (originalAppUrl == null)
				originalAppUrl = act.getMainWebView().getUrl();
			loadPrivacyPolicy(act);
			return true;
		case R.id.termsofuse:
			if (originalAppUrl == null)
				originalAppUrl = act.getMainWebView().getUrl();
			loadTermsOfUse(act);
			return true;
		case R.id.help:
			if (originalAppUrl == null)
				originalAppUrl = act.getMainWebView().getUrl();
			loadHelp(act);
			return true;
		case R.id.backtoapp:
			if (originalAppUrl != null) {
				loadOriginalAppUrl(act);
				originalAppUrl = null;
			}
			return true;
		case R.id.sendfeedback:
			Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
			emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"SurveillanceRights@gmail.com"});
			emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "[SurveillanceWatch] ");
			emailIntent.setType("text/plain");
			act.startActivity(Intent.createChooser(emailIntent, "Send mail"));
			return true;
		default:
			return false;
		}
	}
	
	public static void reload(WebViewActivity act) {
		Log.d("PhoneGapShell", "Deleting cache...");
		act.getCacheDir().delete();
		//this.appView.clearCache(true);
		//this.loadUrl(this.appView.getOriginalUrl());
	}
	
	public static void loadPrivacyPolicy(WebViewActivity act) {
		Log.d("PhoneGapShell", "Loading Privacy Policy ...");
		act.showLoader("Loading...");
		act.getMainWebView().loadUrl("http://surveillancerights.ca/privacypolicy.html#app_privacy");
	}
	
	public static void loadTermsOfUse(WebViewActivity act) {
		Log.d("PhoneGapShell", "Loading Terms of Use ...");
		act.showLoader("Loading...");
		act.getMainWebView().loadUrl("http://surveillancerights.ca/termsofuse.html");
	}
	
	public static void loadHelp(WebViewActivity act) {
		Log.d("PhoneGapShell", "Loading Help ...");
		act.showLoader("Loading...");
		act.getMainWebView().loadUrl("http://surveillancerights.ca/app.html");
	}
	
	public static void loadWelcome(WebViewActivity act) {
		Log.d("PhoneGapShell", "Loading map ...");
		act.showLoader("Loading...");
		act.getMainWebView().loadUrl("file:///android_asset/www/welcome.html");
	}
	
	public static void loadOriginalAppUrl(WebViewActivity act) {
		act.showLoader("Loading...");
		act.getMainWebView().loadUrl(originalAppUrl);
	}
	
	public static void loadMap(WebViewActivity act) {
		Intent intent = new Intent(act, BrowserActivity.class);
		intent.putExtra("page", "overview-map.html");
		intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT); // bring to front if already running
		act.startActivity(intent);
	}
	
	public static void loadListOfInstallations(WebViewActivity act) {
		Intent intent = new Intent(act, BrowserActivity.class);
		intent.putExtra("page", "installations-list.html");
		intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT); // bring to front if already running
		act.startActivity(intent);
	}
}
