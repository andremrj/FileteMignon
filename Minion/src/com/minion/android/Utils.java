package com.minion.android;

import java.util.Date;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.text.format.DateFormat;

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
			MnLog.e(Utils.class, "Error al determinar si hay conexion: " + e.getMessage(), e);
			return false;
		}
	}

	public static String formateaFecha(long fecha, Context context, boolean larga) {
		return formateaFecha(new Date(fecha), context, larga);
	}

	public static String formateaFecha(Date fecha, Context context, boolean larga) {
		StringBuilder sb = new StringBuilder();

		sb.append(DateFormat.getDateFormat(context).format(fecha));
		if (larga) {
			sb.append(" ");
			sb.append(DateFormat.getTimeFormat(context).format(fecha));
		}

		return sb.toString();
	}
}
