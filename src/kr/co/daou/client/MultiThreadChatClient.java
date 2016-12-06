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

	// 수신 메시지 담아두는 변수 선언
	private byte[] messageBuffer = new byte[100];
	private int receiveDataSize;
	private byte[] receiveData = null;

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
	boolean logoutFlag;

	// Java SWING 코드
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

	// 로그인 버튼 클릭시
	private boolean connectServer() {
		try {
			// 소켓 생성
			socket = new Socket(ip, 9999);
			System.out.println("[" + id + "]Server 연결 성공");
			// 입출력 스트림 생성
			this.inMsg = socket.getInputStream();
			this.outMsg = socket.getOutputStream();
			// String message = this.id + "/login";
			// 인증을 위한 JSON 메세지 생성
			byte[] bytes = Utils.makeMessageStringToByte(Utils.makeJSONMessageForAuth(id, id));
			// 서버에 로그인 메시지 전달
			this.outMsg.write(Utils.mergeBytearrays(Utils.intTobyte(bytes.length), bytes));
			this.outMsg.flush();
			// 메시지 수신을 위한 스레드 생성
			thread = new Thread(this);
			thread.start();
			return true;
		} catch (Exception e) {
			System.out.println("[MultiChatClient]connectServer() Exception 발생!!");
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
		// 입력창 클리어
		msgInput.setText("");
	}

	private void whisper(String msg) {
		String[] temp = msg.split("/");
		String message = id + "/whisper" + "/" + temp[0] + "/" + temp[1];
		byte[] bytes = message.getBytes();
		// 서버에 로그인 메시지 전달
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

		// 입력창 클리어
		msgInput.setText("");
	}

	private void exit() {
		System.exit(0);
	}

	private void logout() {
		logoutFlag = true;
		// 로그아웃 메시지 전송
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
		// From서버, ping 받음
		if (msg.equals("ping")) {
			try {
				// 인증을 위한 JSON 메세지 생성
				byte[] bytes = Utils.makeMessageStringToByte(Utils.makeJSONMessageForPingPong(false));
				// 서버에 로그인 메시지 전달
				this.outMsg.write(Utils.mergeBytearrays(Utils.intTobyte(bytes.length), bytes));
				this.outMsg.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("Pong 전송");
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

		while (status) {
			try {
				// timeout 설정
				socket.setSoTimeout(timeout);
				// 수신된 메시지를 DATASIZE 길이
				receiveDataSize = inMsg.read(messageBuffer);
				// 서버로 부터 온 데이터 format
				if (receiveDataSize != 0) {
					receiveData = new byte[receiveDataSize];
					// src, srcPos, dest, destPos, length
					System.arraycopy(messageBuffer, 0, receiveData, 0, receiveDataSize);
				}

				// byte[] offset, length
				msg = new String(receiveData, 0, receiveData.length);
				// '/' 구분자를 기준으로 메시지를 문자열 배열로 파싱
				// msg = Utils.parseJSONMessage(msg);
				rmsg = msg.split("/");
				this.onReceiveMsg(rmsg[0], rmsg[1]);

			} catch (IOException e) {
				// 서버가 죽은 경우
				if (logoutFlag == false) {
					try {
						// Ping 메시지 전송
						System.out.println("Time out 발생...");
						byte[] bytes = Utils.makeMessageStringToByte(Utils.makeJSONMessageForPingPong(true));
						this.outMsg.write(Utils.mergeBytearrays(Utils.intTobyte(bytes.length), bytes));
						this.outMsg.flush();
						System.out.println("ping 전송");
					} catch (IOException e1) {
						e1.printStackTrace();
					}

					try {
						Thread.sleep(5000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					// 수신된 메시지를 DATASIZE 길이
					try {
						receiveDataSize = inMsg.read(messageBuffer);
						// 서버로 부터 온 데이터 format
						if (receiveDataSize != 0) {
							receiveData = new byte[receiveDataSize];
							// src, srcPos, dest, destPos, length
							System.arraycopy(messageBuffer, 0, receiveData, 0, receiveDataSize);
						}
						// byte[] offset, length
						msg = new String(receiveData, 0, receiveData.length);

						// '/' 구분자를 기준으로 메시지를 문자열 배열로 파싱
						// msg = Utils.parseJSONMessage(msg);
						rmsg = msg.split("/");

						// From서버, pong 받음
						if (rmsg[1].equals("pong")) {
							System.out.println(rmsg[0] + "/pong");
						}
					} catch (IOException e1) {
						// 서버가 죽은 경우
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
					// 클라이언트가 죽은 경우
					System.out.println("[MultiChatClient]" + thread.getName() + "종료됨!");
					status = false;
				}
			}
		} // end of while
	}

	public static void main(String[] args) {
		MultiThreadChatClient mcc = new MultiThreadChatClient(Const.SERVER_IP);
	}
}
