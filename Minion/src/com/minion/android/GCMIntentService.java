package com.minion.android;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.widget.Toast;

import com.google.android.gcm.GCMBaseIntentService;

public class GCMIntentService extends GCMBaseIntentService {

	private static final String SENDER_ID = "1083065749294";

	public static int numMessages = 0;
	public static ArrayList<String> mesagges = new ArrayList<String>();

	Activity activityW;
	String msg;
	MinionApplication application;

	public GCMIntentService() {

		super(SENDER_ID);

	}

	@Override
	protected void onError(Context ctx, String registrationId) {

		MnLog.d(this, "Error recibido");

	}

	@Override
	protected void onMessage(Context ctx, Intent intent) {

		application = (MinionApplication) ((Activity) ctx).getApplication();

		if (application.isActivityVisible()) {
			activityW = application.getWeakRef();

			if (activityW instanceof MainActivity) {

				if (intent.getExtras().getString("msg") != null
						&& !intent.getExtras().getString("msg").trim().equals("")) {

					msg = intent.getExtras().getString("msg");

					activityW.runOnUiThread(new Runnable() {
						public void run() {

							((MainActivity) activityW).txtMensaje.setText(msg);
							Toast.makeText(activityW, "Nueva Alerta Recibida", Toast.LENGTH_SHORT).show();
						}
					});

				}
			}

		} else {

			if (intent.getExtras().getString("msg") != null && !intent.getExtras().getString("msg").trim().equals("")) {

				String msg = intent.getExtras().getString("msg");
				String titulo = intent.getExtras().getString("title");
				String tipoMsg = intent.getExtras().getString("msgType");

				MnLog.d(this, tipoMsg);

				mesagges.add(msg);

				NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

				NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).setSmallIcon(
						R.drawable.ic_launcher_1).setContentTitle(titulo);
				// .setAutoCancel(true);

				mBuilder.setContentText(msg).setNumber(++numMessages);

				// Issue the notification here.

				Intent resultIntent = new Intent(this, MainActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("msg", msg);
				resultIntent.putExtras(bundle);

				int notifyID = 1;
				// The stack builder object will contain an artificial back stack for the
				// started Activity.
				// This ensures that navigating backward from the Activity leads out of
				// your application to the Home screen.
				TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
				// Adds the back stack for the Intent (but not the Intent itself)
				stackBuilder.addParentStack(MainActivity.class);
				// Adds the Intent that starts the Activity to the top of the stack
				stackBuilder.addNextIntent(resultIntent);
				PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
				mBuilder.setContentIntent(resultPendingIntent);

				mNotificationManager.notify(notifyID, mBuilder.build());

				MnLog.d(this, msg);
			}
		}

	}

	@Override
	protected void onRegistered(Context ctx, String regId) {

		// Posteriormente enviaremos el id al Tomcat

		MnLog.d(this, "Registro recibido " + regId);

		MinionWSClient client = new MinionWSClient();
		client.execute(MinionWSClient.DEVICE_REGISTER_OPERATION, regId);

	}

	@Override
	protected void onUnregistered(Context ctx, String regId) {

		// Posteriormente enviaremos el id al Tomcat

		MnLog.d(this, "Baja:" + regId);

	}

}