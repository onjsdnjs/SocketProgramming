package kr.co.daou;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class MultiThreadChatClient implements ActionListener, Runnable {
	private String ip;
	private String id;
	private Socket socket;
	private BufferedReader inMsg = null;
	private PrintWriter outMsg = null;
	private int timeout = 12000;
	private File file = new File("C:/Users/user/Desktop/txtFile.txt");
	private int dataSize;

	// 로그인 패널
	private JPanel loginPanel;
	// 로그인 버튼
	private JButton loginButton;
	// 대화명 라벨
	private JLabel label1;
	// 대화명 입력 텍스트 필드
	private JTextField idInput;

	// 로그아웃 패널 구성
	private JPanel logoutPanel;
	// 대화명 출력 라벨
	private JLabel label2;
	// 로그아웃 버튼
	private JButton logoutButton;

	// 입력 패널 구성
	private JPanel msgPanel;
	// 메시지 입력 텍스트 필드
	private JTextField msgInput;
	// 종료 버튼
	private JButton exitButton;
	// 귓속말 버튼
	private JButton whisperButton;
	// 블락 버튼
	private JButton blockButton;

	// 메인 윈도우
	private JFrame jframe;
	// 채팅 내용 출력 창
	private JTextArea msgOut;
	// 카드 레이아웃 관련
	private Container tab;
	private CardLayout clayout;

	private Thread thread;
	// 상태 플래그
	boolean status;

	public MultiThreadChatClient(String ip) {
		this.ip = ip;

		// 로그인 패널 구성
		loginPanel = new JPanel();
		// 레이아웃 설정
		loginPanel.setLayout(new BorderLayout());
		idInput = new JTextField(15);
		loginButton = new JButton("로그인");
		// 이벤트 리스너 등록
		loginButton.addActionListener(this);
		label1 = new JLabel("대화명");
		// 패널에 위젯 구성
		loginPanel.add(label1, BorderLayout.WEST);
		loginPanel.add(idInput, BorderLayout.CENTER);
		loginPanel.add(loginButton, BorderLayout.EAST);

		// 로그아웃 패널 구성
		logoutPanel = new JPanel();
		// 레이아웃 설정
		logoutPanel.setLayout(new BorderLayout());
		label2 = new JLabel();
		logoutButton = new JButton("로그아웃");
		// 이벤트 리스너 등록
		logoutButton.addActionListener(this);
		// 패널에 위젯 구성
		logoutPanel.add(label2, BorderLayout.CENTER);
		logoutPanel.add(logoutButton, BorderLayout.EAST);

		// 입력 패널 구성
		msgPanel = new JPanel();
		whisperButton = new JButton("귓속말");
		whisperButton.addActionListener(this);
		blockButton = new JButton("Block");
		blockButton.addActionListener(this);
		// 레이아웃 설정
		msgPanel.setLayout(new BorderLayout());
		msgInput = new JTextField(30);
		// 이벤트 리스너 등록
		msgInput.addActionListener(this);
		exitButton = new JButton("종료");
		exitButton.addActionListener(this);
		// 패널에 위젯 구성
		msgPanel.add(whisperButton, BorderLayout.WEST);
		msgPanel.add(msgInput, BorderLayout.CENTER);
		msgPanel.add(exitButton, BorderLayout.EAST);
		msgPanel.add(blockButton, BorderLayout.SOUTH);

		// 로그인 / 로그아웃 패널 선택을 위한 카드 레이아웃 패널
		tab = new JPanel();
		clayout = new CardLayout();
		tab.setLayout(clayout);
		tab.add(loginPanel, "login");
		tab.add(logoutPanel, "logout");

		// 메인 프레임 구성
		jframe = new JFrame("::멀티챗::");
		msgOut = new JTextArea("", 10, 30);

		// JTextArea의 내용을 수정하지 못하게 함. 즉 출력 전용으로 사용
		msgOut.setEditable(false);
		// 수직 스크롤바는 항상 나타내고, 수평 스크롤바는 필요할 때만 나타나게 함
		JScrollPane jsp = new JScrollPane(msgOut, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jframe.add(tab, BorderLayout.NORTH);
		jframe.add(jsp, BorderLayout.CENTER);
		jframe.add(msgPanel, BorderLayout.SOUTH);
		// 로그인 패널을 우선 표시
		clayout.show(tab, "login");
		// 프레임 크기 자동 설정
		jframe.pack();
		// 프레임 크기 조정 불가 설정
		jframe.setResizable(false);
		// 프레임 표시
		jframe.setVisible(true);
	} // end of MultiThreadChatClient생성자

	// 이벤트 처리
	@Override
	public void actionPerformed(ActionEvent e) {
		Object obj = e.getSource();

		// 종료 버튼 처리
		if (obj == exitButton) {
			this.exit();
		} else if (obj == loginButton) {
			id = idInput.getText();
			label2.setText("대화명 : " + id);
			clayout.show(tab, "logout");
			this.connectServer();
		} else if (obj == logoutButton) {
			this.logout();
			// 대화 창 클리어
			msgOut.setText("");
			// 로그인 패널로 전환
			clayout.show(tab, "login");
		} else if (obj == whisperButton) {
			this.whisper(msgInput.getText());

		} else if (obj == blockButton) {
			this.block(msgInput.getText());

		} else if (obj == msgInput) {
			this.sendMsg(msgInput.getText());
		}
	}

	private void connectServer() {
		try {
			// 소켓 생성
			socket = new Socket(ip, 9999);
			System.out.println("[Client]Server 연결 성공");

			// 입출력 스트림 생성
			this.inMsg = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
			this.outMsg = new PrintWriter(socket.getOutputStream(), true);

			// 서버에 로그인 메시지 전달
			this.outMsg.println(this.id + "/login");

			// 메시지 수신을 위한 스레드 생성
			thread = new Thread(this);
			thread.start();
		} catch (Exception e) {
			System.out.println("[MultiChatClient]connectServer() Exception 발생!!");
		}
	}

	private void sendMsg(String msg) {

		// 파일을 읽어서 출력
		if (file.exists()) {
			BufferedReader reader = null;
			String line = "";
			String m = null;
			try {
				reader = new BufferedReader(new FileReader(file));
				while ((m = reader.readLine()) != null) {
					line += m;
				}
				outMsg.println(id + "/" + line);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// 메시지 전송
		// outMsg.println(id + "/" + msg);
		// 입력창 클리어
		msgInput.setText("");
	}

	private void whisper(String msg) {
		// 메시지 전송
		String[] temp = msg.split("/");
		outMsg.println(id + "/whisper" + "/" + temp[0] + "/" + temp[1]);
		// 입력창 클리어
		msgInput.setText("");
	}

	private void block(String msg) {
		// 메시지 전송
		String[] temp = msg.split("/");
		outMsg.println(id + "/block" + "/" + temp[0] + "/" + temp[1]);
		// 입력창 클리어
		msgInput.setText("");
	}

	private void exit() {
		System.exit(0);
	}

	private void logout() {
		// 로그아웃 메시지 전송
		outMsg.println(id + "/logout");
		outMsg.close();
		try {
			inMsg.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		status = false;
	}

	private void onReceiveMsg(String id, String msg) {
		// From서버, ping 받음
		if (msg.equals("ping")) {
			outMsg.println(this.id + "/pong");
		}
		// From서버, pong 받음
		else if (msg.equals("pong")) {
			System.out.println(id + "/pong");
		} else if (msg.equals("dataSize")) {
			// 파일의 크기 전송
			outMsg.println(file.length());
		} else {
			// JTextArea에 수신된 메시지 추가
			msgOut.append(id + ">" + msg + "\n");
		}
	}

	@Override
	public void run() {
		// 수신 메시지를 처리하는 변수
		String msg;
		String[] rmsg;

		status = true;

		try {
			socket.setSoTimeout(timeout);
			// 클라이언트에게 timeout 설정
			new PingPong(10000, outMsg).start();

			while (status) {
				// 메시지 수신과 파싱
				msg = inMsg.readLine();
				rmsg = msg.split("/");
				this.onReceiveMsg(rmsg[0], rmsg[1]);
			}
		} catch (IOException e) {
			status = false;
		}
		System.out.println("[MultiChatClient]" + thread.getName() + "종료됨");
	}

	public static void main(String[] args) {
		MultiThreadChatClient mcc = new MultiThreadChatClient("127.0.0.1");
	}
}
