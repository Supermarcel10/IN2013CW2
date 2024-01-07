package com.newideas.balancebit.app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class LocalDBService {
	private static Connection connection = null;

	static boolean connect(String dbPath) {
		try {
			String url = "jdbc:sqlite:" + dbPath;
			connection = DriverManager.getConnection(url);
			return true;
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			return false;
		}
	}

	static boolean endConnection() {
		try {
			if (connection != null) {
				connection.close();
			}
			return true;
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			return false;
		}
	}

	static boolean executeSQL(String sql) {
		try (Statement stmt = connection.createStatement()) {
			stmt.execute(sql);
			return true;
		} catch (SQLException e) {
			return false;
		}
	}

	static ResultSet selectSQL(String sql) {
		try (Statement stmt = connection.createStatement()) {
			return stmt.executeQuery(sql);
		} catch (SQLException e) {
			return null;
		}
	}
}
