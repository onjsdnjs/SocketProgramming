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

	// ���� ���ϰ� Ŭ���̾�Ʈ ���� ����
	private ServerSocket serverSocket = null;
	private Socket clientSocket = null;
	private Connection con = null;
	private Statement stmt = null;
	private ResultSet rs = null;
	private int timeout = 12000;
	// ����� Ŭ���̾�Ʈ �����带 �����ϴ� ArrayList
	ArrayList<ChatThread> chatlist = new ArrayList<ChatThread>();

	public MultiThreadedChatServer() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			// ������ ���� ���������
			con = (Connection) DriverManager.getConnection("jdbc:mysql://localhost/daoudb", "daou", "daou");
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	} // end of constructor

	public void start() {
		try {
			// ���� ���� ����
			serverSocket = new ServerSocket(9999);
			System.out.println("Server START...");

			// ���ѷ����� ���鼭 Ŭ���̾�Ʈ ������ ��ٸ�
			while (true) {
				clientSocket = serverSocket.accept();
				// ����� Ŭ���̾�Ʈ���� ������ Ŭ���� ����
				ChatThread chat = new ChatThread();
				// Ŭ���̾�Ʈ ����Ʈ �߰�
				chatlist.add(chat);
				chat.start();
			}
		} catch (IOException e) {
			System.out.println(e);
			System.out.println("[MultiChatServer]start() Exception �߻�!!");
		}
	} // end of start

	// ����� ��� Ŭ���̾�Ʈ�� �޽��� ����
	void msgSendAll(String msg) {
		for (ChatThread ct : chatlist)
			ct.outMsg.println(msg);
	}

	// Ư�� Ŭ���̾�Ʈ�� �޽��� ����
	void whisperSend(String msg) {
		String[] temp = msg.split("/");
		for (ChatThread ct : chatlist) {
			// �ش� Ŭ���̾�Ʈ socket ã�� ����
			if (ct.rmsg[0].equals(temp[2]))
				ct.outMsg.println(temp[0] + "/" + temp[3]);
		}
	}

	// Ư�� Ŭ���̾�Ʈ �����ϰ� �޽��� ����
	void blockSend(String msg) {
		String[] temp = msg.split("/");
		for (ChatThread ct : chatlist) {
			// �ش� Ŭ���̾�Ʈ socket�� ������ Ŭ���̾�Ʈ ã�� ����
			if (!ct.rmsg[0].equals(temp[2]))
				ct.outMsg.println(temp[0] + "/" + temp[3]);
		}
	}

	// �㰡���� ���� Ŭ���̾�Ʈ���� �޽��� ����
	void alert(String msg) {
		for (ChatThread ct : chatlist) {
			// �ش� Ŭ���̾�Ʈ socket ã�� ����
			if (ct.rmsg[0].equals(msg)) {
				chatlist.remove(this);
				ct.outMsg.println("Server/ �㰡���� ���� ������Դϴ�.");
			}
		}
	} // end of alert

	// �㰡�� ��������� �Ǵ�
	boolean checkUser(String user) {

		try {
			String query = "select ID from idtable where ID='" + user + "'";
			stmt = con.createStatement();
			rs = stmt.executeQuery(query);
			// null�� �ƴ� ���
			if (rs.next() == true) {
				System.out.println(user + "����ڴ��� �����Ͽ����ϴ�.");
				return true;
			} else {
				System.out.println(user + "����ڴ��� �㰡���� ���� ������Դϴ�.");
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	// Ŭ���̾�Ʈ ������ Ŭ����
	class ChatThread extends Thread {

		// ���� �޽����� �Ľ̵� �޽��� ��Ƶδ� ���� ����
		String msg;
		String[] rmsg;
		int dataSize;

		// ����� ��Ʈ�� ����
		private BufferedReader inMsg = null;
		private PrintWriter outMsg = null;

		public void run() {
			boolean status = true;
			System.out.println("##ClientThread START...");
			try {
				// ����� ��Ʈ�� ����
				inMsg = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
				outMsg = new PrintWriter(clientSocket.getOutputStream(), true);

				clientSocket.setSoTimeout(timeout);

				// Ŭ���̾�Ʈ���� timeout ����
				new PingPong(10000, outMsg).start();

				while (status) {

					// ���ŵ� �޽����� msg ������ ����
					msg = inMsg.readLine();
					// '/' �����ڸ� �������� �޽����� ���ڿ� �迭�� �Ľ�
					rmsg = msg.split("/");

					// �Ľ̵� ���ڿ� �迭�� �ι�° ��Ұ��� ���� ó��
					// �α׾ƿ� �޽����϶�
					if (rmsg[1].equals("logout")) {
						chatlist.remove(this);
						msgSendAll("server/" + rmsg[0] + "���� �����߽��ϴ�.");
						// �ش� Ŭ���̾�Ʈ ������ ����� ���� status�� false�� ����
						status = false;
					}
					// �α��� �޽��� �϶�
					else if (rmsg[1].equals("login")) {

						// �㰡�� ��������� check
						if (checkUser(rmsg[0])) {
							msgSendAll("server/" + rmsg[0] + "���� �α����߽��ϴ�.");
							for (ChatThread ct : chatlist) {
								// �ش� Ŭ���̾�Ʈ socket ã�� ����
								if (ct.rmsg[0].equals(rmsg[0])) {
									ct.outMsg.println("server/dataSize");
									dataSize = Integer.parseInt(ct.inMsg.readLine());
									System.out.println(dataSize + "Byte");
								}
							}
						} else {
							// �㰡���� ���� �����
							alert(rmsg[0]);
							clientSocket.close();
						}

					}
					// �ӼӸ� �޽����� ��
					else if (rmsg[1].equals("whisper")) {
						whisperSend(msg);
					}

					// ��� �޽����� ��
					else if (rmsg[1].equals("block")) {
						blockSend(msg);
					}

					// FromŬ���̾�Ʈ, ping ����
					else if (rmsg[1].equals("ping")) {
						outMsg.println("server/pong");
					}

					// FromŬ���̾�Ʈ, pong ����
					else if (rmsg[1].equals("pong")) {
						System.out.println(rmsg[0] + "/pong");
					}
					// �׹��� �Ϲ� �޽����� ��
					else {
						msgSendAll(msg);
					}
				} // end of while
				System.out.println("##" + this.getName() + "stop!!");
			} catch (IOException e) {
				chatlist.remove(this);
				System.out.println("[ChatThread]run() IOException �߻�!!");
			}
		}
	}

	public static void main(String[] args) {
		MultiThreadedChatServer server = new MultiThreadedChatServer();
		server.start();
	}
}
