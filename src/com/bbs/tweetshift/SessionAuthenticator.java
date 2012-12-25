package com.bbs.tweetshift;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import winterwell.jtwitter.OAuthSignpostClient;
import winterwell.jtwitter.Twitter;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class SessionAuthenticator{
	private TwitterApplication twitterApplication;
	private static final String TAG = SessionAuthenticator.class.getSimpleName();
	private static String AUTH_CUSTOMER_KEY = "24QHpjMWBvVMNR0Ax7Ylnw";
	private static String AUTH_CUSTOMER_SECRET = "p9zDi1V8U6gfcSsVwzyzhalFx8OQlQre3lgWuKIk";
	private OAuthProvider provider;
	private OAuthConsumer consumer;
	private static String AUTH_ACCESS_TOKEN = "";
	private static String AUTH_ACCESS_SECRET = "";
	private static final String AUTH_TOKEN_IDENTIFIER = "access_token";
	private static final String AUTH_SECRET_IDENTIFIER = "access_secret";
	private static OAuthSignpostClient client;
	private Twitter twitter;
	private final String IS_VALID = "VALID";
	private final String INVALID = "INVALID";
	
	public SessionAuthenticator(TwitterApplication yamba){
		this.twitterApplication = yamba;
		consumer = new DefaultOAuthConsumer(AUTH_CUSTOMER_KEY,
				AUTH_CUSTOMER_SECRET);
		provider = new DefaultOAuthProvider(
				"http://api.twitter.com/oauth/request_token",
				"http://api.twitter.com/oauth/access_token",
				"http://api.twitter.com/oauth/authorize");
		AUTH_ACCESS_TOKEN = yamba.getPrefs().getString(AUTH_TOKEN_IDENTIFIER, "");
		AUTH_ACCESS_SECRET = yamba.getPrefs().getString(AUTH_SECRET_IDENTIFIER, "");
	}
	
	public OAuthSignpostClient getOAuthClient(){
		
		if(null == client){
			initOAuthClient();
		}
		return client;
	}
	
	public Twitter getTwitter(){
		if(null == twitter && null != getOAuthClient()){
			twitter = new Twitter("USERNAME", getOAuthClient());
			twitter.setupTwitlonger("shifttweet", "ozhgeMO9XnpeYxJz");
			twitter.setIncludeRTs(true);
		}
		return twitter;
	}
	
	public void initOAuthClient(){
		
		
		Log.d(TAG, "AUTH TOKEN :: " + AUTH_ACCESS_TOKEN);
		Log.d(TAG, "AUTH SECRET :: " + AUTH_ACCESS_SECRET);
	
		if (null == AUTH_ACCESS_TOKEN || AUTH_ACCESS_TOKEN.equals("")
				|| null == AUTH_ACCESS_SECRET || AUTH_ACCESS_SECRET.equals("")) {
//			new RerouteAuthorize().execute("");
			return;
		}
		consumer.setTokenWithSecret(AUTH_ACCESS_TOKEN, AUTH_ACCESS_SECRET);
		client = new OAuthSignpostClient(AUTH_CUSTOMER_KEY,
				AUTH_CUSTOMER_SECRET, AUTH_ACCESS_TOKEN, AUTH_ACCESS_SECRET);
	}
	
	public void rerouteAuthorization(){
		new RerouteAuthorize().execute("");
	}
	
	public void setupAuthorization(String pin, Activity context, ProgressDialog progressDialog){
		new FetchAuthToken(context, progressDialog).execute(pin);
	}
	
	private class RerouteAuthorize extends AsyncTask<String, Integer, String> {

		String authUrl;
		Intent intent;
		
		@Override
		protected String doInBackground(String... params) {
			try {
				
				authUrl = provider.retrieveRequestToken(consumer,
						TwitterApplication.CALLBACK_SCHEME+"://callback");
				intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(authUrl));
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				twitterApplication.startActivity(intent);

			} catch (OAuthMessageSignerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (OAuthNotAuthorizedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (OAuthExpectationFailedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (OAuthCommunicationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
	}
	
	private class FetchAuthToken extends AsyncTask<String, Integer, String> {

		Activity context;
		ProgressDialog progressDialog;
		
		public FetchAuthToken(Activity context,ProgressDialog progressDialog){
			this.context = context;
			this.progressDialog = progressDialog;
		}
		
		@Override
		protected String doInBackground(String... arg0) {
			try {
				provider.retrieveAccessToken(consumer, arg0[0]);
				AUTH_ACCESS_TOKEN = consumer.getToken();
				AUTH_ACCESS_SECRET = consumer.getTokenSecret();
				
				Log.d("FetchAuthToken", "AUTH TOKEN :: " + AUTH_ACCESS_TOKEN);
				Log.d("FetchAuthToken", "AUTH SECRET :: " + AUTH_ACCESS_SECRET);

				initOAuthClient();
			} catch (OAuthMessageSignerException e) {
				// TODO Auto-generated catch block
				Log.d("SessionAuthenticator", "Login Failed :: OAuthMessageSignerException");
				e.printStackTrace();
			} catch (OAuthNotAuthorizedException e) {
				// TODO Auto-generated catch block
				Log.d("SessionAuthenticator", "Login Failed :: OAuthNotAuthorizedException");
				e.printStackTrace();
			} catch (OAuthExpectationFailedException e) {
				// TODO Auto-generated catch block
				Log.d("SessionAuthenticator", "Login Failed :: OAuthExpectationFailedException");
				e.printStackTrace();
			} catch (OAuthCommunicationException e) {
				// TODO Auto-generated catch block
				Log.d("SessionAuthenticator", "Login Failed :: OAuthCommunicationException");
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			validateLogin(context, progressDialog);
		}
	}
	
	public void logout(){
		Editor editor = twitterApplication.getPrefs().edit();
		editor.putString(AUTH_TOKEN_IDENTIFIER, "");
		editor.putString(AUTH_SECRET_IDENTIFIER, "");
		editor.commit();
		AUTH_ACCESS_TOKEN = twitterApplication.getPrefs().getString(AUTH_TOKEN_IDENTIFIER, "");
		AUTH_ACCESS_SECRET = twitterApplication.getPrefs().getString(AUTH_SECRET_IDENTIFIER, "");
	}
	
	
	public boolean isLoggedIn(){
		if(null == AUTH_ACCESS_TOKEN || AUTH_ACCESS_TOKEN.equals("")){
			return false;
		}
		return true;
	}
	
	public void validateLogin(Activity context, ProgressDialog progressDialog){
		
		Log.d("SessionAuth", "triggering login check");
		new ValidateUserLogin(context, progressDialog).execute("");
	}
	
	private class ValidateUserLogin extends AsyncTask<String, Integer, String>{

		Activity context;
		ProgressDialog progressDialog;
		public ValidateUserLogin(Activity context, ProgressDialog progressDialog){
			this.context = context;
			this.progressDialog = progressDialog;
		}
		
		@Override
		protected String doInBackground(String... params) {
			Twitter twitter = twitterApplication.getTwitter();
			if(null != twitter && twitter.isValidLogin()){
				Log.d("SessionAuth", "Looks like we succeeded");
				
				//only saves when it's valid
				Editor editor = twitterApplication.getPrefs().edit();
				editor.putString(AUTH_TOKEN_IDENTIFIER, AUTH_ACCESS_TOKEN);
				editor.putString(AUTH_SECRET_IDENTIFIER, AUTH_ACCESS_SECRET);
				editor.commit();
				
				return IS_VALID;
			}else{
				return INVALID;
			}
		}
		
		@Override
		protected void onPostExecute(String result) {
			if( null != progressDialog ){
				progressDialog.dismiss();
			}
			if( IS_VALID.equals(result) ){
//				context.startService(new Intent(context, UpdaterService.class));
				context.startActivity(new Intent(context, TimelineActivity.class)
										.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
										.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
				context.finish();
			}else{
				Toast.makeText(context, "Login failed, please provide the correct login code", Toast.LENGTH_LONG).show();
			}
		}
		
	}
}

