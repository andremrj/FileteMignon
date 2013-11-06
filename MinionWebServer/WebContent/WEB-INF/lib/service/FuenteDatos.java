package org.minion.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class FuenteDatos {

	public FuenteDatos() {

	}

	public static Connection openBDConnection() throws SQLException {

		DataSource ds = null;
		Connection connection = null;

		try {
			Context initContext = new InitialContext();
			Context envContext = (Context) initContext.lookup("java:/comp/env");
			ds = (DataSource) envContext.lookup("jdbc/MINION");

			// get database connection
			connection = ds.getConnection();

			// test(connection);

		} catch (NamingException ne) {

			throw new RuntimeException("Unable to aquire data source", ne);

		}
		return connection;
	}

	public static void closeBDConnection(Connection connection, PreparedStatement ps, ResultSet rs) {

		try {
			if (rs != null)
				rs.close();
		} catch (Exception e) {
		}
		try {
			if (ps != null)
				ps.close();
		} catch (Exception e) {
		}
		try {
			if (connection != null)
				connection.close();
		} catch (Exception e) {
		}

	}

	@SuppressWarnings("unused")
	private static void test(Connection connection) throws SQLException {

		PreparedStatement ps = connection.prepareStatement("SELECT * FROM T_USUARIO");

		ResultSet rs = ps.executeQuery();

		if (rs.next()) {
			System.out.println("HA CONECTADO!!!!!!!!!!!!!!!!!!");
		} else {
			System.out.println("HA PALMADOOOO!!!!!!!!!!!!!!!!!!");
		}

		closeBDConnection(connection, ps, rs);

	}

}
