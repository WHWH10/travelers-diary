package com.android.diary;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.plus.Plus;

import Helpers.MessageHelper;

public class LogInActivity extends Activity implements ConnectionCallbacks, OnConnectionFailedListener {
	private static final int RC_SIGN_IN = 0;
	public static final String LOG_OUT_TAG = "logOutTag";
	
	private GoogleApiClient googleApiClient;
	private boolean signInClicked;
	private boolean intentInProgress;
	private ConnectionResult connectionResult;
	
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
				
		findViewById(R.id.google_plus_sign_in_button).setOnClickListener(new OnClickListener() {			
			public void onClick(View v) {
				if(!googleApiClient.isConnected()){
					signInClicked = true;
					resolveSignInError();
				}else if(googleApiClient.isConnected()){
					Plus.AccountApi.clearDefaultAccount(googleApiClient);
					googleApiClient.disconnect();
					googleApiClient.connect();
				}
			}
		});
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
//		if(isLogOutNeeded() && !signInClicked){
//			logOut();
//		}
		
		signInClicked = false;
		MessageHelper.ToastMessage(getApplicationContext(), "Connected: " + Plus.AccountApi.getAccountName(googleApiClient));
		
		BaseApplication baseApplication = (BaseApplication) getApplicationContext();
		baseApplication.setUserName(Plus.AccountApi.getAccountName(googleApiClient));
		
		finish();		
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
	
	private boolean isLogOutNeeded(){
		Bundle bundle = getIntent().getExtras();
		if(bundle != null){
			return bundle.getBoolean(LOG_OUT_TAG, false);
		}
		
		return false;
	}
	
	private void logOut(){
		BaseApplication baseApplication = (BaseApplication) getApplicationContext();
		baseApplication.setUserName(null);
		if(googleApiClient.isConnected()){
			Plus.AccountApi.clearDefaultAccount(googleApiClient);
			googleApiClient.disconnect();
			googleApiClient.connect();
		}
	}
}
