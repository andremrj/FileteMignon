package com.minion.android;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.util.Log;

public class Utils {

	public static boolean hayConexionInternet(Context context) {
		try {
			ConnectivityManager cMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo netInfo = cMgr.getActiveNetworkInfo();
			if ((netInfo != null) && (netInfo.getState() != null)) {
				return netInfo.getState().equals(State.CONNECTED);
			}
			return false;
		} catch (Exception e) {
			Log.v("Puta", "Error al determinar si hay conexion: " + e.getMessage(), e);
			return false;
		}
	}

}
