package com.android.diary;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

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
	
	private SignInButton signInButton;
	
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
		MessageHelper.ToastMessage(getApplicationContext(), "Connected: " + Plus.AccountApi.getAccountName(googleApiClient));
		
		BaseApplication baseApplication = (BaseApplication) getApplicationContext();
		baseApplication.setUsername(Plus.AccountApi.getAccountName(googleApiClient));		
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
		baseApplication.setUsername(null);
		if(googleApiClient.isConnected()){
			setGooglePlusButtonText(signInButton, getString(R.string.login_btn_log_in));
			Plus.AccountApi.clearDefaultAccount(googleApiClient);
			googleApiClient.disconnect();
			googleApiClient.connect();
		}
	}
}
