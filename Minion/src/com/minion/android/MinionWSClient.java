package com.minion.android;

import java.io.IOException;
import java.util.List;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class MinionWSClient extends AsyncTask<String, Integer, MinionGenericResponse> {

	private static final String NAMESPACE = "http://service.minion.org";
	private static final String URL = "http://serverdown.no-ip.org/MinionWebServer/services/MinionServices.MinionServicesHttpSoap11Endpoint/";
										  
	/* CONSTANTES */
	public static final String DEVICE_REGISTER_OPERATION = "deviceRegister";
	public static final String SEND_ALERT_OPERATION = "sendAlert";

	/* deviceRegister */
	private static final String SOAP_ACTION_DEVICEREGISTER = "http://service.minion.org/wakeUpPia";
	private static final String SOAP_METHOD_DEVICEREGISTER = "deviceRegister";

	/* sendAlert */
	private static final String SOAP_ACTION_SENDALERT = "http://service.minion.org/sendAlert";
	private static final String SOAP_METHOD_SENDALERT = "sendAlert";

	private MinionGenericResponse result = new MinionGenericResponse();

	private Context context;
	private SoapSerializationEnvelope envelope;
	private HttpTransportSE transport;

	public MinionWSClient(Context context) {
		this.context = context;
		this.envelope = null;
		this.transport = null;
	}

	private MinionGenericResponse deviceRegister(String userID, String deviceID) {
		SoapObject request = new SoapObject(NAMESPACE, SOAP_METHOD_DEVICEREGISTER);
		request.addProperty("userID", userID);
		request.addProperty("deviceID", deviceID);

		envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		envelope.dotNet = true;
		envelope.setOutputSoapObject(request);
		transport = new HttpTransportSE(URL);
		try {
			transport.call(SOAP_ACTION_DEVICEREGISTER, envelope);
			SoapObject player = (SoapObject) envelope.getResponse();
			// SoapObject player = (SoapObject) resultsRequestSOAP.getProperty("deviceRegisterResponse");
			String responseCode = player.getProperty("responseCode").toString();
			String responseDesc = player.getProperty("responseDesc").toString();

			Log.v("PUTA", "responseCode:" + responseCode + "|responseDesc:" + responseDesc);
		} catch (Exception ex) {
			result = null;
			Log.v("PUTA", Log.getStackTraceString(ex));
		}

		return result;
	}

	private MinionGenericResponse sendAlert(String userID, String msg) {

		SoapObject request = new SoapObject(NAMESPACE, SOAP_METHOD_SENDALERT);
		request.addProperty("userID", userID);
		request.addProperty("msg", msg);

		envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		envelope.dotNet = true;
		envelope.setOutputSoapObject(request);
		transport = new HttpTransportSE(URL);
		List hola = null;
		try {
			hola = transport.getServiceConnection().getResponseProperties();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (int i = 0; i < hola.size(); i++) {
			Log.v("PUTA", "Elemento" + hola.get(i));
		}

		try {
			transport.call(SOAP_ACTION_SENDALERT, envelope);
			SoapObject player = (SoapObject) envelope.getResponse();
			// SoapObject player = (SoapObject) resultsRequestSOAP.getProperty("deviceRegisterResponse");
			String responseCode = player.getProperty("responseCode").toString();
			String responseDesc = player.getProperty("responseDesc").toString();

			Log.v("PUTA", "responseCode:" + responseCode + "|responseDesc:" + responseDesc);

		} catch (Exception ex) {
			result = null;
			// Log.v("PUTA", Log.getStackTraceString(ex));
			Log.e("PUTA", "I got an error", ex);
		}

		return result;
	}

	@Override
	protected MinionGenericResponse doInBackground(String... paramArrayOfParams) {

		MinionGenericResponse response = null;

		if (Utils.hayConexionInternet(context)) {

			if (paramArrayOfParams[0].equals(DEVICE_REGISTER_OPERATION)) {

				response = deviceRegister(paramArrayOfParams[1], paramArrayOfParams[2]);
			}
			if (paramArrayOfParams[0].equals(SEND_ALERT_OPERATION)) {

				response = sendAlert(paramArrayOfParams[1], paramArrayOfParams[2]);
			}
		}
		return response;
	}
}