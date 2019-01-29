package fr.epsi.book.dal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class PersistenceManager {
	
	private static final String DB_URL = "jdbc:mysql://localhost:3306/a_definir?useSSL=false&useLegacyDatetimeCode=false&serverTimezone=UTC";
	private static final String DB_LOGIN = "a_definir";
	private static final String DB_PWD = "a_definir";
	
	private static Connection connection;
	
	private PersistenceManager() {}
	
	public static Connection getConnection() throws SQLException {
		if ( null == connection || connection.isClosed() ) {
			connection = DriverManager.getConnection( DB_URL, DB_LOGIN, DB_PWD );
		}
		
		return connection;
	}
	
	public static void closeConnection() throws SQLException {
		if ( null != connection && !connection.isClosed() ) {
			connection.close();
		}
	}
}
