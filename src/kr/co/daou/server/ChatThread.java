package kr.co.daou.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import kr.co.daou.PingPong;
import kr.co.daou.db.DBHelper;
import kr.co.daou.utils.Utils;

class ChatThread extends Thread {

	private Socket clientSocket = null;
	// ��ü Ŭ���̾�Ʈ ������
	private ArrayList<ChatThread> chatlist = null;

	// ���� �޽��� ��Ƶδ� 20byte ���� ����
	private byte[] messageBuffer = new byte[20480];
	private int receiveDataSize;
	private byte[] receiveData;

	// ���� �޽����� �Ľ̵� �޽��� ��Ƶδ� ���� ����
	private String msg;
	private String[] rmsg;

	private int timeout = 12000;

	private InputStream inMsg = null;
	private OutputStream outMsg = null;

	public ChatThread(Socket clientSocket, ArrayList<ChatThread> chatlist) {
		this.clientSocket = clientSocket;
		this.chatlist = chatlist;
	}

	// ����� ��� Ŭ���̾�Ʈ�� �޽��� ����
	void msgSendAll(String msg) {
		for (ChatThread ct : chatlist) {
			byte[] b = new byte[20480];
			b = msg.getBytes();
			try {
				ct.outMsg.write(b, 0, b.length);
				ct.outMsg.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// Ư�� Ŭ���̾�Ʈ�� �޽��� ����
	void whisperSend(String msg) {
		String[] temp = msg.split("/");
		for (ChatThread ct : chatlist) {
			// �ش� Ŭ���̾�Ʈ socket ã�� ����
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

	// Ư�� Ŭ���̾�Ʈ �����ϰ� �޽��� ����
	void blockSend(String msg) {
		String[] temp = msg.split("/");
		for (ChatThread ct : chatlist) {
			// �ش� Ŭ���̾�Ʈ socket�� ������ Ŭ���̾�Ʈ ã�� ����
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

	// �㰡���� ���� Ŭ���̾�Ʈ���� �޽��� ����
	void alert(String msg) {
		for (ChatThread ct : chatlist) {
			// �ش� Ŭ���̾�Ʈ socket ã�� ����
			if (ct.rmsg[0].equals(msg)) {
				chatlist.remove(this);
				byte[] b = new byte[20480];
				String str = "Server/ �㰡���� ���� ������Դϴ�.";
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

	// �㰡�� ��������� �Ǵ�
	public boolean checkUser(String user, String pw) {
		try {
			String query = "select * from login where ID='" + user + "' AND PW='" + pw + "'";
			ResultSet rs = DBHelper.selectQuery(query);

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

	// ����Ǵ� �κ�
	public void run() {
		boolean status = true;
		System.out.println("##ClientThread START...");
		try {
			// ����� ��Ʈ�� ����
			inMsg = clientSocket.getInputStream();
			outMsg = clientSocket.getOutputStream();

			boolean authentication = true;
			clientSocket.setSoTimeout(timeout);
			new PingPong(10000, outMsg).start();
			while (status) {

				// ���ŵ� �޽����� DATASIZE ����
				receiveDataSize = inMsg.read(messageBuffer);

				if (receiveDataSize != 0) {
					receiveData = new byte[receiveDataSize];
					// src, srcPos, dest, destPos, length
					System.arraycopy(messageBuffer, 0, receiveData, 0, receiveDataSize);
				}

				// byte[], offset, length
				msg = new String(receiveData, 0, receiveData.length);

				// ó�� ���� ����
				if (authentication) {
					msg = Utils.parseJSONMessage(msg);
					authentication = false;
				}

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
					if (checkUser(rmsg[0], rmsg[0])) {
						msgSendAll("server/" + rmsg[0] + "���� �α����߽��ϴ�.");

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
					String message = "server/pong";
					byte[] bytes = message.getBytes();
					// ������ �α��� �޽��� ����
					this.outMsg.write(bytes);
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

	public static void main(String[] args) {
		MultiThreadedChatServer server = new MultiThreadedChatServer();
		server.start();
	}
}