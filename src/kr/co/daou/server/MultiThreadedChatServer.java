package kr.co.daou.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import kr.co.daou.db.DBHelper;

public class MultiThreadedChatServer {

	// 서버 소켓과 클라이언트 연결 소켓
	private ServerSocket serverSocket = null;
	private Socket clientSocket = null;
	private DBHelper helper = null;

	// 연결된 클라이언트 스레드를 관리하는 ArrayList
	private ArrayList<ChatThread> chatlist = new ArrayList<ChatThread>();

	public MultiThreadedChatServer() {
		helper = new DBHelper();
	}

	public void start() {
		try {
			// 서버 소켓 생성
			serverSocket = new ServerSocket(9999);
			System.out.println("Server START...");
			while (true) {
				clientSocket = serverSocket.accept();
				// 연결된 클라이언트에서 스레드 클래스 생성
				ChatThread chat = new ChatThread(clientSocket, chatlist);
				chatlist.add(chat);
				chat.start();
			}
		} catch (IOException e) {
			System.out.println(e);
			System.out.println("[MultiChatServer]start() Exception 발생!!");
		}
	}

	public static void main(String[] args) {
		MultiThreadedChatServer server = new MultiThreadedChatServer();
		server.start();
	}
}
