package codes.fepi;

import codes.fepi.entity.ClientLoc;
import codes.fepi.entity.ClientRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class Server {

	Map<Character, ClientLoc> clients;
	DatagramSocket socket;
	ObjectMapper mapper;

	Server() throws SocketException {
		mapper = new ObjectMapper();
		clients = new ConcurrentHashMap<>();
		socket = new DatagramSocket(9000);
	}

	void start() {
		Thread receiveThread = new Thread(this::receive);
		receiveThread.start();
	}

	void receive() {
		try {
			while (true) {
				byte[] buf = new byte[128];
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				socket.receive(packet);
				ClientRequest clientRequest = mapper.readValue(packet.getData(), ClientRequest.class);
				ClientLoc searchingClient = clients.get(clientRequest.getName());
				ClientLoc requesterClient = new ClientLoc(packet.getAddress(), packet.getPort(), clientRequest.getName());
				if(searchingClient != null) {
					if(searchingClient.getName() == clientRequest.getTarget()) {
						System.out.println(String.format("Got a pair: %c :: %c", requesterClient.getName(), searchingClient.getName()));
						// mutual searching
						byte[] requester = mapper.writeValueAsBytes(requesterClient);
						DatagramPacket toSearching = new DatagramPacket(requester, requester.length, searchingClient.getAddress(), searchingClient.getPort());
						byte[] searching = mapper.writeValueAsBytes(searchingClient);
						DatagramPacket toRequester = new DatagramPacket(searching, searching.length, requesterClient.getAddress(), requesterClient.getPort());
						socket.send(toSearching);
						socket.send(toRequester);
						continue;
					}
				}
				clients.put(clientRequest.getTarget(), requesterClient);
				System.out.println(String.format("Saving request from %c, target: %c", clientRequest.getName(), clientRequest.getTarget()));
			}
		} catch (Exception e) {
			e.printStackTrace();
			socket.close();
		}
	}
}
