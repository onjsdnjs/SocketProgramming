package kr.co.daou.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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
				ret = ret + temp + Const.END_LINE;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {

			try {
				br.close();
				br = null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				bw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			bw = null;
		}
	}

	public static byte[] makeMessageStringToByte(String msg) {
		return mergeBytearrays(intTobyte(msg.getBytes().length), msg.getBytes());
	}

	// public static String parseMessageByteToString(byte[] bytes){
	// return null
	// ;
	// }

	public static String makeJSONMessageForAuth(String name, String passwd) {
		JSONObject jsonObject = new JSONObject();
		// getLong
		jsonObject.put(Const.JSON_KEY_SEND_TIME, System.currentTimeMillis());
		// auth, pp, push getString
		jsonObject.put(Const.JSON_KEY_DATA_CATEGORY, Const.JSON_VALUE_AUTH);
		JSONObject object = new JSONObject();
		// id, getString
		object.put(Const.JSON_KEY_AUTH_ID, name);
		// passwd, getString
		object.put(Const.JSON_KEY_AUTH_PASSWD, passwd);
		// data (JSONObject)getObjcet
		jsonObject.put(Const.JSON_KEY_DATA, object);

		return jsonObject.toString() + Const.END_LINE;
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
		return jsonObject.toString() + Const.END_LINE;
	}

	public static String parseJSONMessage(String msg) {
		JSONParser jsonParser = new JSONParser();
		String category = null;
		String result = null;
		try {
			JSONObject jsonObject = (JSONObject) jsonParser.parse(msg);
			category = (String) jsonObject.get(Const.JSON_KEY_DATA_CATEGORY);
			if (category.equals(Const.JSON_VALUE_PING)) {
				result = Const.JSON_VALUE_PING;
				result = "server/" + result;
			} else if (category.equals(Const.JSON_VALUE_PONG)) {
				result = Const.JSON_VALUE_PONG;
				result = "server/" + result;
			} else if (category.equals(Const.JSON_VALUE_AUTH)) {
				JSONObject object = (JSONObject) jsonObject.get(Const.JSON_KEY_DATA);
				String id = (String) object.get(Const.JSON_KEY_AUTH_ID);
				String passwd = (String) object.get(Const.JSON_KEY_AUTH_PASSWD);
				result = id + "/login";
				// result = id;
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result = "JSON 파싱 에러";
		}
		return result;
	}

	/**
	 * int형을 byte배열로 바꿈
	 * 
	 * @param integer
	 * 
	 *            order : ByteOrder.LITTLE_ENDIAN ByteOrder.BIG_ENDIAN
	 * @return
	 */
	public static byte[] intTobyte(int integer) {
		ByteBuffer buff = ByteBuffer.allocate(Integer.SIZE / 8);
		buff.order(ByteOrder.LITTLE_ENDIAN);
		// 인수로 넘어온 integer을 putInt로설정
		buff.putInt(integer);
		return buff.array();
	}

	/**
	 * byte배열을 int형로 바꿈
	 * 
	 * @param bytes
	 * @param order
	 *            : ByteOrder.LITTLE_ENDIAN ByteOrder.BIG_ENDIAN
	 * @return
	 */
	public static int byteToInt(byte[] bytes) {
		ByteBuffer buff = ByteBuffer.allocate(Integer.SIZE / 8);
		buff.order(ByteOrder.LITTLE_ENDIAN);
		// buff사이즈는 4인 상태임
		// bytes를 put하면 position과 limit는 같은 위치가 됨.
		buff.put(bytes);
		// flip()가 실행 되면 position은 0에 위치 하게 됨.
		buff.flip();
		return buff.getInt(); // position위치(0)에서 부터 4바이트를 int로 변경하여 반환
	}

	public static byte[] mergeBytearrays(byte[] header, byte[] body) {
		byte[] retMerge = new byte[header.length + body.length];
		System.arraycopy(header, 0, retMerge, 0, header.length);
		System.arraycopy(body, 0, retMerge, header.length, body.length);
		return retMerge;
	}

	public static byte[] divideBytearrays(int headerLength, byte[] bArr) {
		byte[] retDiv = new byte[bArr.length - headerLength];
		System.arraycopy(bArr, headerLength, retDiv, 0, bArr.length - headerLength);
		return retDiv;
	}

}
