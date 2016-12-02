package kr.co.daou;

import java.io.IOException;
import java.io.OutputStream;

public class PingPong extends Thread {
	private int delayTime;
	private OutputStream outMsg;

	// constructor:
	public PingPong(int delayTime, OutputStream outMsg) {
		this.delayTime = delayTime;
		this.outMsg = outMsg;
	}

	public void run() {
		while (true) {
			try {
				Thread.sleep(delayTime);
				byte[] b = new byte[12288];
				String str = "message/ping";
				b = str.getBytes();
				outMsg.write(b, 0, b.length);
				outMsg.flush();
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}