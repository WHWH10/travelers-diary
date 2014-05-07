package com.android.diary;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import android.os.Bundle;
import android.view.View;
import BaseClasses.BaseActivity;
import Helpers.MessageHelper;

public class NewRouteActivity extends BaseActivity {

//	private static final String LOG_TAG = "NEW ROUTE ACTIVITY";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_new_route);
	}
	
	public void exportLogClicked(View view){
		DatabaseHandler db = new DatabaseHandler(getApplicationContext());
		
		List<String> logEntries = db.selectLog();
		db.close();
		
		File file = new File(this.getExternalFilesDir(null).getAbsolutePath() + "/log.txt");
		MessageHelper.ToastMessage(getApplicationContext(), this.getExternalFilesDir(null).getAbsolutePath());
		FileOutputStream fOutputStream;
		try {
			fOutputStream = new FileOutputStream(file);		
			
			for (String logEntry : logEntries) {
				fOutputStream.write(logEntry.getBytes());
			}
			
			fOutputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
}
