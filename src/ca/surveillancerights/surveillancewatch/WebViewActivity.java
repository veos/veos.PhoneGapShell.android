package ca.surveillancerights.surveillancewatch;

import android.app.Activity;
import android.app.ProgressDialog;
import android.webkit.WebView;
import android.webkit.WebViewClient;

abstract public class WebViewActivity extends Activity {

	abstract public WebView getMainWebView();
	
	protected ProgressDialog loaderDialog;
	
	protected void setupMainWebView() {
		getMainWebView().setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished(WebView view, String url) {
				if (loaderDialog != null)
					loaderDialog.dismiss();
			}
		});
	};
	
	public void showLoader(String loaderText) {
		loaderDialog = ProgressDialog.show(this, "", loaderText, true);
	}
}
