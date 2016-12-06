package kr.co.daou.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import kr.co.daou.db.DBHelper;
import kr.co.daou.utils.Utils;

class ChatThread extends Thread {

	private Socket clientSocket = null;
	// 전체 클라이언트 스레드
	private ArrayList<ChatThread> chatlist = null;

	// 수신 메시지와 파싱된 메시지 담아두는 변수 선언
	private String msg;
	private String[] rmsg;

	private int timeout = 12000;

	private InputStream inMsg = null;
	private OutputStream outMsg = null;

	public ChatThread(Socket clientSocket, ArrayList<ChatThread> chatlist) {
		this.clientSocket = clientSocket;
		this.chatlist = chatlist;
	}

	// 연결된 모든 클라이언트에 메시지 전송
	public void msgSendAll(String msg) {
		for (ChatThread ct : chatlist) {
			byte[] b = new byte[msg.length()];
			b = msg.getBytes();
			try {
				ct.outMsg.write(b, 0, b.length);
				ct.outMsg.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// 특정 클라이언트에 메시지 전송
	public void whisperSend(String msg) {
		String[] temp = msg.split("/");
		for (ChatThread ct : chatlist) {
			// 해당 클라이언트 socket 찾는 과정
			if (ct.rmsg[0].equals(temp[2])) {
				byte[] b = new byte[20480];
				String str = temp[0] + " / " + temp[3];
				b = str.getBytes();
				try {
					ct.outMsg.write(b, 0, b.length);
					ct.outMsg.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
	}

	// 특정 클라이언트 제외하고 메시지 전송
	public void blockSend(String msg) {
		String[] temp = msg.split("/");
		for (ChatThread ct : chatlist) {
			// 해당 클라이언트 socket을 제외한 클라이언트 찾는 과정
			if (!ct.rmsg[0].equals(temp[2])) {
				byte[] b = new byte[20480];
				String str = temp[0] + " / " + temp[3];
				b = str.getBytes();
				try {
					ct.outMsg.write(b, 0, b.length);
					ct.outMsg.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
	}

	// 허가되지 않은 클라이언트에게 메시지 전송
	public void alert(String msg) {
		for (ChatThread ct : chatlist) {
			// 해당 클라이언트 socket 찾는 과정
			if (ct.rmsg[0].equals(msg)) {
				chatlist.remove(this);
				byte[] b = new byte[20480];
				String str = "Server/ 허가되지 않은 사용자입니다.";
				b = str.getBytes();
				try {
					ct.outMsg.write(b, 0, b.length);
					ct.outMsg.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	// 허가된 사용자인지 판단
	public boolean checkUser(String user, String pw) {
		try {
			String query = "select * from login where ID='" + user + "' AND PW='" + pw + "'";
			ResultSet rs = DBHelper.selectQuery(query);

			if (rs.next() == true) {
				System.out.println(user + "사용자님이 접속하였습니다.");
				return true;
			} else {
				System.out.println(user + "사용자님은 허가되지 않은 사용자입니다.");
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	// 실행되는 부분
	public void run() {
		boolean status = true;
		System.out.println("##ClientThread START...");
		// 입출력 스트림 생성
		try {
			inMsg = clientSocket.getInputStream();
			outMsg = clientSocket.getOutputStream();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		while (status) {
			try {
				// timeout 설정
				clientSocket.setSoTimeout(timeout);

				// 수신된 메시지 DATASIZE
				byte[] header = new byte[4];
				int headlength = inMsg.read(header);
				int length = Utils.byteToInt(header);

				// DATA 길이만큼 byte배열 선언
				byte[] body = new byte[length];
				int bodylength = inMsg.read(body);
				String json = new String(body, 4, bodylength - 4);
				msg = Utils.parseJSONMessage(json);

				// '/' 구분자를 기준으로 메시지를 문자열 배열로 파싱
				rmsg = msg.split("/");

				// 파싱된 문자열 배열의 두번째 요소값에 따라 처리
				// 로그아웃 메시지일때
				if (rmsg[1].equals("logout")) {
					chatlist.remove(this);
					msgSendAll("server/" + rmsg[0] + "님이 종료했습니다.");
					// 해당 클라이언트 스레드 종료로 인해 status를 false로 설정
					status = false;
				}
				// 로그인 메시지 일때
				else if (rmsg[1].equals("login")) {

					// 허가된 사용자인지 check
					if (checkUser(rmsg[0], rmsg[0])) {
						msgSendAll("server/" + rmsg[0] + "님이 로그인했습니다.");

					} else {
						// 허가되지 않은 사용자
						alert(rmsg[0]);
						clientSocket.close();
					}

				}
				// 귓속말 메시지일 때
				else if (rmsg[1].equals("whisper")) {
					whisperSend(msg);
				}

				// 블락 메시지일 때
				else if (rmsg[1].equals("block")) {
					blockSend(msg);
				}

				// From클라이언트, ping 받음
				else if (rmsg[1].equals("ping")) {
					String message = "server/pong";
					byte[] bytes = message.getBytes();
					// 서버에 로그인 메시지 전달
					this.outMsg.write(bytes);
				}

				// 그밖의 일반 메시지일 때
				else {
					msgSendAll(msg);
				}
			} catch (IOException e) {
				try {
					System.out.println("Time out 발생...");
					String msg = "server/ping";
					byte[] bytes = msg.getBytes();
					outMsg.write(bytes);
					System.out.println("ping 전송");
				} catch (IOException e3) {
					e3.printStackTrace();
				}
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}

				// 수신된 메시지를 DATASIZE 길이
				byte[] header = new byte[4];
				try {
					int headlength = inMsg.read(header);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				int length = Utils.byteToInt(header);

				// DATA 길이만큼 byte배열 선언
				byte[] body = new byte[length];

				try {
					int bodylength;
					bodylength = inMsg.read(body);
					String json = new String(body, 4, bodylength - 4);
					msg = Utils.parseJSONMessage(json);
				} catch (IOException e1) {
					e1.printStackTrace();
				}

				// '/' 구분자를 기준으로 메시지를 문자열 배열로 파싱
				rmsg = msg.split("/");

				// From클라이언트, pong 받음
				if (rmsg[1].equals("pong")) {
					System.out.println(Thread.currentThread().getName() + "/pong");
				} else {
					status = false;
					try {
						clientSocket.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		} // end of while
		System.out.println("##" + this.getName() + "stop!!");
	} // catch (IOException e) {
		// chatlist.remove(this);
		// System.out.println("[ChatThread]run() IOException 발생!!");
		// }
		// }

	public static void main(String[] args) {
		MultiThreadedChatServer server = new MultiThreadedChatServer();
		server.start();
	}
}