package com.bbs.tweetshift;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import winterwell.jtwitter.Status;
import winterwell.jtwitter.User;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class TweetStatusAdapter extends CustomizedArrayAdapter implements OnScrollListener, OnItemClickListener {

	public static final String LIST_TYPE_MENTION = "MENTION_LIST";
	public static final String LIST_TYPE_TIMELINE = "TIMELINE_LIST";
	private List<Status> statusList;
	private Context context;
	private ImageLoader imageLoader;
	private TwitterApplication twitterApplication;
	private long userID;
	private int pageNumber = 1;
	private boolean statusProcessing = false;
	private CustomizedListView listview;
	private BigInteger latestStatusID = BigInteger.ZERO;
	private boolean enableAction = true; 
	private String listType;

	public TweetStatusAdapter(Context context, int textViewResourceId,
			List<Status> statusList, long userID, TwitterApplication twitterApplication, CustomizedListView listview, String listType) {
		super(context, R.layout.row, textViewResourceId, statusList);
		this.statusList = statusList;
		this.context = context;
		imageLoader = new ImageLoader(context);
		this.twitterApplication = twitterApplication;
		this.userID = userID;
		this.listview = listview;
		this.listType = listType;
		listview.setAdapter(this);
		listview.setOnScrollListener(this);
		listview.setOnItemClickListener(this);
		listview.setOverScrollMode(ListView.OVER_SCROLL_ALWAYS);
		fetchStatus();
	}
	
	public TweetStatusAdapter(Context context, int textViewResourceId,
			List<Status> statusList, long userID, TwitterApplication twitterApplication, CustomizedListView listview, boolean enableActions) {
		super(context, R.layout.row, textViewResourceId, statusList);
		this.statusList = statusList;
		this.context = context;
		imageLoader = new ImageLoader(context);
		this.twitterApplication = twitterApplication;
		this.userID = userID;
		this.listview = listview;
		listview.removeFooter();
		listview.setAdapter(this);
		listview.setOverScrollMode(ListView.OVER_SCROLL_NEVER);
		fetchStatus();
	}

	@Override
	public int getCount() {
		if (statusList != null)
			return statusList.size();
		else
			return 0;
	}

	@Override
	public Status getItem(int arg0) {
//		 if (arg0 >= super.getCount()) {
//		      return(new Status(null, "", 000, null));
//		    }
		return statusList.get(arg0);
	}
	
	@Override
	public int getViewTypeCount() {
	    return(super.getViewTypeCount() + 1);
	  }

	public void setStatusList(List<Status> statusList) {
		Log.d("FriendTweetAdapter", "setStatusList :: " + String.valueOf( statusList.size()) );
		this.statusList = statusList;
		this.notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
//		if(position != super.getCount()){
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.row, null);
			}
			if (null == statusList) {
				return super.getView(position, v, parent);
			}
			Status status = statusList.get(position);
	
			boolean isRetweet = false;
			User originaluser = null;
			String usernameText = "";
			Status originalStatus = null;
			long createdAt;
			if (null != status.getOriginal()) {
				originalStatus = status;
				isRetweet = true;
				originaluser = status.user;
				status = status.getOriginal();
			}
	
			if (null != status) {
				TextView textviewUser = (TextView) v
						.findViewById(R.id.textUser);
				TextView textviewCreatedAt = (TextView) v
						.findViewById(R.id.textCreatedAt);
				TextView textviewContent = (TextView) v
						.findViewById(R.id.textTweetContent);
				TextView textviewSource = (TextView) v
						.findViewById(R.id.textSource);
				ImageView profilePicture = (ImageView) v
						.findViewById(R.id.imageView1);
	
				if (isRetweet) {
					usernameText = originaluser.getName() + "\n@RT: "
							+ status.getUser().getName();
					createdAt = originalStatus.getCreatedAt().getTime();
				} else {
					createdAt = status.getCreatedAt().getTime();
					usernameText = status.getUser().getName();
				}
	
				if (null != textviewUser) {
					textviewUser.setText(usernameText);
				}
				if (null != textviewCreatedAt) {
					textviewCreatedAt.setText(DateUtils
							.getRelativeTimeSpanString(createdAt));
				}
				if (null != textviewContent) {
					textviewContent.setText(status.text);
				}
				if (null != textviewSource) {
					textviewSource.setText("via "
							+ Html.fromHtml(status.source));
				}
				if (null != profilePicture) {
					imageLoader.DisplayImage(
							status.user.getProfileImageUrl().toString()
									.replace("_normal", "_reasonably_small"),
							profilePicture);
				}
			}
//		}else{
//			if (v == null) {
//				LayoutInflater vi = (LayoutInflater) context
//						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//				v = vi.inflate(R.layout.loadingrow, parent, false);
//				return v;
//			}
//		}
		return super.getView(position, v, parent);
	}
	
	
	public void fetchStatus(){
		if( !statusProcessing ){
			statusProcessing = true;
			new StatusFetcher().execute("");
		}
	}
	
	public int getItemViewType(int position) {
//	    if (position == super.getCount()) {
//	      return(IGNORE_ITEM_VIEW_TYPE);
//	    }

	    return(super.getItemViewType(position));
	  }
	
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {

		if(firstVisibleItem + visibleItemCount >= totalItemCount && totalItemCount > 0 ){
			if( !statusProcessing ){
				Log.d( "TweetStatusAdapter", "Not processing" );
				statusProcessing = true;
				new StatusFetcher().execute("");
			}
		}
		
	}
	
	private class StatusFetcher extends AsyncTask<String, Integer, String> {

		public StatusFetcher() {
			super();
		}

		@Override
		protected void onPreExecute() {
			listview.getFooterView().setVisibility(View.VISIBLE);
		}
		
		@Override
		protected String doInBackground(String... params) {
			Log.d("FriendTweetAdapter", "pageNumber "+String.valueOf( pageNumber ) );
			List<winterwell.jtwitter.Status> newStatus = new ArrayList<winterwell.jtwitter.Status>();
			if( LIST_TYPE_TIMELINE == listType){
				newStatus = twitterApplication.fetchFriendStatusById(userID, pageNumber++);
			}else if( LIST_TYPE_MENTION == listType){
				newStatus = twitterApplication.fetchMentions( 0, pageNumber++, false );
			}
			Log.d("FriendTweetAdapter", "pageNumber after "+String.valueOf( pageNumber ) );
			if( null == statusList){
				statusList = new ArrayList<winterwell.jtwitter.Status>();
			}
			if( null != newStatus){
				statusList.addAll( newStatus );
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			if(BigInteger.ZERO.equals(latestStatusID) && null != statusList && !statusList.isEmpty()){
				latestStatusID = statusList.get(0).id;
			}
			setStatusList(statusList);
			statusProcessing = false;
			listview.getFooterView().setVisibility(View.GONE);
		}
	}
	
	private class StatusRefresh extends AsyncTask<String, Integer, String> {

		public StatusRefresh() {
			super();
		}

		@Override
		protected String doInBackground(String... params) {
			List<winterwell.jtwitter.Status> newStatus = new ArrayList<winterwell.jtwitter.Status>();;
			if( LIST_TYPE_TIMELINE == listType){
				twitterApplication.getTwitter().setSinceId(latestStatusID.longValue());
				newStatus = twitterApplication.fetchFriendStatusById(userID, 1);
				twitterApplication.getTwitter().setSinceId(null);
			}else if( LIST_TYPE_MENTION == listType){
				newStatus = twitterApplication.fetchMentions( latestStatusID.longValue(), 1, false  );
			}
			if( null != newStatus && !newStatus.isEmpty()){
				statusList.addAll(0, newStatus );
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			if( null != statusList && !statusList.isEmpty()){
				latestStatusID = statusList.get(0).id;
			}
			setStatusList(statusList);
			statusProcessing = false;
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if(enableAction ){
			Intent intent;
			Status status = getItem(position);
			StatusContainer selectedStatus = new StatusContainer(status, status.user.profileImageUrl.toString(), status.source, status.user.name);
			twitterApplication.setSelectedTweet(selectedStatus);
			Log.d("OnClickTweetStatusRow", String.valueOf(id));
			if( id == R.id.textUser || id == R.id.imageView1){
				intent = new Intent(context,
						FriendViewActivity.class);
				intent.putExtra(FriendListActivity.SELECTED_INDEX, status.user.id);
			}else{
				intent = new Intent(context,
						StatusViewActivity.class);
			}
			intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
		}
	}

	@Override
	public void onScrollBottom() {
//		if( !statusProcessing && enableAction ){
//			statusProcessing = true;
//			new StatusFetcher().execute("");
//		}
	}

	@Override
	public void onScrollTop() {
		if( !statusProcessing && enableAction ){
			statusProcessing = true;
			Log.d("TweetStatusAdapter", "OnScrollTop");
			new StatusRefresh().execute("");
		}
	}
	
	public void disableTopBottomBounceAction(){
		enableAction = false;
	}
	
	public void enableTopBottomBounceAction(){
		enableAction = true;
	}
}
