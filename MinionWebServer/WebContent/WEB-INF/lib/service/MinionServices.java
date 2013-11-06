package org.minion.service;

import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Sender;

public class MinionServices {

	private static final Log logger = LogFactory.getLog(MinionServices.class);

	private static final String API_KEY = "AIzaSyB7mBnYm0sTP0WxU5-Teqh54nFOYGbVCis";

	MinionGenericResponse genericResponse;

	public MinionGenericResponse deviceRegister(String userID, String deviceID) {

		long inicio = System.currentTimeMillis();

		logger.info("deviceID=" + deviceID);

		UserDao userDao = new UserDao();

		if (userDao.registerUserDevice(userID, deviceID))
			genericResponse = new MinionGenericResponse(true);
		else
			genericResponse = new MinionGenericResponse(false);

		logger.info("Tiempo: " + (System.currentTimeMillis() - inicio) + " ms.");

		return genericResponse;

	}

	public MinionGenericResponse sendAlert(String userID, String msg) throws IOException {

		long inicio = System.currentTimeMillis();

		UserDao userDao = new UserDao();
		userDao.getUserByID(userID);

		if (userDao.getUsuDispositivo() != null && !userDao.getUsuDispositivo().trim().equals("")) {

			String[] devices = { userDao.getUsuDispositivo() };

			Sender sender = new Sender(API_KEY);

			Message.Builder msgBuilder = new Message.Builder();

			msgBuilder.addData("msg", msg);
			msgBuilder.addData("msgType", "ALARMA");
			msgBuilder.addData("title", userDao.getUsuNombre());

			MulticastResult result = sender.send(msgBuilder.build(), Arrays.asList(devices), 5);

			logger.info("Mensajes enviados exitosamente: " + result.getSuccess());

			logger.info("Mensajes no enviados: " + result.getFailure());

			genericResponse = new MinionGenericResponse(true);

		} else {
			genericResponse = new MinionGenericResponse(false);
			genericResponse.setResponseDesc("No se ha encontrado al usuario destinatario.");
		}

		logger.info("Tiempo: " + (System.currentTimeMillis() - inicio) + " ms.");

		return genericResponse;

	}

}