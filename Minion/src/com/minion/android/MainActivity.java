package com.minion.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;

public class MainActivity extends Activity {

	private static final String SENDER_ID = "1083065749294";

	public TextView txtMensaje;
	private Button registrarbtn;
	private Button enviarbtn;
	private EditText mensajetxt;

	AlertDialog alertDialog;

	// QUITAAAAAAAAAMEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE
	public static String temporalDeregistro;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		MyApplication.setWeakRef(MainActivity.this);

		setContentView(R.layout.activity_main);

		GCMRegistrar.checkDevice(this);

		GCMRegistrar.checkManifest(this);

		final String regId = GCMRegistrar.getRegistrationId(this);

		if (regId.equals("")) {

			// Lanzamos el registro

			GCMRegistrar.register(this, SENDER_ID);

		} else {
			temporalDeregistro = regId;

			Log.v("PUTA", "Ya esta registrado");

		}

		String mensaje = null;
		txtMensaje = (TextView) findViewById(R.id.sex1);
		mensajetxt = (EditText) findViewById(R.id.msgtext);

		registrarbtn = (Button) findViewById(R.id.registrar);
		registrarbtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Log.v("PUTA", "Temporal de registro:" + temporalDeregistro);
				showDialog(0);
			}
		});

		enviarbtn = (Button) findViewById(R.id.enviar);
		enviarbtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Log.v("PUTA", "Temporal de registro:" + temporalDeregistro);
				MinionWSClient client = new MinionWSClient(MainActivity.this);
				client.execute(MinionWSClient.SEND_ALERT_OPERATION, "mpia", mensajetxt.getText().toString());

				Toast.makeText(getApplicationContext(), "Alerta enviada, MP!!", Toast.LENGTH_SHORT).show();
			}
		});

		if (this.getIntent().getExtras() != null) {

			if (this.getIntent().getExtras().getString("msg") != null)
				mensaje = this.getIntent().getExtras().getString("msg");

			txtMensaje.setText(mensaje);
		}

	}

	@Override
	protected Dialog onCreateDialog(int id) {
		LayoutInflater factory;

		View layout;
		destroyDialog();
		switch (id) {
		case 0:
			factory = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
			layout = factory.inflate(R.layout.dialogo_add_mail, null);
			final TextView registrotext = (TextView) layout.findViewById(R.id.adm_txt_mail);
			registrotext.setText("");
			alertDialog = new AlertDialog.Builder(this)
					.setMessage("Introduce tu usuario amor: \n(mpia)").setView(layout)
					.setTitle("Registro").setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {

							dialog.dismiss();

							MinionWSClient client = new MinionWSClient(MainActivity.this);
							client.execute(MinionWSClient.DEVICE_REGISTER_OPERATION, registrotext.getText().toString(),
									temporalDeregistro);

							registrotext.setText("");
							Toast.makeText(getApplicationContext(), "Muy bien amor, muy bien!", Toast.LENGTH_SHORT)
									.show();
						}
					}).create();

		}

		return alertDialog;
	}

	private void destroyDialog() {
		try {
			if (alertDialog != null && alertDialog.isShowing()) {
				alertDialog.dismiss();
			}
		} catch (Exception e) {
			Log.v("PUTA", "Error: " + e.getMessage(), e);
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		MyApplication.activityResumed();
		MyApplication.setWeakRef(MainActivity.this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MyApplication.activityPaused();

		MyApplication.setWeakRef(MainActivity.this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.main, menu);

		return true;

	}

}