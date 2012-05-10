package ca.surveillancerights.veos;

import org.apache.cordova.DroidGap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class VeosShell extends DroidGap {
	
	static final int SET_PREFERENCES = 0;
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.loadUrl(getAppUrl());
    }
    
    public String getAppUrl() {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        
    	String defaultAppUrl = "http://mobile.veos.surveillancerights.ca/overview-map.html";
    	
        String appUrl = prefs.getString("app_url", defaultAppUrl);
        if (appUrl.length() == 0) // make sure that the URL isn't blank
        	appUrl = defaultAppUrl;
        
        if (appUrl.length() == 0) // make sure that it still isn't blank (in case defaultSailAppUrl was hosed)
        	appUrl = "file:///android_asset/www/index.html";
        
        return appUrl;
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.veos, menu);
        return true;
    }
    
    
    
    @Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		Log.v(this.getClass().getName(), "Menu item selected: " + item.toString() + " (" + item.getItemId() + ")");
    	/*if (item.getItemId() == R.id.settings) {
    		Intent prefsActivity = new Intent(getBaseContext(), VeosSettings.class);
    		startActivityForResult(prefsActivity, VeosShell.SET_PREFERENCES);
    		return true;
    	} else {
    		return false;
    	}*/
		return false;
	}

	/*@Override
	public boolean onKeyDown(int i,KeyEvent e){
		return false;
	}*/
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	  if (keyCode == KeyEvent.KEYCODE_BACK) {
	    if(appView.canGoBack()){
	       appView.goBack();
	        return true;
	    }
	  }
	  return super.onKeyDown(keyCode, event);
	}

    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == SET_PREFERENCES) {
            if (resultCode == RESULT_OK) {
            	super.onActivityResult(requestCode, resultCode, intent);
        		this.loadUrl(this.getAppUrl());
            }
        } else {
        	super.onActivityResult(requestCode, resultCode, intent);
        }
    }
    
    public void reload(MenuItem item) {
    	Log.d("PhoneGapShell", "Deleting cache...");
    	this.getCacheDir().delete();
    	this.loadUrl(this.appView.getOriginalUrl());
    }
    
}
