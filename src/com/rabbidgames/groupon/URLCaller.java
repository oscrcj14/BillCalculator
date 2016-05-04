package com.rabbidgames.groupon;

import android.app.Activity;

public interface URLCaller
{
	public void SuccessCallback(String result);
	public void ErrorCallback(String error);
	public Activity GetActivity();
}
