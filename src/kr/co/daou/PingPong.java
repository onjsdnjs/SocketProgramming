package kr.co.daou;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class PingPong extends Thread {
	private int delayTime;
	private PrintWriter outMsg;

	// constructor:
	public PingPong(int delayTime, PrintWriter outMsg) {
		this.delayTime = delayTime;
		this.outMsg = outMsg;
	}

	public void run() {
		try {
			while (true) {
				Thread.sleep(delayTime);
				outMsg.println("message/ping");
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}