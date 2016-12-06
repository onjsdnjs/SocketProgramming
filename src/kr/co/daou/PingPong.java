package kr.co.daou;

import java.io.IOException;
import java.io.OutputStream;

import kr.co.daou.utils.Utils;

public class PingPong extends Thread {
	private String obj;
	private int delayTime;
	private OutputStream outMsg;

	// constructor:
	public PingPong(String obj, int delayTime, OutputStream outMsg) {
		this.delayTime = delayTime;
		this.outMsg = outMsg;
	}

	public void run() {
		while (true) {
			try {
				Thread.sleep(delayTime);
				outMsg.write(Utils.makeJSONMessageForPingPong(true).getBytes());

				outMsg.flush();
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}