package kr.co.daou.client;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import kr.co.daou.format.Const;
import kr.co.daou.utils.Utils;

public class MultiThreadChatClient implements ActionListener, Runnable {
	private String ip;
	private String id;

	private Socket socket;
	private InputStream inMsg = null;
	private OutputStream outMsg = null;
	private int timeout = 6000;

	// ���� �޽��� ��Ƶδ� ���� ����
	private byte[] messageBuffer = new byte[100];
	private int receiveDataSize;
	private byte[] receiveData = null;

	// �α��� �г�
	private JPanel loginPanel;
	// �α��� ��ư
	private JButton loginButton;
	// ��ȭ�� ��
	private JLabel label1;
	// ��ȭ�� �Է� �ؽ�Ʈ �ʵ�
	private JTextField idInput;

	// �α׾ƿ� �г� ����
	private JPanel logoutPanel;
	// ��ȭ�� ��� ��
	private JLabel label2;
	// �α׾ƿ� ��ư
	private JButton logoutButton;

	// �Է� �г� ����
	private JPanel msgPanel;
	// �޽��� �Է� �ؽ�Ʈ �ʵ�
	private JTextField msgInput;
	// ���� ��ư
	private JButton exitButton;
	// �ӼӸ� ��ư
	private JButton whisperButton;
	// ��� ��ư
	private JButton blockButton;

	// ���� ������
	private JFrame jframe;
	// ä�� ���� ��� â
	private JTextArea msgOut;
	// ī�� ���̾ƿ� ����
	private Container tab;
	private CardLayout clayout;

	private Thread thread;
	// ���� �÷���
	boolean status;
	boolean logoutFlag;

	// Java SWING �ڵ�
	public MultiThreadChatClient(String ip) {
		this.ip = ip;

		// �α��� �г� ����
		loginPanel = new JPanel();
		// ���̾ƿ� ����
		loginPanel.setLayout(new BorderLayout());
		idInput = new JTextField(15);
		loginButton = new JButton("�α���");
		// �̺�Ʈ ������ ���
		loginButton.addActionListener(this);
		label1 = new JLabel("��ȭ��");
		// �гο� ���� ����
		loginPanel.add(label1, BorderLayout.WEST);
		loginPanel.add(idInput, BorderLayout.CENTER);
		loginPanel.add(loginButton, BorderLayout.EAST);

		// �α׾ƿ� �г� ����
		logoutPanel = new JPanel();
		// ���̾ƿ� ����
		logoutPanel.setLayout(new BorderLayout());
		label2 = new JLabel();
		logoutButton = new JButton("�α׾ƿ�");
		// �̺�Ʈ ������ ���
		logoutButton.addActionListener(this);
		// �гο� ���� ����
		logoutPanel.add(label2, BorderLayout.CENTER);
		logoutPanel.add(logoutButton, BorderLayout.EAST);

		// �Է� �г� ����
		msgPanel = new JPanel();
		whisperButton = new JButton("�ӼӸ�");
		whisperButton.addActionListener(this);
		blockButton = new JButton("Block");
		blockButton.addActionListener(this);
		// ���̾ƿ� ����
		msgPanel.setLayout(new BorderLayout());
		msgInput = new JTextField(30);
		// �̺�Ʈ ������ ���
		msgInput.addActionListener(this);
		exitButton = new JButton("����");
		exitButton.addActionListener(this);
		// �гο� ���� ����
		msgPanel.add(whisperButton, BorderLayout.WEST);
		msgPanel.add(msgInput, BorderLayout.CENTER);
		msgPanel.add(exitButton, BorderLayout.EAST);
		msgPanel.add(blockButton, BorderLayout.SOUTH);

		// �α��� / �α׾ƿ� �г� ������ ���� ī�� ���̾ƿ� �г�
		tab = new JPanel();
		clayout = new CardLayout();
		tab.setLayout(clayout);
		tab.add(loginPanel, "login");
		tab.add(logoutPanel, "logout");

		// ���� ������ ����
		jframe = new JFrame("::��Ƽê::");
		msgOut = new JTextArea("", 10, 30);

		// JTextArea�� ������ �������� ���ϰ� ��. �� ��� �������� ���
		msgOut.setEditable(false);
		// ���� ��ũ�ѹٴ� �׻� ��Ÿ����, ���� ��ũ�ѹٴ� �ʿ��� ���� ��Ÿ���� ��
		JScrollPane jsp = new JScrollPane(msgOut, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jframe.add(tab, BorderLayout.NORTH);
		jframe.add(jsp, BorderLayout.CENTER);
		jframe.add(msgPanel, BorderLayout.SOUTH);
		// �α��� �г��� �켱 ǥ��
		clayout.show(tab, "login");
		// ������ ũ�� �ڵ� ����
		jframe.pack();
		// ������ ũ�� ���� �Ұ� ����
		jframe.setResizable(false);
		// ������ ǥ��
		jframe.setVisible(true);
	} // end of MultiThreadChatClient������

	// �̺�Ʈ ó��
	@Override
	public void actionPerformed(ActionEvent e) {
		Object obj = e.getSource();

		// ���� ��ư ó��
		if (obj == exitButton) {
			this.exit();
		} else if (obj == loginButton) {
			id = idInput.getText();
			label2.setText("��ȭ�� : " + id);
			clayout.show(tab, "logout");
			this.connectServer();
		} else if (obj == logoutButton) {
			this.logout();
			// ��ȭ â Ŭ����
			msgOut.setText("");
			// �α��� �гη� ��ȯ
			clayout.show(tab, "login");
		} else if (obj == whisperButton) {
			this.whisper(msgInput.getText());

		} else if (obj == blockButton) {
			this.block(msgInput.getText());

		} else if (obj == msgInput) {
			this.sendMsg(msgInput.getText());
		}
	}

	// �α��� ��ư Ŭ����
	private boolean connectServer() {
		try {
			// ���� ����
			socket = new Socket(ip, 9999);
			System.out.println("[" + id + "]Server ���� ����");
			// ����� ��Ʈ�� ����
			this.inMsg = socket.getInputStream();
			this.outMsg = socket.getOutputStream();
			// String message = this.id + "/login";
			// ������ ���� JSON �޼��� ����
			byte[] bytes = Utils.makeMessageStringToByte(Utils.makeJSONMessageForAuth(id, id));
			// ������ �α��� �޽��� ����
			this.outMsg.write(Utils.mergeBytearrays(Utils.intTobyte(bytes.length), bytes));
			this.outMsg.flush();
			// �޽��� ������ ���� ������ ����
			thread = new Thread(this);
			thread.start();
			return true;
		} catch (Exception e) {
			System.out.println("[MultiChatClient]connectServer() Exception �߻�!!");
			return false;
		}
	}

	private void sendMsg(String msg) {
		String message = id + "/" + msg;
		byte[] bytes = message.getBytes();
		try {
			this.outMsg.write(bytes);
			this.outMsg.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// �Է�â Ŭ����
		msgInput.setText("");
	}

	private void whisper(String msg) {
		String[] temp = msg.split("/");
		String message = id + "/whisper" + "/" + temp[0] + "/" + temp[1];
		byte[] bytes = message.getBytes();
		// ������ �α��� �޽��� ����
		try {
			this.outMsg.write(bytes);
			this.outMsg.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		msgInput.setText("");
	}

	private void block(String msg) {

		String[] temp = msg.split("/");
		String message = id + "/block" + "/" + temp[0] + "/" + temp[1];
		byte[] bytes = message.getBytes();

		try {
			this.outMsg.write(bytes);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// �Է�â Ŭ����
		msgInput.setText("");
	}

	private void exit() {
		System.exit(0);
	}

	private void logout() {
		logoutFlag = true;
		// �α׾ƿ� �޽��� ����
		String message = id + "/logout";
		byte[] bytes = message.getBytes();

		try {
			this.outMsg.write(bytes);
			this.outMsg.flush();
			outMsg.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			inMsg.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		status = false;
	}

	private void onReceiveMsg(String id, String msg) {
		// From����, ping ����
		if (msg.equals("ping")) {
			try {
				// ������ ���� JSON �޼��� ����
				byte[] bytes = Utils.makeMessageStringToByte(Utils.makeJSONMessageForPingPong(false));
				// ������ �α��� �޽��� ����
				this.outMsg.write(Utils.mergeBytearrays(Utils.intTobyte(bytes.length), bytes));
				this.outMsg.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("Pong ����");
		} else {
			// JTextArea�� ���ŵ� �޽��� �߰�
			msgOut.append(id + ">" + msg + "\n");
		}
	}

	@Override
	public void run() {
		// ���� �޽����� ó���ϴ� ����
		String msg;
		String[] rmsg;
		status = true;

		while (status) {
			try {
				// timeout ����
				socket.setSoTimeout(timeout);
				// ���ŵ� �޽����� DATASIZE ����
				receiveDataSize = inMsg.read(messageBuffer);
				// ������ ���� �� ������ format
				if (receiveDataSize != 0) {
					receiveData = new byte[receiveDataSize];
					// src, srcPos, dest, destPos, length
					System.arraycopy(messageBuffer, 0, receiveData, 0, receiveDataSize);
				}

				// byte[] offset, length
				msg = new String(receiveData, 0, receiveData.length);
				// '/' �����ڸ� �������� �޽����� ���ڿ� �迭�� �Ľ�
				// msg = Utils.parseJSONMessage(msg);
				rmsg = msg.split("/");
				this.onReceiveMsg(rmsg[0], rmsg[1]);

			} catch (IOException e) {
				// ������ ���� ���
				if (logoutFlag == false) {
					try {
						// Ping �޽��� ����
						System.out.println("Time out �߻�...");
						byte[] bytes = Utils.makeMessageStringToByte(Utils.makeJSONMessageForPingPong(true));
						this.outMsg.write(Utils.mergeBytearrays(Utils.intTobyte(bytes.length), bytes));
						this.outMsg.flush();
						System.out.println("ping ����");
					} catch (IOException e1) {
						e1.printStackTrace();
					}

					try {
						Thread.sleep(5000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					// ���ŵ� �޽����� DATASIZE ����
					try {
						receiveDataSize = inMsg.read(messageBuffer);
						// ������ ���� �� ������ format
						if (receiveDataSize != 0) {
							receiveData = new byte[receiveDataSize];
							// src, srcPos, dest, destPos, length
							System.arraycopy(messageBuffer, 0, receiveData, 0, receiveDataSize);
						}
						// byte[] offset, length
						msg = new String(receiveData, 0, receiveData.length);

						// '/' �����ڸ� �������� �޽����� ���ڿ� �迭�� �Ľ�
						// msg = Utils.parseJSONMessage(msg);
						rmsg = msg.split("/");

						// From����, pong ����
						if (rmsg[1].equals("pong")) {
							System.out.println(rmsg[0] + "/pong");
						}
					} catch (IOException e1) {
						// ������ ���� ���
						if (logoutFlag == false) {
							boolean flag = true;
							while (flag) {
								if (connectServer())
									flag = false;
							}
						}
					}
				}

				if (logoutFlag == true) {
					// Ŭ���̾�Ʈ�� ���� ���
					System.out.println("[MultiChatClient]" + thread.getName() + "�����!");
					status = false;
				}
			}
		} // end of while
	}

	public static void main(String[] args) {
		MultiThreadChatClient mcc = new MultiThreadChatClient(Const.SERVER_IP);
	}
}
