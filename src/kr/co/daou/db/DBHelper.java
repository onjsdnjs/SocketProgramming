package kr.co.daou.db;

import kr.co.daou.format.Const;
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
		connectDB();
	}

	public void connectDB() {
		try {
			Class.forName(Const.CLASS_FOR_NAME);
			// 소켓을 통해 연결시켜줌
			con = (Connection) DriverManager.getConnection(Const.JDBC_URL + Const.DB_NAME, Const.DB_USER_ID,
					Const.DB_USER_PASSWORD);
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

	public void closeDBSet() {
		try {
			rs.close();
			stmt.close();
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
