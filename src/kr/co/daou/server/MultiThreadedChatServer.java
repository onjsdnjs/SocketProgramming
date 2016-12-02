package kr.co.daou.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import kr.co.daou.db.DBHelper;

public class MultiThreadedChatServer {

	// ���� ���ϰ� Ŭ���̾�Ʈ ���� ����
	private ServerSocket serverSocket = null;
	private Socket clientSocket = null;
	private DBHelper helper = null;

	// ����� Ŭ���̾�Ʈ �����带 �����ϴ� ArrayList
	private ArrayList<ChatThread> chatlist = new ArrayList<ChatThread>();

	public MultiThreadedChatServer() {
		helper = new DBHelper();
	}

	public void start() {
		try {
			// ���� ���� ����
			serverSocket = new ServerSocket(9999);
			System.out.println("Server START...");
			while (true) {
				clientSocket = serverSocket.accept();
				// ����� Ŭ���̾�Ʈ���� ������ Ŭ���� ����
				ChatThread chat = new ChatThread(clientSocket, chatlist);
				chatlist.add(chat);
				chat.start();
			}
		} catch (IOException e) {
			System.out.println(e);
			System.out.println("[MultiChatServer]start() Exception �߻�!!");
		}
	}

	public static void main(String[] args) {
		MultiThreadedChatServer server = new MultiThreadedChatServer();
		server.start();
	}
}
