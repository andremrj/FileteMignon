package com.minion.android;

import java.io.IOException;

import java.util.Arrays;

import org.apache.http.NameValuePair;

import org.apache.http.client.ClientProtocolException;

import org.apache.http.client.HttpClient;

import org.apache.http.client.entity.UrlEncodedFormEntity;

import org.apache.http.client.methods.HttpPost;

import org.apache.http.impl.client.DefaultHttpClient;

import org.apache.http.message.BasicNameValuePair;

import android.content.Context;

import android.os.AsyncTask;

import android.util.Log;

public class JHHelper {

	// OpenShift
	// static final String URL_SERVLET = "http://dresi-minionserv.rhcloud.com/MinionServer/RegistrationServlet";

	// Local
	static final String URL_SERVLET = "http://192.168.1.15:9090/MinionServer/RegistrationServlet";

	public static void registerTomcat(final Context context, String registration) {

		Log.d("PUTA", "Registrando en tomcat");

		new AsyncTask<String, Void, String>() {

			@Override
			protected String doInBackground(String... registrations) {

				return register(context, registrations[0]);

			}

			@Override
			protected void onPostExecute(String registration) {

			}

			private String register(Context context, String registration) {

				HttpClient client = new DefaultHttpClient();

				try {

					HttpPost post = new HttpPost(URL_SERVLET);

					post.setEntity(new UrlEncodedFormEntity(

					Arrays.asList(new NameValuePair[] { new BasicNameValuePair("ID",

					registration) })));

					int httpStatus = client.execute(post).getStatusLine().getStatusCode();

					if (httpStatus < 400) {

						Log.d("PUTA", " El https status " + httpStatus);

						return registration;

					} else {

						Log.d("PUTA", " El https status es mas de 400 " + httpStatus);

					}

				} catch (ClientProtocolException exception) {

					Log.e("PUTA", " Error al conectar con el servidor TOMCAT");

					exception.printStackTrace();

				} catch (IOException exception) {

					Log.e("PUTA", "Error 2 al conectar con el servidor TOMCAT");

					exception.printStackTrace();

				}

				return null;

			}

		}.execute(registration);

	}

}