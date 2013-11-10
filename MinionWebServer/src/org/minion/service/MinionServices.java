package org.minion.service;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
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

	public MinionGenericResponse sendAlert(String userSend, String userRecive, String msg) throws IOException {

		long inicio = System.currentTimeMillis();

		/*
		 * TODO:De momento como los mensajes son entre ella y yo realizamos una simple validación del usuario
		 * destinatario
		 */

		UserDao userDao = new UserDao();
		userDao.getUserByID(userSend);

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

	public MinionGenericResponse sendMessage(String userSend, String userReceive, String msg) {

		long inicio = System.currentTimeMillis();

		logger.info("Registrando mensaje del usuario=" + userSend + " para el usuario=" + userReceive + " con texto="
				+ msg);

		/*
		 * TODO:De momento como los mensajes son entre ella y yo realizamos una simple validación del usuario
		 * destinatario
		 */
		try {
			UserDao userDao = new UserDao();
			userDao.getUserByID(userReceive);

			UserDao userDaoEnvia = new UserDao();
			userDaoEnvia.getUserByID(userSend);

			if (userDao.getUsuDispositivo() != null && !userDao.getUsuDispositivo().trim().equals("")
					&& userDaoEnvia.getUsuDispositivo() != null && !userDaoEnvia.getUsuDispositivo().trim().equals("")) {

				String[] devices = { userDao.getUsuDispositivo() };

				Sender sender = new Sender(API_KEY);

				MensajeDao mensajeDao = new MensajeDao();
				mensajeDao.setMenFecha(System.currentTimeMillis());
				mensajeDao.setMenTexto(msg);
				mensajeDao.setMenUsuEnvia(userDaoEnvia.getUsuID());
				mensajeDao.setMenUsuRecibe(userDao.getUsuID());
				mensajeDao.registerMessage();

				Message.Builder msgBuilder = new Message.Builder();

				msgBuilder.addData("msg", msg);
				msgBuilder.addData("msgType", "message");
				msgBuilder.addData("title", userDaoEnvia.getUsuNombre());

				MulticastResult result = sender.send(msgBuilder.build(), Arrays.asList(devices), 5);

				if (result.getSuccess() > 0) {

					logger.info("Mensajes enviados exitosamente: " + result.getSuccess());

					mensajeDao.updateEstado(MensajeDao.ESTADO_PROCESADO);
					genericResponse = new MinionGenericResponse(true);

				} else {
					logger.warn("Mensajes no enviados: " + result.getFailure());
					genericResponse = new MinionGenericResponse(false);
					genericResponse
							.setResponseDesc("No se ha podido enviar el mensaje, avisa a André porque algo no va bien ;-).");

				}

			} else {
				logger.warn("No se ha encontrado al usuario destinatario: " + userReceive
						+ " o no tiene ningún dispositivo asociado.");

				genericResponse = new MinionGenericResponse(false);
				genericResponse.setResponseDesc("No se ha encontrado al usuario destinatario.");
			}

		} catch (Exception e) {

			genericResponse = new MinionGenericResponse(false);
			genericResponse.setResponseDesc("Ha ocurrido un error enviando el mensaje");

			logger.error("Ha ocurrido un error enviando el mensaje : ", e);
		}

		logger.info("Tiempo: " + (System.currentTimeMillis() - inicio) + " ms.");

		return genericResponse;

	}

	public MinionMensajeResponse getConversation(String userSend, String userReceive) {

		long inicio = System.currentTimeMillis();

		logger.info("Obteniendo conversacion entre el usuario=" + userSend + " y el usuario=" + userReceive);

		MinionMensajeResponse mensajeResponse = null;

		/*
		 * TODO:De momento como los mensajes son entre ella y yo realizamos una simple validación del usuario
		 * destinatario
		 */
		try {
			UserDao userDao = new UserDao();
			userDao.getUserByID(userReceive);

			if (userDao.getUsuDispositivo() != null && !userDao.getUsuDispositivo().trim().equals("")) {

				mensajeResponse = new MinionMensajeResponse();
				MensajeDao mensajeDao = new MensajeDao();

				ArrayList<MensajeDao> utilArrayList = mensajeDao.getConversation(userSend, userReceive);
				ArrayList<MensajeDto> mensajeDtoList = new ArrayList<MensajeDto>();


				for (int i = 0; i < utilArrayList.size(); i++) {
					mensajeDtoList.add(new MensajeDto(utilArrayList.get(i)));
				}

				mensajeResponse.setMensajeDtoList(mensajeDtoList);
				mensajeResponse.setResponseCode(0);
				mensajeResponse.setResponseDesc("OK");

			} else {
				logger.warn("No se ha encontrado al usuario destinatario: " + userReceive
						+ " o no tiene ningún  dispositivo asociado.");

				mensajeResponse = new MinionMensajeResponse(false);
				mensajeResponse.setResponseDesc("No se ha encontrado al usuario destinatario.");
			}

		} catch (Exception e) {

			mensajeResponse = new MinionMensajeResponse(false);
			mensajeResponse.setResponseDesc("Ha ocurrido un error enviando el mensaje");

			logger.error("Ha ocurrido un error enviando el mensaje : ", e);
		}

		logger.info("Tiempo: " + (System.currentTimeMillis() - inicio) + " ms.");

		return mensajeResponse;

	}
}