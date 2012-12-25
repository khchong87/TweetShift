package com.bbs.tweetshift;

import java.util.ArrayList;
import java.util.List;

import winterwell.jtwitter.Status;
import winterwell.jtwitter.TwitterException;
import winterwell.jtwitter.TwitterException.Repetition;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ads.AdSize;
import com.google.ads.AdView;

public class StatusViewActivity extends BaseActivity {
	StatusContainer status;
	private ProgressDialog m_ProgressDialog = null;
//	private ImageLoader imageLoader;
	private List<Status> replyList;
	private TweetStatusAdapter statusAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.statusdetail);
		setTitle("Status View");
		AdView adView = new AdView(this, AdSize.BANNER, "a1507bd4d478a6b");

		LinearLayout layout = (LinearLayout) findViewById(R.id.statusViewMainList);
		layout.addView(adView, 0);
		adView.loadAd(twitterApplication.getAdRequest());
		status = twitterApplication.getSelectedTweet();
		final ImageButton rtButton = (ImageButton) findViewById(R.id.imageButtonRT);

//		imageLoader = new ImageLoader(this);
		
		rtButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				new RetweetTask().execute("");
				if (rtButton.isSelected()) {
					rtButton.setBackgroundColor(Color.BLACK);
				} else {
					rtButton.setBackgroundColor(Color.TRANSPARENT);
				}
			}
		});

		ImageButton replyButton = (ImageButton) findViewById(R.id.imageButtonReply);
		replyButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				createReplyTweetDialog();
			}
		});

		ImageButton favouriteButton = (ImageButton) findViewById(R.id.imageButtonFavourite);
		favouriteButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				new FavouriteTweet().execute("");
			}
		});

		// we will push the selected status to twitter, then fetch from there
		m_ProgressDialog = ProgressDialog.show(StatusViewActivity.this,
				"Please wait...", "Retrieving data ...", true);
		new InitiateView().execute("");
	}

	@Override
	protected void onResume() {
		super.onResume();
		twitterApplication.setCurrentActivity(this);
		setTitle("Status View");
		initView();
	}

	private void initView() {
		if (null != status) {
			DisplayUserOnClickListner listner = new DisplayUserOnClickListner();
			TextView friendNameTextView = (TextView) findViewById(R.id.textViewFriendName);
			TextView statusDetailContentTextView = (TextView) findViewById(R.id.textViewStatusDetailContent);
			TextView sourceTextView = (TextView) findViewById(R.id.textViewSource);
			TextView createTimeTextView = (TextView) findViewById(R.id.textViewCreateTime);
			TextView screenNameTextView = (TextView) findViewById(R.id.textViewScreenName2);
			ImageView userProfileImageView = (ImageView) findViewById(R.id.imageViewStatusProfileImage);

			if (null != createTimeTextView) {
				createTimeTextView.setText(DateUtils
						.getRelativeTimeSpanString(status.getStatus().createdAt
								.getTime()));
			}
			if (null != screenNameTextView) {
				screenNameTextView.setOnClickListener(listner);
				screenNameTextView.setText("@"
						+ status.getStatus().user.getScreenName());
			}
			if (null != friendNameTextView) {
				friendNameTextView.setOnClickListener(listner);
				friendNameTextView.setText(status.getUserName());
			}
			if (null != statusDetailContentTextView) {
				Log.d("StatusViewActivity",
						"Status Length is :: "
								+ String.valueOf(status.getStatus().text
										.length()));
				statusDetailContentTextView.setText(status.getStatus()
						.getText());
			}
			if (null != userProfileImageView) {
				userProfileImageView.setOnClickListener(listner);
				new ImageLoader(this).DisplayImage(status.getProfileImageURL()
						.toString().replace("_normal", "_reasonably_small"),
						userProfileImageView);
			}
			if (null != sourceTextView) {
				sourceTextView.setText("via " + Html.fromHtml(status.source));
			}
			if( null != replyList && ! replyList.isEmpty() ){
				CustomizedListView statusContentListView = (CustomizedListView) findViewById(R.id.linearLayoutStatusContent);
				setStatusAdapter(new TweetStatusAdapter(StatusViewActivity.this, R.id.textTweetContent,
						replyList, Long.valueOf(0), twitterApplication,statusContentListView, false));
			}
		}
	}

//	private View createReplyRow(Status status) {
//
//		LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//		View v = vi.inflate(R.layout.row, null);
//
//		if (null != status) {
//			TextView textviewUser = (TextView) v.findViewById(R.id.textUser);
//			TextView textviewCreatedAt = (TextView) v.findViewById(R.id.textCreatedAt);
//			TextView textviewContent = (TextView) v.findViewById(R.id.textTweetContent);
//			TextView textviewSource = (TextView) v.findViewById(R.id.textSource);
//			ImageView profilePicture = (ImageView) v.findViewById(R.id.imageView1);
//
//			boolean isRetweet = false;
//			User originaluser = null;
//			String usernameText = "";
//			Status originalStatus = null;
//			long createdAt;
//			if (null != status.getOriginal()) {
//				originalStatus = status;
//				isRetweet = true;
//				originaluser = status.user;
//				status = status.getOriginal();
//			}
//			
//			if (isRetweet) {
//				usernameText = originaluser.getName() + "\n@RT: "
//						+ status.getUser().getName();
//				createdAt = originalStatus.getCreatedAt().getTime();
//			} else {
//				createdAt = status.getCreatedAt().getTime();
//				usernameText = status.getUser().getName();
//			}
//
//			if (null != textviewUser) {
//				textviewUser.setText(usernameText);
//			}
//			if (null != textviewCreatedAt) {
//				textviewCreatedAt.setText(DateUtils
//						.getRelativeTimeSpanString(createdAt));
//			}
//			if (null != textviewContent) {
//				textviewContent.setText(status.text);
//			}
//			if (null != textviewSource) {
//				textviewSource.setText("via " + Html.fromHtml(status.source));
//			}
//			if (null != profilePicture) {
//				imageLoader.DisplayImage(status.user.getProfileImageUrl()
//						.toString().replace("_normal", "_reasonably_small"),
//						profilePicture);
//			}
//		}
//
//		return v;
//	}

	private void createReplyTweetDialog() {
		twitterApplication.CreateStatusDialog("Reply Status", "@" + status.getStatus().getUser().getScreenName() + " ",status.getStatus().id );
	}

	public TweetStatusAdapter getStatusAdapter() {
		return statusAdapter;
	}

	public void setStatusAdapter(TweetStatusAdapter statusAdapter) {
		this.statusAdapter = statusAdapter;
	}

	private class RetweetTask extends AsyncTask<String, Integer, String> {
		private static final String RETWEET_SUCCESS = "SUCCESS";
		private static final String RETWEET_FAILED = "RETWEETTED";
		private static final String RETWEETTED_STATUS = "FAILED";

		@Override
		protected String doInBackground(String... params) {
			try {
				twitterApplication.getTwitter().retweet(status.getStatus());
				return RETWEET_SUCCESS;
			} catch (Repetition r) {
				return RETWEETTED_STATUS;
			} catch (TwitterException t) {
				Log.e("Retweet Twitter Exception", "Failed", t);
			} catch (Exception e) {
				Log.e("Retweet General Exception", "Failed", e);
			}
			return RETWEET_FAILED;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (RETWEET_SUCCESS.equals(result)) {
				Toast.makeText(StatusViewActivity.this,
						"Message Retweetted Successfully", Toast.LENGTH_LONG).show();
			} else if (RETWEET_SUCCESS.equals(result)) {
				Toast.makeText(StatusViewActivity.this,
						"Message Already Retweetted", Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(StatusViewActivity.this, "Message Failed", Toast.LENGTH_LONG)
						.show();
			}
		}
	}

	private class FavouriteTweet extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... params) {
			twitterApplication.getTwitter().setFavorite(status.status,
					!status.status.isFavorite());
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			m_ProgressDialog = ProgressDialog.show(StatusViewActivity.this,
					"Please wait...", "Retrieving data ...", true);
			new InitiateView().execute("");
			super.onPostExecute(result);
		}

	}

	private class InitiateView extends AsyncTask<String, Integer, String> {

		winterwell.jtwitter.Status currentStatus;

		@Override
		protected String doInBackground(String... params) {
			Log.d("StatusViewActivity" , "ur status id is :: " + status.status.getId());
			currentStatus = twitterApplication.getTwitter().getStatus(
					status.status.getId());
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			ImageButton favouriteButton = (ImageButton) findViewById(R.id.imageButtonFavourite);
			if (currentStatus.isFavorite()) {
				favouriteButton.setImageResource(R.drawable.favourite);
			} else {
				favouriteButton.setImageResource(R.drawable.not_favourite);
			}
			status = new StatusContainer(currentStatus, currentStatus.getUser()
					.getProfileImageUrl().toString(), currentStatus.source,
					currentStatus.getUser().getName());
			
			if( null != currentStatus.inReplyToStatusId ){
				Log.d("StatusViewActivity", "The reply status is :: "+ currentStatus.inReplyToStatusId );
				replyList = new ArrayList<winterwell.jtwitter.Status>();
//				statusContentListView.removeFooter();
				new PreviousReply(currentStatus).execute("");
			}else{
				initView();
				if (null != m_ProgressDialog) {
					m_ProgressDialog.dismiss();
				}
			}
		}
	}

	private class PreviousReply extends AsyncTask<String, Integer, String> {

		winterwell.jtwitter.Status statusToLoad;
		winterwell.jtwitter.Status previousStatus;

		public PreviousReply(winterwell.jtwitter.Status statusToLoad) {
			this.statusToLoad = statusToLoad;
		}

		@Override
		protected String doInBackground(String... params) {
			try{
				previousStatus = twitterApplication.getTwitter().getStatus(
						statusToLoad.inReplyToStatusId);
			} catch (TwitterException e){
				previousStatus = null;
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			if( null != previousStatus ){
				replyList.add(previousStatus);
			}
			if( null != previousStatus && null != previousStatus.inReplyToStatusId ){
				Log.d("StatusViewActivity", "The reply status is :: "+ previousStatus.inReplyToStatusId );
				new PreviousReply(previousStatus).execute("");
			}else{
//				statusAdapter.notifyDataSetChanged();
				initView();
				if (null != m_ProgressDialog) {
					m_ProgressDialog.dismiss();
				}
			}
		}

	}

	private class DisplayUserOnClickListner implements OnClickListener {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent(StatusViewActivity.this,
					FriendViewActivity.class);
			intent.putExtra(FriendListActivity.SELECTED_INDEX,
					status.getStatus().user.id);
			intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			StatusViewActivity.this.startActivity(intent);
		}

	}
}
