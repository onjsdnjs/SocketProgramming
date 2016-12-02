package kr.co.daou.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBHelper {
	private static Connection con = null;
	private static Statement stmt = null;
	private static ResultSet rs = null;

	public DBHelper() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			// 소켓을 통해 연결시켜줌
			con = (Connection) DriverManager.getConnection("jdbc:mysql://localhost/daoudb", "daou", "daou");
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static ResultSet selectQuery(String query) {
		try {
			stmt = con.createStatement();
			rs = stmt.executeQuery(query);
			return rs;
		} catch (SQLException e) {
			e.printStackTrace();
			return rs;
		}

	} // end of selectQuery
}
