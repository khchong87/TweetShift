package com.bbs.tweetshift;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

/**
 * The base activity with common features shared by TimelineActivity and
 * StatusActivity
 */
public class BaseActivity extends Activity { //
	public static TwitterApplication twitterApplication; //

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		twitterApplication = (TwitterApplication) getApplication(); //
	}

	// Called only once first time menu is clicked on
	@Override
	public boolean onCreateOptionsMenu(Menu menu) { //
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	// Called every time user clicks on a menu item
	@Override
	public boolean onOptionsItemSelected(MenuItem item) { //
		Intent intent;
		switch (item.getItemId()) {
//		case R.id.itemPrefs:
//			startActivity(new Intent(this, PrefsActivity.class)
//					.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
//			break;
//		case R.id.itemToggleService:
//			if (twitterApplication.isServiceRunning()) {
//				stopService(new Intent(this, UpdaterService.class));
//			} else {
//				startService(new Intent(this, UpdaterService.class));
//			}
//			
//			break;
//		case R.id.itemPurge:
////			((YambaApplication) getApplication()).getStatusData().delete();
//			Toast.makeText(this, R.string.msgAllDataPurged, Toast.LENGTH_LONG)
//					.show();
//			twitterApplication.printImageCache();
//			twitterApplication.getStatusData().deleteAllTableData();
//			break;
		case R.id.itemTimeline:
			startActivity(new Intent(this, TimelineActivity.class).addFlags(
					Intent.FLAG_ACTIVITY_SINGLE_TOP).addFlags(
					Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
			break;
		case R.id.itemStatus:
//			startActivity(new Intent(this, StatusActivity2.class)
//					.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
			twitterApplication.CreateStatusDialog("Update Status", "", null);
			break;
		case R.id.itemFriendList:
			intent = new Intent(this, FriendListActivity.class).addFlags(
					Intent.FLAG_ACTIVITY_SINGLE_TOP).addFlags(
					Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(intent);
			break;
//		case R.id.itemFollowerList:
//			intent = new Intent(this, FriendListActivity.class);
//			intent.putExtra(FriendListActivity.LIST_TYPE,
//					FriendListActivity.LIST_FOLLOWER);
//			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//			startActivity(intent);
//			break;
		case R.id.itemMyProfile:
			twitterApplication.displayMyProfile();
			break;
		case R.id.itemMention:
			intent = new Intent(this, MentionsActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(intent);
			break;
		case R.id.itemLogout:
			twitterApplication.logout();
			intent = new Intent(this, LoginRequester.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			break;
		}
		return true;
	}


	// Called every time menu is opened
	// @Override
	// public boolean onPrepareOptionsMenu(Menu menu) { //
	// Log.d("BaseActivity", "menu item is null? " + (null == menu ) );
	// MenuItem toggleItem = menu.findItem(R.id.itemToggleService); //
	// if (twitterApplication.isServiceRunning()) { //
	// toggleItem.setTitle(R.string.titleServiceStop);
	// toggleItem.setIcon(android.R.drawable.ic_media_pause);
	// } else { //
	// toggleItem.setTitle(R.string.titleServiceStart);
	// toggleItem.setIcon(android.R.drawable.ic_media_play);
	// }
	// return true;
	// }
}