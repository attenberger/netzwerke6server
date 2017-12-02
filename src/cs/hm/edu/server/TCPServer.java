package cs.hm.edu.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
//import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class TCPServer implements Measureable<Socket> {

	private static final int BUFFER_SIZE = 1400;

	private final ExecutorService executor = Executors.newSingleThreadExecutor();

	private final int timeout;

	private final AtomicBoolean firstPacket = new AtomicBoolean(true);

	private AtomicLong firstArrived = new AtomicLong();
	private AtomicLong lastArrived = new AtomicLong();

	private AtomicLong counter = new AtomicLong();

	public TCPServer(int port, int timeout) {
		this.timeout = timeout;

		try (ServerSocket serverSocket = new ServerSocket(port)) {
			System.out.println("Waiting for connection...");
			Socket client = serverSocket.accept();

			System.out.println("Got connection");

			executor.execute(() -> {
				measure(client);
			});


		} catch (IOException e) {
			e.printStackTrace();
		}

		executor.shutdown();
	}

	@Override
	public void measure(Socket socket) {
		boolean setTimeout = true;

		try (InputStream in = socket.getInputStream()) {
			byte[] buffer = new byte[BUFFER_SIZE];
			while (true) {
				for (int i = 0; i < BUFFER_SIZE; i++) {
					buffer[i] = (byte) in.read();
				}

				synchronized (firstPacket) {
					if (firstPacket.get()) {
						firstPacket.set(false);
						firstArrived.set(System.nanoTime());
					}
				}
				// Update current timer.
				lastArrived.set(System.nanoTime());

				// Increment counter.
				counter.incrementAndGet();

//				ByteBuffer b = ByteBuffer.wrap(buffer);
//				short sequenceNum = b.getShort(0);
//				System.out.println(sequenceNum);
//				if (sequenceNum == 0)
//					System.out.println(Arrays.toString(buffer));

				if (setTimeout) {
					// Set timeout on socket.
					try {
						socket.setSoTimeout(timeout);
					} catch (SocketException e) {
						e.printStackTrace();
					}
					setTimeout = false;
				}
			}
		} catch (IOException e) {
			System.out.println("Verbindung zum Client unterbrochen.");

			if (!socket.isClosed()) {
				try {
					socket.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}


		double seconds = (lastArrived.get() - firstArrived.get()) / (double) (1000 * 1000 * 1000);
		double dataRate = (counter.get() * BUFFER_SIZE) / seconds;

		System.out.println(counter.get() + "Pakete in " + seconds + " Sekunden mit einer Datenrate von " + dataRate + " B/s");
	}

}
