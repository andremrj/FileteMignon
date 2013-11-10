package org.minion.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class UserDao {

	private static final Log logger = LogFactory.getLog(UserDao.class);

	private String usuID;
	private String usuNombre;
	private String usuDispositivo;

	public UserDao() {

		usuID = null;
		usuNombre = null;
		usuDispositivo = null;

	}

	public String getUsuID() {
		return usuID;
	}

	public void setUsuID(String usuID) {
		this.usuID = usuID;
	}

	public String getUsuNombre() {
		return usuNombre;
	}

	public void setUsuNombre(String usuNombre) {
		this.usuNombre = usuNombre;
	}

	public String getUsuDispositivo() {
		return usuDispositivo;
	}

	public void setUsuDispositivo(String usuDispositivo) {
		this.usuDispositivo = usuDispositivo;
	}

	public boolean registerUserDevice(String userID, String deviceID) {

		logger.debug("Registrando el id de dispositivo para el usuario = " + userID);

		Connection connection = null;
		PreparedStatement ps = null;
		boolean registroCorrecto = false;
		try {
			connection = FuenteDatos.openBDConnection();

			ps = connection.prepareStatement("UPDATE T_USUARIO SET USU_DISPOSITIVO = ? WHERE USU_ID = ?");

			int i = 1;

			ps.setString(i, deviceID);
			i++;
			ps.setString(i, userID);

			i = ps.executeUpdate();

			if (i > 0) {
				registroCorrecto = true;
			}

			logger.debug("Número de Registros actualizados: " + i);

		} catch (SQLException e) {

			logger.error("Ha ocurrido un error" + e.getMessage(), e);
		} finally {
			FuenteDatos.closeBDConnection(connection, ps, null);

		}
		return registroCorrecto;

	}

	public void getUserByID(String userID) {

		if (userID == null || userID.trim().equals("")) {
			logger.error("ID asignado al intentar cargar un usuario.");
			return;
		}

		logger.debug("Cargando usuario con USU_ID = " + userID);

		this.usuID = userID;

		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			connection = FuenteDatos.openBDConnection();

			ps = connection
					.prepareStatement("SELECT USU_ID, USU_NOMBRE, USU_DISPOSITIVO FROM  T_USUARIO  WHERE USU_ID = ?");

			int i = 1;

			ps.setString(i, userID);

			rs = ps.executeQuery();

			if (rs.next()) {
				asignarAtributos(rs);
			} else {
				logger.debug("No se ha encontrado ningún usuario con el USU_ID = " + userID);
			}
		} catch (SQLException e) {
			logger.error("Ha ocurrido un error" + e.getMessage(), e);
		} finally {
			FuenteDatos.closeBDConnection(connection, ps, null);
		}

	}

	private void asignarAtributos(ResultSet rs) throws SQLException {

		logger.debug("Asignando atributos para el usuario USU_ID = " + usuID);

		if (!rs.wasNull()) {
			this.setUsuID(new String(rs.getString("USU_ID")));
		} else {
			this.setUsuID(null);
		}

		if (!rs.wasNull()) {
			this.setUsuNombre(new String(rs.getString("USU_NOMBRE")));
		} else {
			this.setUsuNombre(null);
		}

		if (!rs.wasNull()) {
			this.setUsuDispositivo(new String(rs.getString("USU_DISPOSITIVO")));
		} else {
			this.setUsuDispositivo(null);
		}

	}
}