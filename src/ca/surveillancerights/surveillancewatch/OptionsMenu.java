package ca.surveillancerights.surveillancewatch;

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
		if (item.getItemId() == R.id.privacypolicy) {
			if (originalAppUrl == null)
				originalAppUrl = act.getMainWebView().getUrl();
			loadPrivacyPolicy(act);
			return true;
		} else if (item.getItemId() == R.id.termsofuse) {
			if (originalAppUrl == null)
				originalAppUrl = act.getMainWebView().getUrl();
			loadTermsOfUse(act);
			return true;
		} else if (item.getItemId() == R.id.help) {
			if (originalAppUrl == null)
				originalAppUrl = act.getMainWebView().getUrl();
			loadHelp(act);
			return true;
		} else if (item.getItemId() == R.id.backtoapp) {
			if (originalAppUrl != null) {
				loadOriginalAppUrl(act);
				originalAppUrl = null;
			} else
				loadWelcome(act);
			return true;
		} else {
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
}
