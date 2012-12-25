package com.bbs.tweetshift;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import winterwell.jtwitter.Status;
import winterwell.jtwitter.Twitter;
import winterwell.jtwitter.TwitterException;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ads.AdRequest;

public class TwitterApplication extends Application implements
		OnSharedPreferenceChangeListener { //

	public static final String CALLBACK_SCHEME = "x-tweetbearz-scheme";
	public static final String CALLBACK_URL = CALLBACK_SCHEME + "://callback";
	public static final String LATEST_MENTION = "latest_mention";

	private static final String TAG = "TwitterApplication";
	public Map<ImageView, String> imageViewCache = new HashMap<ImageView, String>();
	public Map<String, ImageView> imageViewCacheByUrl = new HashMap<String, ImageView>();
	public Map<String, Bitmap> bitmapCache = new HashMap<String, Bitmap>();
	private SessionAuthenticator sessionAuth;
	// public Twitter twitter; //
	private SharedPreferences prefs;
	public Map<EditText, String> testCache = new HashMap<EditText, String>();
	float pixelInDp;
	float pixelInSp;
	List<UserContainer> followerList;
	List<UserContainer> followingList;
	List<String> statusIdList;
	List<Status> statusUpdateList;
	StatusContainer selectedTweet;
	private AdRequest adRequest;
	// public File cacheDir = new File(getCacheDir(), "Yamba");
	private boolean serviceRunning;
	private Activity currentActivity;

	@Override
	public void onCreate() { //
		super.onCreate();
		this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
		this.prefs.registerOnSharedPreferenceChangeListener(this);
		Log.d(TAG, "onCreated");
		// Resources r = getResources();
		// pixelInDp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
		// 60, r.getDisplayMetrics());
		// pixelInSp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 60,
		// r.getDisplayMetrics());
		sessionAuth = new SessionAuthenticator(this);
		// getTwitter();
		adRequest = new AdRequest();

	}

	@Override
	public void onTerminate() { //
		super.onTerminate();
		Log.d(TAG, "onTerminated");
		stopService(new Intent(this, UpdaterService.class));
	}

	public synchronized Twitter getTwitter() {
		return sessionAuth.getTwitter();
	}

	public void setupAuthorization(String pin, Activity context,
			ProgressDialog progressDialog) {
		sessionAuth.setupAuthorization(pin, context, progressDialog);
	}

	public void rerouterAuthorization() {
		sessionAuth.rerouteAuthorization();
	}

	public void isValidLogin(Activity context) {
		sessionAuth.validateLogin(context, null);
	}

	public boolean isLoggedIn() {
		return sessionAuth.isLoggedIn();
	}

	public void setFollowerList(List<UserContainer> userList) {
		followerList = userList;
	}

	public List<UserContainer> getFollowerList() {
		return followerList;
	}

	public void setFollowingList(List<UserContainer> userList) {
		followingList = userList;
	}

	public List<UserContainer> getFollowingList() {
		return followingList;
	}

	public void setSelectedTweet(StatusContainer selectedTweet) {
		this.selectedTweet = selectedTweet;
	}

	public StatusContainer getSelectedTweet() {
		return selectedTweet;
	}

	public void logout() {
		sessionAuth.logout();
	}

	public SharedPreferences getPrefs() {
		return prefs;
	}

	public boolean isServiceRunning() { //
		return serviceRunning;
	}

	public void setServiceRunning(boolean serviceRunning) { //
		this.serviceRunning = serviceRunning;
	}

	public Activity getCurrentActivity() {
		return currentActivity;
	}

	public void setCurrentActivity(Activity currentActivity) {
		this.currentActivity = currentActivity;
	}

	public synchronized List<Status> fetchFriendStatusById(long userID,
			int pageNumber) {
		// Log.d("TwitterApplication", "userID is :: " + userID);
		// Log.d("TwitterApplication", "Page Number is :: " + pageNumber);
		Twitter twitter = getTwitter();
		try {
			twitter.setPageNumber(pageNumber);
			List<Status> statusList;
			if (userID == 0) {
				statusList = twitter.getUserTimeline();
			}
			if (userID == -1) {
				statusList = twitter.getHomeTimeline();
			} else {
				statusList = twitter.getUserTimeline(userID);
			}
			return statusList;
		} catch (TwitterException e1) {
			Log.d(TAG, e1.getMessage());
			return null;
		} catch (RuntimeException e) {
			Log.d(TAG, e.getMessage());
			return null;
		}
	}

	public synchronized List<Status> fetchMentions(long sinceID,
			int pageNumber, boolean fromService) {
		Twitter twitter = getTwitter();
		try {
			// Log.d("TwitterApplication", "Since ID is :: " + sinceID);
			// Log.d("TwitterApplication", "Page Number is :: " + pageNumber);
			twitter.setSinceId(sinceID);
			if (0 == sinceID) {
				twitter.setSinceId(null);
			}
			if (0 == pageNumber) {
				twitter.setPageNumber(null);
			} else {
				twitter.setPageNumber(pageNumber);
			}
			List<Status> statusList = twitter.getMentions();
			if (pageNumber == 1 && !fromService) {
				Status lastestMention = statusList.get(0);

				Editor editor = getPrefs().edit();
				editor.putLong(LATEST_MENTION, lastestMention.id.longValue());
				editor.commit();
			}
			twitter.setSinceId(null);
			return statusList;
		} catch (TwitterException e1) {
			e1.printStackTrace();
//			Log.d(TAG, e1.getMessage());
			return null;
		} catch (RuntimeException e) {
			e.printStackTrace();
//			Log.d(TAG, e.getMessage());
			return null;
		}
	}

	public synchronized List<Status> fetchFriendStatusByIdAndStatusID(
			long userID, Number statusID) {
		Twitter twitter = getTwitter();
		try {
			twitter.setSinceId(statusID);
			List<Status> statusList;
			if (userID == 0) {
				statusList = twitter.getUserTimeline();
			}
			if (userID == -1) {
				statusList = twitter.getHomeTimeline();
			} else {
				statusList = twitter.getUserTimeline(userID);
			}
			twitter.setSinceId(null);
			return statusList;
		} catch (TwitterException e1) {
			return null;
		} catch (RuntimeException e) {
			return null;
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {

	}

	public void displayMyProfile() {
		new DisplayOwnProfile().execute("");
	}

	private class DisplayOwnProfile extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... params) {
			Intent intent = new Intent(TwitterApplication.this,
					FriendViewActivity.class);
			intent.putExtra(FriendListActivity.SELECTED_INDEX,
					TwitterApplication.this.getTwitter().getSelf().id);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			return null;
		}

	}

	public AdRequest getAdRequest() {
		return adRequest;
	}

	public void CreateStatusDialog(String dialogTitle, String dialogContent,
			final Number replyStatusID) {
		AlertDialog.Builder alert = new AlertDialog.Builder(currentActivity);
		alert.setMessage("Reply Status");
		// Set an EditText view to get user input
		// final EditText input = new EditText(StatusViewActivity.this);
		// input.setText("@" + status.getStatus().getUser().getScreenName() +
		// " ");
		// input.setSelection(input.getText().length());

		LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = vi.inflate(R.layout.status2, null);
		v.setBackgroundColor(Color.BLACK);
		alert.setView(v);
		final AlertDialog alertDialog = alert.create();


		final TextView textCount = (TextView) v.findViewById(R.id.textCount);
		final EditText statusContent = (EditText) v
				.findViewById(R.id.editText1);
		final ImageButton updateButton = (ImageButton) v
				.findViewById(R.id.imageButtonSendTweet);
		updateButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				new PostStatus(replyStatusID).execute(statusContent.getText().toString());
				alertDialog.dismiss();
			}
		});

		// alert.setPositiveButton("Reply", new
		// DialogInterface.OnClickListener() {
		// public void onClick(DialogInterface dialog, int whichButton) {
		// taskToExecute.execute(statusContent.getText().toString());
		//
		// }
		// });
		//
		// alert.setNegativeButton("Cancel",
		// new DialogInterface.OnClickListener() {
		// public void onClick(DialogInterface dialog, int whichButton) {
		// // Canceled.
		// }
		// });
		alertDialog.show();

		statusContent.setText(dialogContent);
		statusContent.setSelection(statusContent.getText().length());

		textCount.setText(Integer.toString(140 - statusContent.getText()
				.length()));
		textCount.setTextColor(Color.GREEN);

		statusContent.addTextChangedListener(new TextWatcher() {
			int count = 0;

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable statusText) {
				count = 140 - statusText.length(); //
				textCount.setText(Integer.toString(count));
				textCount.setTextColor(Color.GREEN); //
				updateButton.setEnabled(true);
				if (count < 10)
					textCount.setTextColor(Color.YELLOW);
				if (count < 0) {
					textCount.setTextColor(Color.RED);
					updateButton.setEnabled(false);
				}
			}
		});
	}

	// Asynchronously posts to twitter
	class PostStatus extends AsyncTask<String, Integer, String> {
		
		Number replyToStatusID;
		
		public PostStatus(Number replyToStatusID) {
			this.replyToStatusID = replyToStatusID;
		}
		
		// Called to initiate the background activity
		@Override
		protected String doInBackground(String... statuses) {
			try { 
				getTwitter().updateStatus(statuses[0], replyToStatusID);
				return "Success";
			} catch (TwitterException e) {
				Log.e("BaseActivity", e.toString());
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
			Log.d("BaseActivity", result);
			Toast.makeText(getCurrentActivity(), result, Toast.LENGTH_LONG)
					.show();
		}
	}
}