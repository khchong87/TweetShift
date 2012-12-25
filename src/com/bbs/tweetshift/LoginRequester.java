package com.bbs.tweetshift;

import oauth.signpost.OAuth;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

public class LoginRequester extends Activity {
	private static final String TAG = "LoginRequester";
	ImageButton loginButton;
	TwitterApplication yamba;
	private ProgressDialog m_ProgressDialog = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loginrequester);
		yamba = (TwitterApplication) this.getApplication();
		loginButton = (ImageButton) findViewById(R.id.imageButtonLogin);
		loginButton.setEnabled(true);
		loginButton.setOnClickListener(new LoginRequesterAction());
		if (yamba.isLoggedIn()) {
			startActivity(new Intent(LoginRequester.this,
					TimelineActivity.class).addFlags(
					Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(
					Intent.FLAG_ACTIVITY_CLEAR_TASK));
			this.finish();
		}
		Intent intent = getIntent();
		if( null != intent){
			Uri uri = intent.getData();
			if (uri != null
					&& uri.getScheme().equals(TwitterApplication.CALLBACK_SCHEME)) {
				Log.d(TAG, "callback: " + uri.getPath());
	
				String verifier = uri.getQueryParameter(OAuth.OAUTH_VERIFIER);
				Log.d(TAG, "verifier: " + verifier);
				m_ProgressDialog = ProgressDialog.show(this,
						"Please wait...", "Logging in...", true);
				yamba.setupAuthorization(verifier, LoginRequester.this, m_ProgressDialog);
			}
		}
	}

	private class LoginRequesterAction implements OnClickListener {

		@Override
		public void onClick(View v) {
			Log.d(TAG, "Trigger check");
			yamba.rerouterAuthorization();
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Uri uri = intent.getData();
		Log.d(TAG, "callback: " + uri.getPath());
		Log.d(TAG, "scheme: " + uri.getScheme());
		if (uri != null
				&& uri.getScheme().equals(TwitterApplication.CALLBACK_SCHEME)) {
			Log.d(TAG, "callback: " + uri.getPath());

			String verifier = uri.getQueryParameter(OAuth.OAUTH_VERIFIER);
			Log.d(TAG, "verifier: " + verifier);

			yamba.setupAuthorization(verifier, LoginRequester.this, m_ProgressDialog);
		}
	}

}
