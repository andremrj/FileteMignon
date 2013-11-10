package com.minion.android;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.app.Application;

public class MinionApplication extends Application {

	private WeakReference<Activity> weakRef;
	private boolean activityVisible;

	private String usuario = null;

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public  Activity getWeakRef() {
		return weakRef.get();
	}

	public void setWeakRef(Activity activity) {
		weakRef = new WeakReference<Activity>(activity);
	}

	public boolean isActivityVisible() {
		return activityVisible;
	}

	public void activityResumed() {
		activityVisible = true;
	}

	public void activityPaused() {
		activityVisible = false;
	}

}