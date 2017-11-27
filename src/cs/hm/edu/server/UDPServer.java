package cs.hm.edu.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public class UDPServer implements Measureable<DatagramPacket> {

	private static final ExecutorService executor = Executors.newFixedThreadPool(1);

	private static final int BUFFER_SIZE = 1400;

	private boolean firstPacket = true;

	private AtomicLong firstArrived = new AtomicLong();
	private AtomicLong lastArrived = new AtomicLong();

	private AtomicLong counter = new AtomicLong(0);

	public UDPServer(int port, int timeout) {
		try (DatagramSocket serverSocket = new DatagramSocket(port)) {
			while (!serverSocket.isClosed()) {
				DatagramPacket received = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);

				serverSocket.receive(received);

				if (firstPacket) {
					// Save start time.
					firstArrived.set(System.nanoTime());

					// Set timeout for future received packets.
					serverSocket.setSoTimeout(timeout);

					// Increment counter.
					counter.incrementAndGet();
					firstPacket = false;
				}

				executor.execute(() -> {
					measure(received);
				});
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		executor.shutdown();
		double seconds = (lastArrived.get() - firstArrived.get()) / (double) (1000 * 1000 * 1000);
		double dataRate = (counter.get() * BUFFER_SIZE) / seconds;

		System.out.println(counter.get() + "Pakete in " + seconds + " Sekunden mit einer Datenrate von " + dataRate + " B/s");
	}

	@Override
	public void measure(DatagramPacket received) {
		// Update current timer.
		lastArrived.set(System.nanoTime());

		// Increment counter.
		counter.incrementAndGet();

		ByteBuffer buffer = ByteBuffer.wrap(received.getData());

		short sequenceNum = buffer.getShort(0);

		//System.out.println("Received UDP Packet with number " + sequenceNum);
	}

}
