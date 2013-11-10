package org.minion.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MensajeDao {

	private static final Log logger = LogFactory.getLog(MensajeDao.class);

	public final static String ESTADO_PROCESADO = "PROC";
	public final static String ESTADO_FALLIDO = "FAIL";
	public final static String ESTADO_PENDIENTE = "PDTE";

	private Integer menID;
	private long menFecha;
	private String menUsuEnvia;
	private String menUsuRecibe;
	private String menTexto;
	private String menEstado;

	public MensajeDao() {

		menID = null;
		menUsuEnvia = null;
		menUsuRecibe = null;
		menTexto = null;
		menEstado = null;

	}

	public Integer getMenID() {
		return menID;
	}

	public void setMenID(Integer menID) {
		this.menID = menID;
	}

	public long getMenFecha() {
		return menFecha;
	}

	public void setMenFecha(long menFecha) {
		this.menFecha = menFecha;
	}

	public String getMenUsuEnvia() {
		return menUsuEnvia;
	}

	public void setMenUsuEnvia(String menUsuEnvia) {
		this.menUsuEnvia = menUsuEnvia;
	}

	public String getMenUsuRecibe() {
		return menUsuRecibe;
	}

	public void setMenUsuRecibe(String menUsuRecibe) {
		this.menUsuRecibe = menUsuRecibe;
	}

	public String getMenTexto() {
		return menTexto;
	}

	public void setMenTexto(String menTexto) {
		this.menTexto = menTexto;
	}

	public String getMenEstado() {
		return menEstado;
	}

	public void setMenEstado(String menEstado) {
		this.menEstado = menEstado;
	}

	public boolean registerMessage() throws Exception {

		logger.debug("Insertando mensaje del usuario = " + this.menUsuEnvia + " para el usuario = " + this.menUsuRecibe);

		if (this.menUsuEnvia == null || this.menUsuRecibe == null || this.menTexto == null) {
			throw new Exception("Error insertando el mensaje, alguno de los atributos obligatorios es nulo");
		}

		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean successInsert = false;

		Integer auxSeq = null;

		try {
			connection = FuenteDatos.openBDConnection();

			ps = connection.prepareStatement("SELECT (MAX(MEN_ID) + 1) AS NEXT_ID FROM T_MENSAJE");

			rs = ps.executeQuery();

			while (rs.next()) {
				auxSeq = new Integer(rs.getInt("NEXT_ID"));
				setMenID(auxSeq);
			}

			ps = connection
					.prepareStatement("INSERT INTO T_MENSAJE (MEN_FECHA, MEN_USU_ENVIA, MEN_USU_RECIBE, MEN_TEXTO, MEN_ESTADO) SELECT ?,?,?,?,? FROM DUAL");

			int i = 1;

			ps.setTimestamp(i, new Timestamp(this.menFecha));
			i++;
			ps.setString(i, this.menUsuEnvia);
			i++;
			ps.setString(i, this.menUsuRecibe);
			i++;
			ps.setString(i, this.menTexto);
			i++;
			ps.setString(i, ESTADO_PENDIENTE);

			successInsert = ps.execute();

			logger.debug("Resultado de la inserción " + successInsert);

		} catch (SQLException e) {

			logger.error("Ha ocurrido un error" + e.getMessage(), e);
		} finally {
			FuenteDatos.closeBDConnection(connection, ps, null);

		}
		return successInsert;

	}

	public ArrayList<MensajeDao> getConversation(String userSend, String userReceive) {

		if (userSend == null || userSend.trim().equals("") || userReceive == null || userReceive.trim().equals("")) {
			logger.error("ID de usuario no asignados al intentar recuperar una conversación.");
			return null;
		}

		logger.debug("Cargando conversación entre los usuarios userSend = " + userSend + " y userReceive = "
				+ userReceive);

		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<MensajeDao> msgList = new ArrayList<MensajeDao>();

		try {
			connection = FuenteDatos.openBDConnection();

			ps = connection
					.prepareStatement("SELECT MEN_ID, MEN_FECHA, MEN_USU_ENVIA, MEN_USU_RECIBE, MEN_TEXTO, MEN_ESTADO FROM T_MENSAJE WHERE MEN_USU_ENVIA IN (?,?) AND MEN_USU_RECIBE IN (?,?)");

			int i = 1;
			int auxCuentaNumeroMensajes = 0;

			ps.setString(i, userSend);
			i++;
			ps.setString(i, userReceive);
			i++;
			ps.setString(i, userSend);
			i++;
			ps.setString(i, userReceive);

			rs = ps.executeQuery();

			while (rs.next()) {

				MensajeDao dao = new MensajeDao();
				dao.asignarAtributos(rs);
				dao.setMenEstado(ESTADO_PROCESADO);
				msgList.add(dao);

				auxCuentaNumeroMensajes++;
			}
			// Como los estamos recuperando todos para pintarlos nos aseguramos de actualizarlos a estado Procesado
			// TODO: Aquí deberíamos llamar al método updateEstado y no hacer esta guarrería

			ps = connection
					.prepareStatement("UPDATE T_MENSAJE SET MEN_ESTADO = ? WHERE MEN_ID >= ? AND MEN_ID <= ? AND MEN_ESTADO = ?");

			i = 1;

			ps.setString(i, ESTADO_PROCESADO);
			i++;
			ps.setInt(i, ((MensajeDao) msgList.get(0)).getMenID());
			i++;
			ps.setInt(i, ((MensajeDao) msgList.get(auxCuentaNumeroMensajes - 1)).getMenID());
			i++;
			ps.setString(i, ESTADO_PENDIENTE);

			auxCuentaNumeroMensajes = ps.executeUpdate();

			logger.info("Se ha actualizado el estado de " + auxCuentaNumeroMensajes + " a " + ESTADO_PROCESADO);

			// } else {
			// logger.warn("No se ha encontrado ningúna  convesarción");
			// return null;
			// }
		} catch (SQLException e) {
			logger.error("Ha ocurrido un error" + e.getMessage(), e);
		} finally {
			FuenteDatos.closeBDConnection(connection, ps, null);
		}
		return msgList;

	}

	public void updateEstado(String estadoNuevo) {

		logger.debug("Actualizando el estado del mensaje id=" + getMenID() + " a estado=" + estadoNuevo);

		setMenEstado(estadoNuevo);

		Connection connection = null;
		PreparedStatement ps = null;

		try {
			connection = FuenteDatos.openBDConnection();

			ps = connection.prepareStatement("UPDATE T_MENSAJE SET MEN_ESTADO = ? WHERE MEN_ID = ?");

			int i = 1;

			ps.setString(i, getMenEstado());
			i++;
			ps.setInt(i, getMenID());

			ps.execute();

		} catch (SQLException e) {

			logger.error("Ha ocurrido un error" + e.getMessage(), e);
		} finally {
			FuenteDatos.closeBDConnection(connection, ps, null);

		}

	}

	private void asignarAtributos(ResultSet rs) throws SQLException {

		logger.debug("Asignando atributos para el mensaje");

		if (!rs.wasNull()) {
			this.setMenID(new Integer(rs.getInt("MEN_ID")));
		} else {
			this.setMenID(-1);
		}

		if (!rs.wasNull()) {
			this.setMenFecha((rs.getTimestamp("MEN_FECHA")).getTime());
		} else {
			this.setMenFecha(-1);
		}

		if (!rs.wasNull()) {
			this.setMenUsuEnvia(new String(rs.getString("MEN_USU_ENVIA")));
		} else {
			this.setMenUsuEnvia(null);
		}
		if (!rs.wasNull()) {
			this.setMenUsuRecibe(new String(rs.getString("MEN_USU_RECIBE")));
		} else {
			this.setMenUsuRecibe(null);
		}
		if (!rs.wasNull()) {
			this.setMenTexto(new String(rs.getString("MEN_TEXTO")));
		} else {
			this.setMenTexto(null);
		}
		if (!rs.wasNull()) {
			this.setMenEstado(new String(rs.getString("MEN_ESTADO")));
		} else {
			this.setMenEstado(null);
		}

	}
}