package ca.surveillancerights.surveillancewatch;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

public class PhotoViewActivity extends Activity {
	
	Thread loadPhotoThread;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.photo);
		
		Intent intent = getIntent();
		String photoUrl = intent.getExtras().getString("imageUrl");
		
		loadPhoto(photoUrl);
	}
	
	private void loadPhoto(String photoUrl) {
		final URL url;
		try {
			url = new URL(photoUrl);
			loadPhotoThread = new Thread() {
				public void run() {
					final ImageView imageView = (ImageView) findViewById(R.id.photoView);
	
					InputStream content;
					try {
						content = (InputStream) url.getContent();
						final Drawable d = Drawable.createFromStream(content , "src"); 
						runOnUiThread(new Runnable() {
							public void run() {
								imageView.setImageDrawable(d);
							}
						});
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
			};
			loadPhotoThread.start();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
