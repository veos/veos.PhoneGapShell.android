package ca.surveillancerights.surveillancewatch;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class BrowserActivity extends Activity {
	
	final static String DEFAULT_VEOS_URL = "http://mobile.watch.surveillancerights.ca/";
	//final static String DEFAULT_VEOS_URL = "http://192.168.222.114:8000/";
	
	private static final int TAKE_PICTURE = 0;
	
	private static final String photoFolderPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SurveillanceRights";
	private static final String photoFilePath = "last_photo.jpg";
	
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
		
		// geolocation database is disabled for now... slower lookups, but maybe better privacy?
		//browserSettings.setGeolocationDatabasePath("/data/data/SurveillanceRights");
		
		// always grant permission when WebView requests geolocation
		browser.setWebChromeClient(new WebChromeClient() {
			public void onGeolocationPermissionsShowPrompt(String origin,
					GeolocationPermissions.Callback callback) {
				callback.invoke(origin, true, false);
			}
		});
		
		// add Camera interface for taking photos
		browser.addJavascriptInterface(this, "Camera");
		
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
	
	public void takePhoto(final String callback) {
	    Log.v("Camera Plugin", "Starting takePhoto callback");

	    Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
	    //intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(photoFolderPath, photoFilePath)));
	    startActivityForResult(intent, TAKE_PICTURE);
	}
	
	public String getPhotoUri() {
	    return Uri.fromFile(new File(photoFolderPath, photoFilePath)).toString();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);

	    switch (requestCode) {
	        case TAKE_PICTURE:
	            if (resultCode == Activity.RESULT_OK) {
	                //Do whatever you need to do when the camera returns
	                //This is after the picture is already saved, we return to the page
	            	Log.v("BrowserActivity", "Got photo!");
	            }
	            break;
	        default:
	            Log.v("Camera", "Something strange happened...");
	            break;
	    }
	}
}
