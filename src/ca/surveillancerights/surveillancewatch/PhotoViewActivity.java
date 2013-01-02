package ca.surveillancerights.surveillancewatch;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class PhotoViewActivity extends WebViewActivity {
	
	Thread loadPhotoThread;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (Build.VERSION.SDK_INT < 11) // hide action bar for 2.x since we have a menu button for those 
			this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.photo);
		
		setupMainWebView();
		
		Intent intent = getIntent();
		String photoUrl = intent.getExtras().getString("imageUrl");
		

		WebView photoView =  getMainWebView();
		photoView.setPadding(0, 0, 0, 0);
		WebSettings photoViewSettings = photoView.getSettings();
		photoViewSettings.setSupportZoom(true);
		photoViewSettings.setBuiltInZoomControls(true);

		loadPhoto(photoUrl);
	}
	
	private void loadPhoto(String photoUrl) {
		showLoader("Loading photo: "+photoUrl);
		WebView photoView =  getMainWebView();
		photoView.setBackgroundColor(0x00000000);
		photoView.loadUrl(photoUrl);
	}

	@Override
	public WebView getMainWebView() {
		return (WebView) findViewById(R.id.photoView);
	}
	
	public void back(View v) {
		finish();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return OptionsMenu.create(this, menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		return OptionsMenu.selectItem(this, featureId, item);
	}
}
