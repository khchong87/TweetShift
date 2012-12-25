package com.bbs.tweetshift;

import winterwell.jtwitter.Twitter;
import winterwell.jtwitter.TwitterException;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class StatusActivity2 extends Activity implements OnClickListener,
		TextWatcher, OnSharedPreferenceChangeListener {
	private static final String TAG = "StatusActivity";
	EditText editText;
	Button updateButton;
	Twitter twitter;
	TextView textCount;
	private int count;
	SharedPreferences prefs;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.status2);
		// Find views
		editText = (EditText) findViewById(R.id.editText1);
		updateButton = (Button) findViewById(R.id.imageButtonSendTweet);
		textCount = (TextView) findViewById(R.id.textCount);

		textCount.setText(Integer.toString(140));
		textCount.setTextColor(Color.GREEN);

		updateButton.setOnClickListener(this);
		editText.addTextChangedListener(this);

		prefs = PreferenceManager.getDefaultSharedPreferences(this); //
		prefs.registerOnSharedPreferenceChangeListener(this);
	}

	// Asynchronously posts to twitter
	class PostToTwitter extends AsyncTask<String, Integer, String> {

		// Called to initiate the background activity
		@Override
		protected String doInBackground(String... statuses) {
			try {
				TwitterApplication yamba = ((TwitterApplication) getApplication());
				yamba.getTwitter().updateStatus(statuses[0]);
				return "Success";
			} catch (TwitterException e) {
				Log.e(TAG, e.toString());
				e.printStackTrace();
				return "Failed to post";
			}
		}

		// Called when there's a status to be updated
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			// Not used in this case
		}

		// Called once the background activity has completed
		@Override
		protected void onPostExecute(String result) {
			Log.d(TAG, result);
			Toast.makeText(StatusActivity2.this, result, Toast.LENGTH_LONG)
					.show();
			if( "Success".equals(result) ){
				StatusActivity2.this.finish();
			}
		}
	}

	// Called when button is clicked
	public void onClick(View v) {
		String status = editText.getText().toString();
		new PostToTwitter().execute(status); //
		Log.d(TAG, "onClicked");
	}

	@Override
	public void afterTextChanged(Editable statusText) {
		count = 140 - statusText.length(); //
		textCount.setText(Integer.toString(count));
		textCount.setTextColor(Color.GREEN); //
		updateButton.setEnabled(true);
		if (count < 10)
			textCount.setTextColor(Color.YELLOW);
		if (count < 0){
			textCount.setTextColor(Color.RED);
			updateButton.setEnabled(false);
		}
	}

	@Override
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
			int arg3) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub

	}

	// Called first time user clicks on the menu button
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

//	// Called when an options item is clicked
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		switch (item.getItemId()) {
//		case R.id.itemPrefs:
//			startActivity(new Intent(this, PrefsActivity.class));
//			break;
////		case R.id.itemServiceStart:
////			startService(new Intent(this, UpdaterService.class)); 
////			break;
////		case R.id.itemServiceStop:
////			stopService(new Intent(this, UpdaterService.class)); 
////			break;
//		}
//		return true; //
//	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1) {
		twitter = null;
	}

}