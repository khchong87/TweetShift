package com.bbs.tweetshift;

import java.util.ArrayList;
import java.util.List;

import com.google.ads.AdSize;
import com.google.ads.AdView;

import winterwell.jtwitter.Twitter;
import winterwell.jtwitter.TwitterException;
import winterwell.jtwitter.Twitter_Users;
import winterwell.jtwitter.User;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class FriendListActivity extends BaseActivity {
	public static final String SEND_FRIENDLIST_NOTIFICATION = "com.bbs.tweetshift.SEND_FRIENDLIST_NOTIFICATIONS";
	public static final String RECEIVE_FRIENDLIST_NOTIFICATION = "com.bbs.tweetshift.RECEIVE_FRIENDLIST_NOTIFICATIONS";
	public static final String NEW_FRIENDLIST_INTENT = "com.bbs.tweetshift.NEW_FRIENDLIST";
	public static final String SELECTED_INDEX = "com.bbs.tweetshift.SELECTED_INDEX";
	public static final String LIST_TYPE= "com.bbs.tweetshift.FRIENDLISTYPE";
	public static final String LIST_FOLLOWER = "com.bbs.tweetshift.FOLLOWER";
	public static final String LIST_FRIEND = "com.bbs.tweetshift.FOLLOWING";
	
	
	private FriendlistAdapter adapter;
	private ListView listFriendlist;
	private Twitter twitter;
	private List<UserContainer> userList;
	private List<UserContainer> followerList;
	private List<UserContainer> friendList;
	private ProgressDialog m_ProgressDialog = null;
	private String userScreenName;
	private Object listType;
	private Button friendListButton;
	private Button followerListButton;
	

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friendlist);
		twitter = twitterApplication.getTwitter();
		AdView adView = new AdView(this, AdSize.BANNER, "a1507bd4d478a6b");

	    LinearLayout layout = (LinearLayout) findViewById(R.id.friendlistMainList);
	    layout.addView(adView, 0);
	    adView.loadAd(twitterApplication.getAdRequest());

		this.setTitle("Friend/Follower List");
		Bundle intentBundle = getIntent().getExtras();
		
		friendListButton = (Button) findViewById(R.id.buttonFriends);
		followerListButton = (Button) findViewById(R.id.buttonFollowers);
		friendListButton.setOnClickListener(new FriendListButtonListner());
		followerListButton.setOnClickListener(new FollowerListButtonListner());
		
		if (null != intentBundle) {
			userScreenName = intentBundle.getString(FriendListActivity.SELECTED_INDEX);
		}
		if( null == listType ){
			listType = LIST_FRIEND;
			friendListButton.setBackgroundResource(R.drawable.textfield_bg);
		}
		if( null == userScreenName ){
				userList = twitterApplication.getFollowingList();
		}else{
			userList = null;
		}
		
		if (null == userList || userList.isEmpty()) {
			m_ProgressDialog = ProgressDialog.show(FriendListActivity.this,
					"Please wait...", "Retrieving data ...", true);
		}
		adapter = new FriendlistAdapter(this, R.layout.row_friend, userList);
		listFriendlist = (ListView) findViewById(R.id.listViewFriendList);
		listFriendlist.setAdapter(adapter);
		listFriendlist.setOnItemClickListener(new FriendSelectionListener());
		new UserFriendListFetcher().execute("");
	}

	private void updateFriendList(List<UserContainer> friendList) {
		if (null != friendList) {
			Log.d(FriendListActivity.class.getSimpleName(),
					String.valueOf(friendList.size()));
		}
		this.userList = friendList;
		if( null == userScreenName ){
			twitterApplication.setFollowerList(friendList);
		}
		adapter.setUserList(friendList);
	}
	
	private class UserFriendListFetcher extends
			AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... params) {
			try {
				Twitter_Users users = twitter.users();
				List<User> userList = new ArrayList<User>();
				if( LIST_FOLLOWER.equals( listType ) ){
					userList.addAll( users.getFollowers(userScreenName) );
				}else{
					userList.addAll( users.getFriends( userScreenName) );
				}
				
				List<UserContainer> userContainerList = new ArrayList<UserContainer>();
				for (User user : userList) {
					userContainerList.add(new UserContainer(user));
				}
				Log.d("UserFriendListFetcher", "calling friend list fetching");
				if(null != m_ProgressDialog ){
					m_ProgressDialog.dismiss();
				}
				updateFriendList(userContainerList);
				return null;
			} catch (TwitterException e1) {
				Log.d("UserFriendListFetcher",
						"TwitterException" + e1.getClass());
				Log.e("UserFriendListFetcher",
						"Failed to fetch status updates :: Twitter Exception",
						e1);
				m_ProgressDialog.dismiss();
				return null;
			} catch (RuntimeException e) {
				Log.d("UserFriendListFetcher", "RuntimeException", e);
				m_ProgressDialog.dismiss();
				return null;
			}
		}
		
		@Override
		protected void onPostExecute(String result) {
			adapter.notifyDataSetChanged();
		}
	}


	@Override
	protected void onResume() {
		super.onResume();
		twitterApplication.setCurrentActivity(this);
		// Setup List
		if( LIST_FRIEND.equals( listType ) ){
			this.setTitle("Friend List");
		}else{
			this.setTitle("Follower List");
		}
	}

	class FriendSelectionListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			Intent intent = new Intent(FriendListActivity.this,
					FriendViewActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra(SELECTED_INDEX, userList.get(position).getUserID());
			startActivity(intent);
		}
	}

	class FriendListButtonListner implements OnClickListener{

		@Override
		public void onClick(View v) {
			m_ProgressDialog = ProgressDialog.show(FriendListActivity.this,
					"Please wait...", "Retrieving data ...", true);
			if( null == friendList || friendList.isEmpty() ){
				//fetch list here, obviously friend list just follow as well
				friendListButton.setBackgroundResource(R.drawable.textfield_bg);
				followerListButton.setBackgroundResource( R.color.transparent );
				listType = LIST_FRIEND;
				if( null != friendList && ! friendList.isEmpty()){
					updateFriendList(friendList);
					adapter.notifyDataSetChanged();
					m_ProgressDialog.dismiss();
				}else{
					new UserFriendListFetcher().execute("");
				}
			}
		}
		
	}
	
	class FollowerListButtonListner implements OnClickListener{

		@Override
		public void onClick(View v) {
			m_ProgressDialog = ProgressDialog.show(FriendListActivity.this,
					"Please wait...", "Retrieving data ...", true);
			if( null == followerList || followerList.isEmpty() ){
				//fetch list here, obviously friend list just follow as well
				followerListButton.setBackgroundResource(R.drawable.textfield_bg);
				friendListButton.setBackgroundResource( R.color.transparent );
				listType = LIST_FOLLOWER;
				if( null != followerList && ! followerList.isEmpty()){
					updateFriendList(followerList);
					adapter.notifyDataSetChanged();
					m_ProgressDialog.dismiss();
				}else{
					new UserFriendListFetcher().execute("");
				}
			}
		}
		
	}
}
