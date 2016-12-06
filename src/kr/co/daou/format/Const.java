package kr.co.daou.format;

import java.io.File;

public interface Const {
	/**
	 * Socket Connection
	 */
	String SERVER_IP = "localhost";
	int PORT_NUM = 9999;
	int STREAM_TIME_OUT = 3000;
	int SEND_WATING_TIME = 1000;

	/**
	 * File Read & Write
	 */
	String SRC_FILE_PATH = "D:" + File.separator + "test_text" + File.separator + "client" + File.separator;
	String SRC_FILE_NAME = "src_test.txt";
	String DEST_FILE_PATH = "D:" + File.separator + "test_text" + File.separator + "server" + File.separator;
	String DEST_FILE_NAME = "dest_test.txt";

	/**
	 * DB Connection
	 */
	String CLASS_FOR_NAME = "com.mysql.jdbc.Driver";
	String JDBC_URL = "jdbc:mysql://localhost/";
	String DB_NAME = "daoudb";
	String DB_USER_ID = "daou";
	String DB_USER_PASSWORD = "daou";

	/**
	 * JSON
	 */
	String JSON_KEY_SEND_TIME = "send_time";
	String JSON_KEY_DATA_CATEGORY = "data_category";
	String JSON_KEY_DATA = "data";
	String JSON_KEY_DATA_SIZE = "data_size";
	String JSON_KEY_AUTH_ID = "id";
	String JSON_KEY_AUTH_PASSWD = "passwd";

	String JSON_VALUE_AUTH = "auth";
	String JSON_VALUE_PING = "ping";
	String JSON_VALUE_PONG = "pong";
	String JSON_VALUE_PUSH = "push";

	String END_LINE = "\n";

}
