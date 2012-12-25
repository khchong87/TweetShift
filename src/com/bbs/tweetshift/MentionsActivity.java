package com.bbs.tweetshift;

import java.util.ArrayList;

import winterwell.jtwitter.Status;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;

import com.google.ads.AdSize;
import com.google.ads.AdView;

public class MentionsActivity extends BaseActivity {

	CustomizedListView listTimeline;
	private AdView adView;
	private TweetStatusAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.timeline);
		// Find your views
		listTimeline = (CustomizedListView) findViewById(R.id.listTimeline);
		// start up 
		listTimeline.setOnItemClickListener(new MentionActivityOnItemClickListener() );

	    LinearLayout layout = (LinearLayout) findViewById(R.id.timelineMainList);
		adView = new AdView(this, AdSize.BANNER, "a1507bd4d478a6b");
	    layout.addView(adView, 0);
	    adView.loadAd(twitterApplication.getAdRequest());
	    
//	    if( null == adView ){
//	    	adView = new AdView( this, 
//	    						"http://my.mobfox.com/request.php",
//	    						"31bd2e5264545e12e67688dc756ab382",
//	    						true, true);
//	    	adView.setAdListener(this);
//		    layout.addView(adView, 0);
//	    	
//	    }
//	    if( null == adManager){
//	    	adManager = new AdManager( this, 
//					"http://my.mobfox.com/request.php",
//					"31bd2e5264545e12e67688dc756ab382",
//					true);
//	    	adManager.setListener(this);
//			adManager.requestAd();
//	    }
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		twitterApplication.setCurrentActivity(this);
		this.setTitle("Mentions");
		
		if( null == adapter){
			adapter = new TweetStatusAdapter(this, R.id.textTweetContent,
					new ArrayList<Status>(), -1, twitterApplication, listTimeline, TweetStatusAdapter.LIST_TYPE_MENTION);
		}
	}
	
	class MentionActivityOnItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			Intent intent = new Intent(MentionsActivity.this,
					StatusViewActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			Status status = (Status) listTimeline.getItemAtPosition(position);
			StatusContainer selectedStatus = new StatusContainer(status, status.user.profileImageUrl.toString(), status.source, status.user.name);
			twitterApplication.setSelectedTweet(selectedStatus);
			startActivity(intent);
		}
	}
}
