package kr.co.daou.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import kr.co.daou.format.Const;

public class Utils {
	public static String readFile(String filePath) {
		String ret = "";
		String temp = "";
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(filePath));
			while ((temp = br.readLine()) != null) {
				ret = ret + temp + "\n";
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {

			try {
				br.close();
				br = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return ret;
	}

	public static void writeFile(String filePath, String text) {

		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(filePath, true));
			bw.write(text);
			bw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			bw = null;
		}
	}

	public static String makeJSONMessageForAuth(String id, String passwd) {
		JSONObject jsonObject = new JSONObject();
		// getLong
		jsonObject.put(Const.JSON_KEY_SEND_TIME, System.currentTimeMillis());
		// auth, pp, push getString
		jsonObject.put(Const.JSON_KEY_DATA_CATEGORY, Const.JSON_VALUE_AUTH);
		JSONObject object = new JSONObject();
		// id, getString
		object.put(Const.JSON_KEY_AUTH_ID, id);
		// passwd, getString
		object.put(Const.JSON_KEY_AUTH_PASSWD, passwd);
		// data (JSONObject)getObjcet
		jsonObject.put(Const.JSON_KEY_DATA, object);

		// data_size getInt
		jsonObject.put(Const.JSON_KEY_DATA_SIZE, jsonObject.toString().getBytes().length);
		System.out.println(jsonObject.toString());
		return jsonObject.toString();
	}

	public static String makeJSONMessageForPingPong(boolean isPing) {
		JSONObject jsonObject = new JSONObject();
		// getLong
		jsonObject.put(Const.JSON_KEY_SEND_TIME, System.currentTimeMillis());
		// auth, pp, push getString
		if (isPing) {
			jsonObject.put(Const.JSON_KEY_DATA_CATEGORY, Const.JSON_VALUE_PING);
		} else {
			jsonObject.put(Const.JSON_KEY_DATA_CATEGORY, Const.JSON_VALUE_PONG);
		}
		// data_size getInt
		jsonObject.put(Const.JSON_KEY_DATA_SIZE, jsonObject.toString().getBytes().length);
		System.out.println(jsonObject.toString());
		return jsonObject.toString();
	}

	public static String parseJSONMessage(String msg) {
		JSONParser jsonParser = new JSONParser();
		String category = null;
		String result = null;
		try {
			JSONObject jsonObject = (JSONObject) jsonParser.parse(msg);
			category = (String) jsonObject.get(Const.JSON_KEY_DATA_CATEGORY);
			if (category.equals(Const.JSON_VALUE_PING)) {
				result = Const.JSON_VALUE_PONG;
			} else if (category.equals(Const.JSON_VALUE_PONG)) {
				result = Const.JSON_VALUE_PING;
			} else if (category.equals(Const.JSON_VALUE_AUTH)) {
				JSONObject object = (JSONObject) jsonObject.get(Const.JSON_KEY_DATA);
				String id = (String) object.get(Const.JSON_KEY_AUTH_ID);
				String passwd = (String) object.get(Const.JSON_KEY_AUTH_PASSWD);
				result = id + "/login";
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result = "JSON ÆÄ½Ì ¿¡·¯";
		}
		return result;
	}
}
