package com.android.diary;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.plus.Plus;

import Helpers.MessageHelper;

public class LogInActivity extends Activity implements ConnectionCallbacks, OnConnectionFailedListener {
	private static final int RC_SIGN_IN = 0;
	
	private GoogleApiClient googleApiClient;
	private boolean signInClicked;
	private boolean intentInProgress;
	private ConnectionResult connectionResult;
	private GraphUser user;
	
	private SignInButton signInButton;
	private LoginButton loginButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_log_in);
		
		googleApiClient = new GoogleApiClient.Builder(this)
		.addConnectionCallbacks(this)
		.addOnConnectionFailedListener(this)
		.addApi(Plus.API)
		.addScope(Plus.SCOPE_PLUS_LOGIN)
		.build();
		
		signInButton = (SignInButton)findViewById(R.id.google_plus_sign_in_button);
		if(googleApiClient.isConnected()){
			setGooglePlusButtonText(signInButton, getString(R.string.login_btn_log_out));
		}else{
			setGooglePlusButtonText(signInButton, getString(R.string.login_btn_log_in));
		}
		
		signInButton.setOnClickListener(new OnClickListener() {			
			public void onClick(View v) {
				if(!googleApiClient.isConnected()){
					signInClicked = true;
					resolveSignInError();
				}else if(googleApiClient.isConnected()){
					logOut();
				}
			}
		});
		
		loginButton = (LoginButton) findViewById(R.id.fb_login_button);
		loginButton.setReadPermissions("email");
        loginButton.setUserInfoChangedCallback(new LoginButton.UserInfoChangedCallback() {
            @Override
            public void onUserInfoFetched(GraphUser user) {
            	LogInActivity.this.user = user;
            	if(LogInActivity.this.user != null){
            		BaseApplication baseApplication = (BaseApplication) getApplicationContext();
            		baseApplication.setUserInfo(user.getProperty("email").toString(), user.getName(), LoginType.Facebook);
            		MessageHelper.ToastMessage(getApplicationContext(), "Connected: " + user.getProperty("email").toString() + " " + user.getName());
            	}else {
            		BaseApplication baseApplication = (BaseApplication) getApplicationContext();
            		baseApplication.removeUserInfo(LoginType.Facebook);
            	}
            }
        });
	}

	private void setGooglePlusButtonText(SignInButton signInButton, String buttonText) {
	    for (int i = 0; i < signInButton.getChildCount(); i++) {
	        View v = signInButton.getChildAt(i);
	        if (v instanceof TextView) {
	            TextView mTextView = (TextView) v;
	            mTextView.setText(buttonText);
	            return;
	        }
	    }
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		googleApiClient.connect();
	}

	@Override
	protected void onStop() {
		super.onStop();
		if(googleApiClient.isConnected())
			googleApiClient.disconnect();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if(data != null)
			Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
		
		if(requestCode == RC_SIGN_IN){
			if(resultCode != RESULT_OK){
				signInClicked = false;
			}
			
			intentInProgress = false;
			if(!googleApiClient.isConnected()){
				googleApiClient.connect();
			}
		}
	}

	public void onConnectionFailed(ConnectionResult result) {
		if(!intentInProgress){
			connectionResult = result;
			
			if(signInClicked){
				resolveSignInError();
			}
		}
	}

	public void onConnected(Bundle connectionHint) {
		setGooglePlusButtonText(signInButton, getString(R.string.login_btn_log_out));
		signInClicked = false;
		MessageHelper.ToastMessage(getApplicationContext(), "Connected: " + Plus.AccountApi.getAccountName(googleApiClient) + " " + Plus.PeopleApi.getCurrentPerson(googleApiClient).getDisplayName());
		
		BaseApplication baseApplication = (BaseApplication) getApplicationContext();
		baseApplication.setUserInfo(Plus.AccountApi.getAccountName(googleApiClient), Plus.PeopleApi.getCurrentPerson(googleApiClient).getDisplayName(), LoginType.GooglePlus);
	}

	public void onConnectionSuspended(int cause) {
		googleApiClient.connect();
	}
	
	private void resolveSignInError() {
		if (!intentInProgress && connectionResult.hasResolution()) {
			try {
				intentInProgress = true;
				startIntentSenderForResult(connectionResult.getResolution().getIntentSender(), RC_SIGN_IN, null, 0, 0, 0);
		    } catch (SendIntentException e) {
		    	intentInProgress = false;
		    	googleApiClient.connect();
		    }
		}
	}
	
	private void logOut(){
		BaseApplication baseApplication = (BaseApplication) getApplicationContext();
		baseApplication.removeUserInfo(LoginType.GooglePlus);
		if(googleApiClient.isConnected()){
			setGooglePlusButtonText(signInButton, getString(R.string.login_btn_log_in));
			Plus.AccountApi.clearDefaultAccount(googleApiClient);
			googleApiClient.disconnect();
			googleApiClient.connect();
		}
	}
}
