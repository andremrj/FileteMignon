package com.minion.android;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.app.Application;

public class MyApplication extends Application {

	static WeakReference<Activity> weakRef;

	public static Activity getWeakRef() {
		return weakRef.get();
	}

	public static void setWeakRef(Activity activity) {
		MyApplication.weakRef = new WeakReference<Activity>(activity);
	}

	public static boolean isActivityVisible() {
		return activityVisible;
	}

	public static void activityResumed() {
		activityVisible = true;
	}

	public static void activityPaused() {
		activityVisible = false;
	}

	private static boolean activityVisible;
}