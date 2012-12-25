package com.bbs.tweetshift;

import java.util.ArrayList;
import java.util.List;

import winterwell.jtwitter.Status;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

public class UpdaterService extends Service {
	static final String TAG = UpdaterService.class.getSimpleName();
	private static final int MENTION_NOTIFICATION_ID = 12345;
	private boolean runFlag = false;
	private Updater updater;
	private TwitterApplication twitterApplication;
	private int newMentionCount = 0;
	SQLiteDatabase db;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		this.twitterApplication = (TwitterApplication) getApplication();
		this.updater = new Updater();
		Log.d(TAG, "onCreated");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		this.runFlag = true;
//		twitterApplication.setServiceRunning(true);
		try{
			this.updater.start();
		} catch (IllegalThreadStateException e){
			Log.d(TAG, "catched thread exception");
		}
		Log.d(TAG, "onStarted");
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "On Destroy for Updater Service");
		super.onDestroy();
		this.runFlag = false;
//		twitterApplication.setServiceRunning(false);
		this.updater.interrupt();
		this.updater = null;
		Log.d(TAG, "onDestroyed");
	}

	/**
	 * Thread that performs the actual update from the online service
	 */
	private class Updater extends Thread {
		List<Status> mentionList;
		private int pageNumber = 1;

		public Updater() {
			super("UpdaterService-Updater");
			mentionList = new ArrayList<Status>();
		}

		@Override
		public void run() {
			UpdaterService updaterService = UpdaterService.this;
			List<Status> newMentionList;
			while (updaterService.runFlag) {
				Log.d(TAG, "Running background thread");
				try {
					//fetch mentions here
					Log.d(TAG, "fetch data now");
					Number latestMentionID = twitterApplication.getPrefs().getLong(TwitterApplication.LATEST_MENTION, 0);
//					if( latestMentionID.equals(0)){
//						latestMentionID = null;
//					}
					
					Log.d(TAG, "Latest mention id is " + latestMentionID);
					
					newMentionList = twitterApplication.fetchMentions( latestMentionID.longValue(), pageNumber++, true);
					if( null != newMentionList && !newMentionList.isEmpty() ){
						Log.d(TAG, "hey, we got something");
						//send notification
						mentionList.addAll(newMentionList);
						Log.d(TAG, mentionList.size() + " mentions found!");
						while(newMentionList.size() >= 20){
							newMentionList = twitterApplication.fetchMentions( latestMentionID.longValue(), pageNumber++, true);
							mentionList.addAll(newMentionList);
						}
						Log.d(TAG, mentionList.size() + " mentions found!");
						if( newMentionCount != mentionList.size()){
							newMentionCount = mentionList.size();
							NotificationCompat.Builder mBuilder =
							        new NotificationCompat.Builder(twitterApplication)
							        .setSmallIcon(R.drawable.mention)
							        .setContentTitle( mentionList.size() + " New Mentions")
							        .setContentText("You have " + mentionList.size() + " new mentions on Twitter")
							        .setAutoCancel(true);
								
								// Creates an explicit intent for an Activity in your app
								Intent resultIntent = new Intent(twitterApplication, MentionsActivity.class);
				
								// The stack builder object will contain an artificial back stack for the
								// started Activity.
								// This ensures that navigating backward from the Activity leads out of
								// your application to the Home screen.
								TaskStackBuilder stackBuilder = TaskStackBuilder.create(twitterApplication);
								// Adds the back stack for the Intent (but not the Intent itself)
								stackBuilder.addParentStack(TimelineActivity.class);
								// Adds the Intent that starts the Activity to the top of the stack
								stackBuilder.addNextIntent(resultIntent);
								PendingIntent resultPendingIntent =
								        stackBuilder.getPendingIntent(
								            0,
								            PendingIntent.FLAG_UPDATE_CURRENT
								        );
								mBuilder.setContentIntent(resultPendingIntent);
								NotificationManager mNotificationManager =
								    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
								// mId allows you to update the notification later on.
								mNotificationManager.notify(MENTION_NOTIFICATION_ID, mBuilder.build());
								try {
							        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
							        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
							        r.play();
							    } catch (Exception e) {}
						}
						
					}else{
						newMentionCount = 0;
						Log.d(TAG, "FFFFFFFUUUUUUUUUUUU");
					}
					mentionList.clear();
					pageNumber = 1;
					Thread.sleep(900000); // 
				} catch (InterruptedException e) {
					updaterService.runFlag = false; // 
				}
			}
		}
	} // Updater
}
