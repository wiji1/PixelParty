package dev.wiji.pixelparty.sql;

import java.sql.Connection;
import java.sql.SQLException;

public enum ConnectionInfo {

	PIXEL_PARTY(TableManager.plugin.getConfigOption("sql-url"),
			TableManager.plugin.getConfigOption("sql-user"),
			TableManager.plugin.getConfigOption("sql-pass"),
			1000L * 60 * 60 * 24 * 30)
	;

	 public final String URL;
	 public final String USERNAME;
	 public final String PASSWORD;
	 public final long MAX_TIME;


	 ConnectionInfo(String URL, String USERNAME, String PASSWORD, long MAX_TIME) {
		 this.URL = URL;
		 this.USERNAME = USERNAME;
		 this.PASSWORD = PASSWORD;
		 this.MAX_TIME = MAX_TIME;
	 }

	 public Connection getConnection() {
		 String URL = this.URL;
		 try { Class.forName("com.mysql.jdbc.Driver"); } catch(ClassNotFoundException e) { throw new RuntimeException(e); }
		 try { return java.sql.DriverManager.getConnection(URL, USERNAME, PASSWORD); } catch(SQLException e) { throw new RuntimeException(e); }
	 }
}
