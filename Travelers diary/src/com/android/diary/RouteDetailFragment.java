package com.android.diary;

import BaseClasses.BaseFragment;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import com.android.diary.R;

public class RouteDetailFragment extends BaseFragment {
	
	public static final String ROUTE_ID = "routeId";
	private ImageButton btnEditTitle;
	private ImageButton btnEditDescription;
	private String title;
	private String description;
	TextView tvTitle;
	TextView tvDescription;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		if(container == null)
			return null;
		
		View view = inflater.inflate(R.layout.fragment_route_detail, container, false);
		initializeButtons(view);
		this.title = "";
		setValues(view);
		return view;
	}
	
	private void initializeButtons(View view)
	{
		btnEditTitle = (ImageButton) view.findViewById(R.id.route_det_title_btnEdit);
		btnEditTitle.setOnClickListener(new android.view.View.OnClickListener() {			
			public void onClick(View v) {
				changeRouteTitlePressed();
			}
		});
		
		btnEditDescription = (ImageButton) view.findViewById(R.id.route_det_description_btnEdit);
		btnEditDescription.setOnClickListener(new android.view.View.OnClickListener() {			
			public void onClick(View v) {
				changeRouteDescriptionPressed();
			}
		});
	}
	
	public static RouteDetailFragment newInstance(int routeId)
	{
		RouteDetailFragment fragment = new RouteDetailFragment();
		 
		Bundle args = new Bundle();
		args.putInt(ROUTE_ID, routeId);
		fragment.setArguments(args);
		return fragment;
	}
	
	private int getRouteID()
	{
		if(getArguments() == null)
			return 0;
		return getArguments().getInt(ROUTE_ID, 0);
	}
	
	private void setValues(View view)
	{
		DatabaseHandler db = new DatabaseHandler(getActivity());
		Route route = db.getRoute(getRouteID());
		db.close();
		
		if(route == null)
			return;
		
		tvTitle = (TextView) view.findViewById(R.id.route_det_title_textView);
		tvTitle.setText(route.getTitle());
		
		tvDescription = (TextView) view.findViewById(R.id.route_det_description_textView);
		tvDescription.setText(route.getDescription());

		TextView dateCreaded = (TextView) view.findViewById(R.id.route_det_created_textView);
		dateCreaded.setText(route.getDateCreated().toString());
		
		TextView dateModified = (TextView) view.findViewById(R.id.route_det_modified_textView);
		dateModified.setText(route.getDateModified().toString());
	}
	
	private void changeRouteTitlePressed()
	{
		AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
		final EditText editText = new EditText(getActivity());
		editText.setHint(R.string.hint_title);
		editText.setText(tvTitle.getText());
		editText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
		final InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
		
		dialog.setView(editText);
		dialog.setNeutralButton(R.string.dialog_set, new OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				title = editText.getText().toString();
				changeTitle();
				getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
			}
		});
		dialog.setNegativeButton(R.string.dialog_cancel, new OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		dialog.show();
		editText.setSelection(editText.getText().length());
	}
	
	private void changeRouteDescriptionPressed()
	{
		AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
		final EditText editText = new EditText(getActivity());
		editText.setHint(R.string.hint_description);
		editText.setText(tvDescription.getText());
		editText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
		final InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
		
		dialog.setView(editText);
		dialog.setNeutralButton(R.string.dialog_set, new OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				description = editText.getText().toString();
				changeDescription();
				getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
			}
		});
		dialog.setNegativeButton(R.string.dialog_cancel, new OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		dialog.show();
		editText.setSelection(editText.getText().length());
	}
	
	private void changeTitle()
	{
		if(!title.isEmpty() && getRouteID() != 0)
		{
			ContentValues contentValues = new ContentValues();
			contentValues.put(DatabaseHandler.KEY_TITLE, title);
			
			DatabaseHandler db = new DatabaseHandler(getActivity());
			db.updateRoute(contentValues, getRouteID());
			db.close();
			
			tvTitle.setText(title);
		}
	}
	
	private void changeDescription()
	{
		if(!description.isEmpty() && getRouteID() != 0)
		{
			ContentValues contentValues = new ContentValues();
			contentValues.put(DatabaseHandler.KEY_DESCRIPTION, description);
			
			DatabaseHandler db = new DatabaseHandler(getActivity());
			db.updateRoute(contentValues, getRouteID());
			db.close();
			
			tvDescription.setText(description);
		}
	}
}
