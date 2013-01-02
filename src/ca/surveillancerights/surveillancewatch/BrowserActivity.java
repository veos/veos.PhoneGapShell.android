package ca.surveillancerights.surveillancewatch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

public class BrowserActivity extends WebViewActivity {

	final static String DEFAULT_VEOS_URL = "http://mobile.watch.surveillancerights.ca/";
	//final static String DEFAULT_VEOS_URL = "http://mobile.dev.surveillancerights.ca/";
	//final static String DEFAULT_VEOS_URL = "http://192.168.43.221:8000/";
	//final static String DEFAULT_VEOS_URL = "http://10.2.1.79:8000";

	private static final int GET_PHOTO_FROM_CAMERA = 0;
	private static final int GET_PHOTO_FROM_GALLERY = 1;
	
	private static final int MAX_PHOTO_WIDTH = 720;
	
	private WebView browser;
	
	private String lastPhotoLocalFilename;
	private Uri lastPhotoRemoteUri;
	private String captureCallback;
	private Thread uploadThread;

	private static final String photoFolderPath = Environment
			.getExternalStorageDirectory().getAbsolutePath()
			+ "/SurveillanceWatch";
	private static final String photoFilePath = "last_photo.jpg";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (Build.VERSION.SDK_INT < 11) // hide action bar for 2.x since we have a menu button for those 
			this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.browser);

		// ((TextView) findViewById(R.id.WelcomeText)).setMovementMethod(new
		// ScrollingMovementMethod());
		browser = (WebView) findViewById(R.id.browser);
		// browser.setBackgroundColor(0x00000000);
		
		setupMainWebView();
		
		browser.setPadding(0, 0, 0, 0);

		WebSettings browserSettings = browser.getSettings();
		browserSettings.setJavaScriptEnabled(true);
		browserSettings.setGeolocationEnabled(true);
//		browserSettings.setCacheMode(WebSettings.LOAD_NO_CACHE); // disable this
//																	// for
//																	// production!

		// geolocation database is disabled for now... slower lookups, but maybe
		// better privacy?
		// browserSettings.setGeolocationDatabasePath("/data/data/SurveillanceRights");

		Intent intent = getIntent();
		
		String loaderText;
		String loadPage = intent.getExtras().getString("page");
		if (loadPage.equals("overview-map.html")) {
			loaderText = "Loading map...";
		} else if (loadPage.equals("installations-list.html")) {
			loaderText = "Loading list...";
		} else {
			loaderText = "Loading data...";
		}
		
		showLoader(loaderText);
		
		// always grant permission when WebView requests geolocation
		browser.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onGeolocationPermissionsShowPrompt(String origin,
					GeolocationPermissions.Callback callback) {
				callback.invoke(origin, true, false);
			}
		});
		

		// add interface for executing functions in this activity in javascript
		browser.addJavascriptInterface(this, "Android");

		String url = getVeosUrl() + "/app.html#/"+loadPage;
		Log.v(BrowserActivity.class.getName(), "Loading: " + url);
		
		
		browser.loadUrl(url);
	}

	public String getVeosUrl() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);

		String defaultAppUrl = DEFAULT_VEOS_URL;
		// String defaultAppUrl = "http://mobile.dev.surveillancerights.ca";

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

	public void getPhotoFromCamera(final String toUrl, final String callback) {
		File folder = new File(photoFolderPath);
		boolean success = false;
		if (!folder.exists()) {
			success = folder.mkdir();
		}
		// TODO: check that folder exists / was created successfully

		Uri filePathUri = Uri.fromFile(new File(photoFolderPath, photoFilePath));
		
		Intent intent = new Intent(
				MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, filePathUri);
		
		// FIXME: can't seem to pass Intent extras to onActivityResult;
		//			setting this flag causes crash... not sure why... using instance variables instead for now
		//intent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
		
		this.lastPhotoLocalFilename = filePathUri.getSchemeSpecificPart();
		this.lastPhotoRemoteUri = Uri.parse(toUrl);
		this.captureCallback = callback;
		
		startActivityForResult(intent, GET_PHOTO_FROM_CAMERA);
	}
	
	public void getPhotoFromGallery(final String toUrl, final String callback) {
		File folder = new File(photoFolderPath);
		boolean success = false;
		if (!folder.exists()) {
			success = folder.mkdir();
		}
		// TODO: check that folder exists / was created successfully

		Intent intent = new Intent(Intent.ACTION_PICK,
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		
		// FIXME: can't seem to pass Intent extras to onActivityResult;
		//			setting this flag causes crash... not sure why... using instance variables instead for now
		//intent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
		
		this.lastPhotoRemoteUri = Uri.parse(toUrl);
		this.captureCallback = callback;
		
		startActivityForResult(intent, GET_PHOTO_FROM_GALLERY);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case GET_PHOTO_FROM_CAMERA:
			if (resultCode == Activity.RESULT_OK) {
				// Do whatever you need to do when the camera returns
				// This is after the picture is already saved, we return to the
				// page
				Log.v("BrowserActivity", "Got photo! URI is: "+lastPhotoLocalFilename);
				browser.loadUrl("javascript:"+this.captureCallback+"();");
				Log.v("BrowserActivity", "Executed js callback "+this.captureCallback);
				uploadThread = new Thread() {
					public void run() {
						uploadPhoto(lastPhotoRemoteUri, lastPhotoLocalFilename);
					}
				};
				uploadThread.start();
			}
			break;
		case GET_PHOTO_FROM_GALLERY:
			if (resultCode == Activity.RESULT_OK) {
				
				// weirdness necessary to convert gallery activity URL into something we can load a File from
				String[] projection = { MediaStore.Images.Media.DATA };
			    Cursor cursor = managedQuery(data.getData(), projection, null, null, null);
			    startManagingCursor(cursor);
			    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			    cursor.moveToFirst();
			    this.lastPhotoLocalFilename = cursor.getString(column_index);
			    
				Log.v("BrowserActivity", "Got photo! URI is: "+lastPhotoLocalFilename);
			    
				final String captureCallback = this.captureCallback;
				
				uploadThread = new Thread() {
					public void run() {
						browser.loadUrl("javascript:"+captureCallback+"();");
						Log.v("BrowserActivity", "Executed js callback "+captureCallback);
						
						uploadPhoto(lastPhotoRemoteUri, lastPhotoLocalFilename);
					}
				};
				uploadThread.start();
			}
			break;
		default:
			Log.v("Camera", "Something strange happened... Activity request code result was: "+requestCode);
			break;
		}
	}
	
	public void viewPhoto(String url) {
		Log.v("BrowserActivity", "showing photo with url "+url);
		Intent intent = new Intent(this, PhotoViewActivity.class);
		intent.putExtra("imageUrl", url);
		intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT); // bring to front if already running
		startActivity(intent);
	}
	
	// TODO: move this out of here
	private void scalePhoto(String filename) {
		Bitmap photo, scaledPhoto;
		try {
			photo = BitmapFactory.decodeStream(new FileInputStream(filename));
			int scaledWidth, scaledHeight;
			float scaleFactor;
			
			scaleFactor = photo.getWidth() / (float) MAX_PHOTO_WIDTH;
			
			Log.v("BrowserActivity", "Will reduce photo size by factor of "+scaleFactor);
			
			scaledWidth = (int) (photo.getWidth() / scaleFactor);
			scaledHeight = (int) (photo.getHeight() / scaleFactor);
			
			scaledPhoto = Bitmap.createScaledBitmap(photo, scaledWidth, scaledHeight, true);
			
			scaledPhoto.compress(CompressFormat.JPEG, 70, new FileOutputStream(filename));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// TODO: move most of this out of here
	private void uploadPhoto(Uri toUrl, String fromFilename) {
		browser.loadUrl("javascript:androidUploadStart();");
		
		scalePhoto(fromFilename);
		
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(toUrl.toString());
		/*.getSchemeSpecificPart())*/
		FileBody image = new FileBody(new File(fromFilename), "image/jpeg");
		//StringBody comment = new StringBody("Filename: " + fromUrl.toString());

		MultipartEntity reqEntity = new MultipartEntity();
		reqEntity.addPart("photo[image]", image);
		//eqEntity.addPart("comment", comment);
		httppost.setEntity(reqEntity);

		HttpResponse response;
		try {
			response = httpclient.execute(httppost);
			HttpEntity resEntity = response.getEntity();
			
			BufferedReader br = new BufferedReader(
                    new InputStreamReader((response.getEntity().getContent())));
			
			String body = "";
			String line;
			while ((line = br.readLine()) != null) {
				body += line;
			}
			
			JSONObject json = null;
			try {
				json = new JSONObject(body);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block 
				Log.e("uploadPhoto", "Failed to parse JSON response: "+body);
				e1.printStackTrace();
			}
			Integer photoId;
			try {
				photoId = json.getInt("id");
				browser.loadUrl("javascript:window.androidUploadSuccess("+photoId.toString()+");");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				Log.e("uploadPhoto", "JSON response did not have an id: "+json.toString());
				e.printStackTrace();
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			browser.loadUrl("javascript:window.androidUploadError();");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			browser.loadUrl("javascript:window.androidUploadError();");
		}
	}
	
	public void showToast(String toast) {
        Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return OptionsMenu.create(this, menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		return OptionsMenu.selectItem(this, featureId, item);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// Check if the key event was the Back button and if there's history
	    if ((keyCode == KeyEvent.KEYCODE_BACK) && browser.canGoBack()) {
	    	showLoader("Loading...");
	        browser.goBack();
	        return true;
	    }
	    // If it wasn't the Back key or there's no web page history, bubble up to the default
	    // system behavior (probably exit the activity)
	    return super.onKeyDown(keyCode, event);
	}
	
	public WebView getMainWebView() {
		return browser;
	}
}
