package ca.surveillancerights.surveillancewatch;

import android.app.Activity;
import android.app.ProgressDialog;
import android.webkit.WebView;

abstract public class WebViewActivity extends Activity {

	abstract public WebView getMainWebView();
	
	protected ProgressDialog loaderDialog;
	
	public void showLoader(String loaderText) {
		loaderDialog = ProgressDialog.show(this, "", loaderText, true);
	}
}
