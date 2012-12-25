package com.bbs.tweetshift;

import java.util.ArrayList;

import winterwell.jtwitter.Status;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.ads.AdSize;
import com.google.ads.AdView;

public class FriendViewActivity extends BaseActivity {

	public static final String SEND_STATUSLIST_NOTIFICATION = "com.marakana.yamba.SEND_STATUS_NOTIFICATIONS";
	public static final String RECEIVE_STATUSLIST_NOTIFICATION = "com.marakana.yamba.RECEIVE_STATUS_NOTIFICATIONS";
	public static final String NEW_STATUSLIST_INTENT = "com.marakana.yamba.NEW_STATUSLIST";

	private UserContainer user;
	private TweetStatusAdapter statusAdapter = null;
	private Long userID;
	private ProgressDialog m_ProgressDialog = null;
	private boolean isFollowing;
	private ImageLoader imageLoader;
	private Bitmap profileBackgroundBitmap;
	private AdView adView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friendview);
		imageLoader = new ImageLoader(this);

		Bundle intentBundle = getIntent().getExtras();
		if (null != intentBundle) {
			userID = intentBundle.getLong(FriendListActivity.SELECTED_INDEX);
		}

		m_ProgressDialog = ProgressDialog.show(FriendViewActivity.this,
				"Please wait...", "Retrieving data ...", true);
		new ViewInitiator().execute("");
		TextView followingStatuss = (TextView) FriendViewActivity.this
				.findViewById(R.id.textViewFollowStatus);
		followingStatuss.setOnClickListener(new AddFriendTextViewListener());
	}


	private void reinitView() {
		if (null != m_ProgressDialog) {
			m_ProgressDialog.dismiss();
		}
		if (null != user) {
			setTitle(user.getName() + "'s Page");
			// new StatusFetcher(0, user.getScreenName()).execute("");
			if (null == statusAdapter) {
				Log.d("FriendViewActivity", "statusAdapter is null!!!");
				CustomizedListView friendstatuslist = (CustomizedListView) findViewById(R.id.listViewFriendStatus);
				statusAdapter = new TweetStatusAdapter(this,
						R.id.textTweetContent, new ArrayList<Status>(), userID,
						twitterApplication, friendstatuslist,
						TweetStatusAdapter.LIST_TYPE_TIMELINE);
				statusAdapter.fetchStatus();
			}

			TextView friendNameTextView = (TextView) findViewById(R.id.textViewFriendName);
			TextView screenNameTextView = (TextView) findViewById(R.id.textViewScreenName);
			TextView followingTextView = (TextView) findViewById(R.id.textViewFollowing);
			TextView FollowerTextView = (TextView) findViewById(R.id.textViewFollower);
			ImageView imageViewBackground = (ImageView) findViewById(R.id.imageviewfriendbackground);
			// TextView descriptionTextView = (TextView)
			// findViewById(R.id.textViewDescription);
			ImageView userProfileImageView = (ImageView) findViewById(R.id.imageViewFriendPicture);
			LinearLayout layout = (LinearLayout) findViewById(R.id.linearLayoutMainContent);
			
			
			if (null != profileBackgroundBitmap) {
				Log.d("FriendView", user.getUser().profileBackgroundColor );
				imageViewBackground.setScaleType(ScaleType.FIT_START);
				imageViewBackground.setImageBitmap(profileBackgroundBitmap);
				
			}
			
			if( null != user.getUser().profileBackgroundColor){
				FrameLayout frameLayout = (FrameLayout) findViewById(R.id.friendviewMainList);
				frameLayout.setBackgroundColor(Color.parseColor("#"+user.getUser().profileBackgroundColor));
			}
			
			if( null != layout ){
				adView = new AdView(this, AdSize.BANNER, "a1507bd4d478a6b");
				layout.addView(adView, 0);
				adView.loadAd(twitterApplication.getAdRequest());
			}
			
			if (null != friendNameTextView) {
				friendNameTextView.setText(user.getName());
			}
			if (null != screenNameTextView) {
				screenNameTextView.setText("@" + user.getScreenName());
			}
			if (null != followingTextView) {
				followingTextView.setText(user.getFriendCount() + " Friends");
				followingTextView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						Log.d("followingTextView", "followingTextView clicked");
						Intent intent = new Intent(FriendViewActivity.this,
								FriendListActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						intent.putExtra(FriendListActivity.SELECTED_INDEX,
								user.getScreenName());
						intent.putExtra(FriendListActivity.LIST_TYPE,
								FriendListActivity.LIST_FRIEND);
						startActivity(intent);
					}
				});

			}
			if (null != FollowerTextView) {
				FollowerTextView
						.setText(user.getFollowerCount() + " Followers");
				FollowerTextView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						Log.d("followingTextView", "followingTextView clicked");
						Intent intent = new Intent(FriendViewActivity.this,
								FriendListActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						intent.putExtra(FriendListActivity.SELECTED_INDEX,
								user.getScreenName());
						intent.putExtra(FriendListActivity.LIST_TYPE,
								FriendListActivity.LIST_FOLLOWER);
						startActivity(intent);
					}
				});
			}
			// if( null != descriptionTextView ){
			// descriptionTextView.setText(user.description);
			// }
			if (null != userProfileImageView) {
				new ImageLoader(this).DisplayImage(user.getProfilePictureURL()
						.toString().replace("_normal", "_reasonably_small"),
						userProfileImageView);
			}
		}
	}

	// private class UserProfilePictureLoader extends AsyncTask<String, int,
	// String>{
	//
	// }

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		twitterApplication.setCurrentActivity(this);
		if (null != user) {
			setTitle(user.getName() + "'s Page");
		}
	}

	private class ViewInitiator extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... params) {
			user = new UserContainer(twitterApplication.getTwitter().users()
					.getUser(userID));
			isFollowing = twitterApplication
					.getTwitter()
					.users()
					.isFollower(
							twitterApplication.getTwitter().getSelf()
									.getScreenName(), user.getScreenName());

			profileBackgroundBitmap = imageLoader.getBitmap(user
					.getProfileBackgroundImageURL().toString());
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			reinitView();
			TextView followingStatuss = (TextView) FriendViewActivity.this
					.findViewById(R.id.textViewFollowStatus);
			if (!isFollowing) {
				followingStatuss.setCompoundDrawablesWithIntrinsicBounds(
						R.drawable.add_friend, 0, 0, 0);
				followingStatuss.setText(R.string.follow);
			} else {
				followingStatuss.setCompoundDrawablesWithIntrinsicBounds(
						R.drawable.friend, 0, 0, 0);
				followingStatuss.setText(R.string.following);
			}
		}
	}

	private class AddFriendTask extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... params) {
			if (isFollowing) {
				twitterApplication.getTwitter().users()
						.stopFollowing(user.getScreenName());
			} else {
				twitterApplication.getTwitter().users()
						.follow(user.getScreenName());
			}

			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			new ViewInitiator().execute("");
		}

	}

	private class AddFriendTextViewListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			m_ProgressDialog = ProgressDialog.show(FriendViewActivity.this,
					"Please wait...", "Retrieving data ...", true);
			new AddFriendTask().execute("");
		}

	}

}
