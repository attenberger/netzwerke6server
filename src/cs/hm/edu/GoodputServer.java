package cs.hm.edu;

import cs.hm.edu.server.TCPServer;
import cs.hm.edu.server.UDPServer;

import java.io.IOException;

public class GoodputServer {

	private static final int PORT = 63423;
	private static final int TIMEOUT = 5000;

	public static void main(String... args) throws IOException {
		boolean isTcp = Boolean.parseBoolean(args[0]);

		if (isTcp) {
			new TCPServer(PORT, TIMEOUT);
		} else {
			new UDPServer(PORT, TIMEOUT);
		}
	}

}
