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
	// ��ü Ŭ���̾�Ʈ ������
	private ArrayList<ChatThread> chatlist = null;

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

	// Ư�� Ŭ���̾�Ʈ�� �޽��� ����
	public void whisperSend(String msg) {
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
	public void blockSend(String msg) {
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
	public void alert(String msg) {
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
		// ����� ��Ʈ�� ����
		try {
			inMsg = clientSocket.getInputStream();
			outMsg = clientSocket.getOutputStream();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		while (status) {
			try {
				// timeout ����
				clientSocket.setSoTimeout(timeout);

				// ���ŵ� �޽��� DATASIZE
				byte[] header = new byte[4];
				int headlength = inMsg.read(header);
				int length = Utils.byteToInt(header);

				// DATA ���̸�ŭ byte�迭 ����
				byte[] body = new byte[length];
				int bodylength = inMsg.read(body);
				String json = new String(body, 4, bodylength - 4);
				msg = Utils.parseJSONMessage(json);

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

				// �׹��� �Ϲ� �޽����� ��
				else {
					msgSendAll(msg);
				}
			} catch (IOException e) {
				try {
					System.out.println("Time out �߻�...");
					String msg = "server/ping";
					byte[] bytes = msg.getBytes();
					outMsg.write(bytes);
					System.out.println("ping ����");
				} catch (IOException e3) {
					e3.printStackTrace();
				}
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}

				// ���ŵ� �޽����� DATASIZE ����
				byte[] header = new byte[4];
				try {
					int headlength = inMsg.read(header);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				int length = Utils.byteToInt(header);

				// DATA ���̸�ŭ byte�迭 ����
				byte[] body = new byte[length];

				try {
					int bodylength;
					bodylength = inMsg.read(body);
					String json = new String(body, 4, bodylength - 4);
					msg = Utils.parseJSONMessage(json);
				} catch (IOException e1) {
					e1.printStackTrace();
				}

				// '/' �����ڸ� �������� �޽����� ���ڿ� �迭�� �Ľ�
				rmsg = msg.split("/");

				// FromŬ���̾�Ʈ, pong ����
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
		// System.out.println("[ChatThread]run() IOException �߻�!!");
		// }
		// }

	public static void main(String[] args) {
		MultiThreadedChatServer server = new MultiThreadedChatServer();
		server.start();
	}
}