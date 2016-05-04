package com.rabbidgames.groupon;

import android.app.Activity;
import com.google.ads.*;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.RelativeLayout;
import android.content.*;
import android.app.AlertDialog;

public class StaticObjects
{
	final static String SETTINGS_KEY = "GrouponBillCalculatorSettings";
	
	public static String FlurryKey()
	{
		return "TFB59AZICKR8ZBFVM88K";
	}
	
	public static String AdKey()
	{
		return "a14f8b1ca65fb6b";
	}
	
	public static String BaseURL()
	{
		return "http://www.socialpioneer.webuda.com/API/";
	}

	public static void BasicAlert(String title, String message, Activity a)
	{
		AlertDialog alertDialog = new AlertDialog.Builder(a).create();
		alertDialog.setTitle(title);
		alertDialog.setMessage(message);
		alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
		   public void onClick(DialogInterface dialog, int which) {
		      dialog.cancel();
		   }
		});
		alertDialog.show();
	}
	
	public static Animation GetProgressAnimation()
	{
		Animation anim;
		anim = new RotateAnimation(0, 0, 0, 0);
		anim.setRepeatMode(Animation.REVERSE);
		anim.setRepeatCount(Animation.INFINITE);
		anim.setDuration(10000L);
		anim.setInterpolator(new AccelerateDecelerateInterpolator());
		
		return anim;
	}
	
	public static void ShowAd(Activity a, boolean bottomAlign)
	{
		RelativeLayout layout = new RelativeLayout(a);
		a.addContentView(layout, new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
	
		if(bottomAlign)
		{
			layout.setVerticalGravity(Gravity.BOTTOM);
		}
		else
		{
			layout.setVerticalGravity(Gravity.CENTER);
		}

		AdView myAd = new AdView(a, AdSize.BANNER,  StaticObjects.AdKey());
		
		myAd.setAdListener( new AdListener()
		{
			public void onPresentScreen(Ad a)
			{
			}
			
			public void onDismissScreen(Ad a)
			{
			}
			
			public void onFailedToReceiveAd(Ad a, AdRequest.ErrorCode error)
			{
			}
	
			public void onReceiveAd(Ad a)
			{
			}
			
			public void onLeaveApplication(Ad a)
			{
			}
		});
		
		myAd.setBackgroundColor(0xff000000);
		myAd.setVisibility(android.view.View.VISIBLE);
		myAd.setGravity(Gravity.CENTER);
		RelativeLayout.LayoutParams rl = (RelativeLayout.LayoutParams)myAd.getLayoutParams();
		rl.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		myAd.setLayoutParams(rl);
		
		layout.addView(myAd);
		
		myAd.loadAd(new AdRequest());
	}
	 
	public static String StripSpecialNumberCharacters(String str)
	{
		 str = str.replace("$", "");
		 str = str.replace("%", "");
		 
		 return str;
	}
	 
	public static String FormatPercent(String str)
	{
		 if(!str.endsWith("%"))
		 {
			 str += "%";
		 }
		 
		 return str;
	}
	 
	public static String FormatCurrency(String str)
	{
		if(str.startsWith(".") || str.equals(""))
		{
			str = "0" + str;
		}
		
		if(!str.startsWith("$"))
		{
			str = "$" + str;
		}
		
		if(!str.contains("."))
		{
			str += ".00";
		}
		
		int dotIndex = str.indexOf(".");
		
		String trailingDigits = dotIndex + 1 < str.length() ? str.substring(dotIndex + 1, str.length())  : "00";
		
		String firstPart = dotIndex > 0 ? str.substring(0, dotIndex)  : "$0";
		
		if(trailingDigits.length() > 2)
		{
			trailingDigits = trailingDigits.substring(0, 2);
		}
		
		for(int i = 0; i < 2 - trailingDigits.length(); ++i)
		{
			trailingDigits += "0";
		}
		
		return firstPart + "." + trailingDigits;
	}
	
	public static float ValidatePositiveFloat(String inputString, String fieldName, Activity activity)
	{
		float returnValue = -1f;
		
		try
		{
			returnValue = Float.parseFloat(StaticObjects.StripSpecialNumberCharacters(inputString));
		}
		catch(NumberFormatException nfe)
    	{
			ShowFloatError(fieldName, activity);
			return -1f;
    	}
		
		if(returnValue >= 0f)
		{
			return returnValue;
		}
		else
		{
			ShowFloatError(fieldName, activity);
			return -1f;
		}
	}
	
	public static void ShowFloatError(String fieldName, Activity activity)
	{
		StaticObjects.BasicAlert(activity.getResources().getText(R.string.error).toString(),
				activity.getResources().getText(R.string.invalid_float).toString()+ " " + fieldName, activity);
	}
	
	public static int ValidatePositiveInt(String inputString, String fieldName, Activity activity)
	{
		int returnValue = -1;
		
		try
		{
			returnValue = Integer.parseInt(inputString);
		}
		catch(NumberFormatException nfe)
    	{
			ShowIntError(fieldName, activity);
			return -1;
    	}
		
		if(returnValue >= 0)
		{
			return returnValue;
		}
		else
		{
			ShowIntError(fieldName, activity);
			return -1;
		}
	}
	
	public static void ShowIntError(String fieldName, Activity activity)
	{
		StaticObjects.BasicAlert(activity.getResources().getText(R.string.error).toString(),
				activity.getResources().getText(R.string.invalid_integer).toString()+ " " + fieldName, activity);
	}
	
	public static boolean CheckIsOnline(Activity a) {
		 ConnectivityManager cm = (ConnectivityManager) a.getBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		 NetworkInfo netInfo = cm.getActiveNetworkInfo();
		 if (netInfo != null && netInfo.isConnected())
		 {
			 return true;
		 }
		 else
		 {
			 return false;
		 }
	}
	
	public static void SetPreferenceFloat(Context context, String key, float value)
	{
		SharedPreferences settings = context.getSharedPreferences(SETTINGS_KEY, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putFloat(key, value);
		editor.commit();
	}
	
	public static float GetPreferenceFloat(Context context, String key, float defaultValue){
		SharedPreferences settings = context.getSharedPreferences(SETTINGS_KEY, 0);
		return settings.getFloat(key, defaultValue);
	}
	
	public static void SetPreferenceString(Context context, String key, String value)
	{
		SharedPreferences settings = context.getSharedPreferences(SETTINGS_KEY, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(key, value);
		editor.commit();
	}
	
	public static String GetPreferenceString(Context context, String key, String defaultValue){
		SharedPreferences settings = context.getSharedPreferences(SETTINGS_KEY, 0);
		return settings.getString(key, defaultValue);
	}
}
