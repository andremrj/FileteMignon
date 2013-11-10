package com.minion.android;

import android.util.Log;

public class MnLog {

	private static String CAT = "MnLog";

	private static boolean debug = false;
	private static boolean info = false;
	private static boolean warn = true;
	private static boolean error = true;

	public static void d(Object objeto, String mensaje) {
		d(objeto, mensaje, null);
	}

	public static void d(Object objeto, String mensaje, Throwable e) {
		try {
			if (debug) {
				Log.d(CAT + objeto.getClass().getName(), mensaje, e);
			}
		} catch (Exception ex) {
			Log.w(CAT, "Error al pintar el mensaje: " + e.getMessage(), e);
		}
	}

	public static void i(Object objeto, String mensaje) {
		i(objeto, mensaje, null);
	}

	public static void i(Object objeto, String mensaje, Throwable e) {
		try {
			if (info) {
				Log.i(CAT + objeto.getClass().getName(), mensaje, e);
			}
		} catch (Exception ex) {
			Log.w(CAT, "Error al pintar el mensaje: " + e.getMessage(), e);
		}
	}

	public static void w(Object objeto, String mensaje) {
		w(objeto, mensaje, null);
	}

	public static void w(Object objeto, String mensaje, Throwable e) {
		try {
			if (warn) {
				Log.w(CAT + objeto.getClass().getName(), mensaje, e);
			}
		} catch (Exception ex) {
			Log.w(CAT, "Error al pintar el mensaje: " + e.getMessage(), e);
		}
	}

	public static void e(Object objeto, String mensaje) {
		e(objeto, mensaje, null);
	}

	public static void e(Object objeto, String mensaje, Throwable e) {
		try {
			if (error) {
				Log.e(CAT + objeto.getClass().getName(), mensaje, e);
			}
		} catch (Exception ex) {
			Log.w(CAT, "Error al pintar el mensaje: " + e.getMessage(), e);
		}
	}

	public static boolean isDebug() {
		return debug;
	}
}
