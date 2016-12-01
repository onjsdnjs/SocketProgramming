package kr.co.daou;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class MultiThreadedChatServer {

	// 서버 소켓과 클라이언트 연결 소켓
	private ServerSocket serverSocket = null;
	private Socket clientSocket = null;
	private Connection con = null;
	private Statement stmt = null;
	private ResultSet rs = null;
	private int timeout = 12000;
	// 연결된 클라이언트 스레드를 관리하는 ArrayList
	ArrayList<ChatThread> chatlist = new ArrayList<ChatThread>();

	public MultiThreadedChatServer() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			// 소켓을 통해 연결시켜줌
			con = (Connection) DriverManager.getConnection("jdbc:mysql://localhost/daoudb", "daou", "daou");
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	} // end of constructor

	public void start() {
		try {
			// 서버 소켓 생성
			serverSocket = new ServerSocket(9999);
			System.out.println("Server START...");

			// 무한루프를 돌면서 클라이언트 연결을 기다림
			while (true) {
				clientSocket = serverSocket.accept();
				// 연결된 클라이언트에서 스레드 클래스 생성
				ChatThread chat = new ChatThread();
				// 클라이언트 리스트 추가
				chatlist.add(chat);
				chat.start();
			}
		} catch (IOException e) {
			System.out.println(e);
			System.out.println("[MultiChatServer]start() Exception 발생!!");
		}
	} // end of start

	// 연결된 모든 클라이언트에 메시지 전송
	void msgSendAll(String msg) {
		for (ChatThread ct : chatlist)
			ct.outMsg.println(msg);
	}

	// 특정 클라이언트에 메시지 전송
	void whisperSend(String msg) {
		String[] temp = msg.split("/");
		for (ChatThread ct : chatlist) {
			// 해당 클라이언트 socket 찾는 과정
			if (ct.rmsg[0].equals(temp[2]))
				ct.outMsg.println(temp[0] + "/" + temp[3]);
		}
	}

	// 특정 클라이언트 제외하고 메시지 전송
	void blockSend(String msg) {
		String[] temp = msg.split("/");
		for (ChatThread ct : chatlist) {
			// 해당 클라이언트 socket을 제외한 클라이언트 찾는 과정
			if (!ct.rmsg[0].equals(temp[2]))
				ct.outMsg.println(temp[0] + "/" + temp[3]);
		}
	}

	// 허가되지 않은 클라이언트에게 메시지 전송
	void alert(String msg) {
		for (ChatThread ct : chatlist) {
			// 해당 클라이언트 socket 찾는 과정
			if (ct.rmsg[0].equals(msg)) {
				chatlist.remove(this);
				ct.outMsg.println("Server/ 허가되지 않은 사용자입니다.");
			}
		}
	} // end of alert

	// 허가된 사용자인지 판단
	boolean checkUser(String user) {

		try {
			String query = "select ID from idtable where ID='" + user + "'";
			stmt = con.createStatement();
			rs = stmt.executeQuery(query);
			// null이 아닌 경우
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

	// 클라이언트 스레드 클래스
	class ChatThread extends Thread {

		// 수신 메시지와 파싱된 메시지 담아두는 변수 선언
		String msg;
		String[] rmsg;
		int dataSize;

		// 입출력 스트림 생성
		private BufferedReader inMsg = null;
		private PrintWriter outMsg = null;

		public void run() {
			boolean status = true;
			System.out.println("##ClientThread START...");
			try {
				// 입출력 스트림 생성
				inMsg = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
				outMsg = new PrintWriter(clientSocket.getOutputStream(), true);

				clientSocket.setSoTimeout(timeout);

				// 클라이언트에게 timeout 설정
				new PingPong(10000, outMsg).start();

				while (status) {

					// 수신된 메시지를 msg 변수에 저장
					msg = inMsg.readLine();
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
						if (checkUser(rmsg[0])) {
							msgSendAll("server/" + rmsg[0] + "님이 로그인했습니다.");
							for (ChatThread ct : chatlist) {
								// 해당 클라이언트 socket 찾는 과정
								if (ct.rmsg[0].equals(rmsg[0])) {
									ct.outMsg.println("server/dataSize");
									dataSize = Integer.parseInt(ct.inMsg.readLine());
									System.out.println(dataSize + "Byte");
								}
							}
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
						outMsg.println("server/pong");
					}

					// From클라이언트, pong 받음
					else if (rmsg[1].equals("pong")) {
						System.out.println(rmsg[0] + "/pong");
					}
					// 그밖의 일반 메시지일 때
					else {
						msgSendAll(msg);
					}
				} // end of while
				System.out.println("##" + this.getName() + "stop!!");
			} catch (IOException e) {
				chatlist.remove(this);
				System.out.println("[ChatThread]run() IOException 발생!!");
			}
		}
	}

	public static void main(String[] args) {
		MultiThreadedChatServer server = new MultiThreadedChatServer();
		server.start();
	}
}
