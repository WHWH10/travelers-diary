package Helpers;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.diary.DatabaseHandler;

public class MessageHelper {
	
	public static void LogMessage(Context context, String logTag, String message)
	{
		LogToDatabase(context, message, "", logTag);
		Log.i(logTag, message);
	}
	
	public static void LogErrorMessage(Context context, String logTag, String message)
	{
		LogToDatabase(context, message, "", logTag);		
		Log.e(logTag, message);
	}
	
	public static void LogWarningMessage(Context context, String logTag, String message)
	{
		LogToDatabase(context, message, "", logTag);		
		Log.w(logTag, message);
	}
	
	public static void ToastMessage(Context context, String message)
	{
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}
	
	public static void ToastMessageLong(Context context, String message)
	{
		Toast.makeText(context, message, Toast.LENGTH_LONG).show();
	}
	
	private static void LogToDatabase(Context context, String message, String user, String tag)
	{
		DatabaseHandler db = new DatabaseHandler(context);
		db.insertLog(message, user, tag);
		db.close();
	}
}
