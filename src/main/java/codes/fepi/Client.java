package codes.fepi;

import codes.fepi.entity.ClientLoc;
import codes.fepi.entity.ClientRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class Client {

	private DatagramSocket socket;
	private byte[] requestBytes;
	private ObjectMapper mapper;
	private ScheduledExecutorService scheduler;
	private ClientLoc loc;
	private boolean gotPartner = false;
	byte[] zeros = new byte[256];

	Client() throws SocketException, JsonProcessingException {
		scheduler = Executors.newScheduledThreadPool(1);
		Scanner scanner = new Scanner(System.in);
		System.out.println("Your name?");
		String name = scanner.next();
		System.out.println("\nYour target?");
		String target = scanner.next();
		System.out.println();
		mapper = new ObjectMapper();
		ClientRequest request = new ClientRequest(name.charAt(0), target.charAt(0));
		requestBytes = mapper.writeValueAsBytes(request);
		socket = new DatagramSocket(9000 + (int) (Math.random() * 100.0));
	}

	void start() {
		scheduler.scheduleAtFixedRate(this::requestPartner, 1, 10, TimeUnit.SECONDS);
		new Thread(this::listen).start();
	}

	private void requestPartner() {
		try {
			System.out.println("Requesting partner...");
			DatagramPacket partnerRequest = new DatagramPacket(requestBytes, requestBytes.length, InetAddress.getLocalHost(), 9000);
			socket.send(partnerRequest);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void listen() {
		try {
			while (true) {
				byte[] buf = new byte[256];
				DatagramPacket rec = new DatagramPacket(buf, buf.length);
				socket.receive(rec);
				try {
					if (!gotPartner) {
						ClientLoc clientLoc = mapper.readValue(rec.getData(), ClientLoc.class);
						scheduler.shutdownNow();
						scheduler.awaitTermination(2, TimeUnit.SECONDS);
						scheduler = Executors.newScheduledThreadPool(1);
						scheduler.scheduleAtFixedRate(this::keepAlive, 10, 10, TimeUnit.SECONDS);
						System.out.println(String.format("Got connection to %c at %s:%d", clientLoc.getName(), clientLoc.getAddress().getHostAddress(), clientLoc.getPort()));
						loc = clientLoc;
						new Thread(this::send).start();
						gotPartner = true;
						continue;
					}
				} catch (Exception json) {
				}
				byte[] data = rec.getData();
				if (!Arrays.equals(data, zeros)) {
					System.out.println(new String(data).trim());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void send() {
		try {
			Scanner scanner = new Scanner(System.in);
			while (true) {
				String in = scanner.nextLine();
				if (loc == null) {
					System.out.println("Not connected yet!");
				} else {
					socket.send(new DatagramPacket(in.getBytes(), in.getBytes().length, loc.getAddress(), loc.getPort()));
				}
			}
		} catch (NoSuchElementException e) {
			//ignore that
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void keepAlive() {
		byte[] bytes = {0};
		try {
			socket.send(new DatagramPacket(bytes, bytes.length, loc.getAddress(), loc.getPort()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
