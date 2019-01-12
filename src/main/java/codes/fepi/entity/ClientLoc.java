package codes.fepi.entity;

import java.net.InetAddress;

public class ClientLoc {
	private InetAddress address;
	private int port;
	private char name;

	public ClientLoc() {
	}

	public ClientLoc(InetAddress address, int port, char name) {
		this.address = address;
		this.port = port;
		this.name = name;
	}

	public InetAddress getAddress() {
		return address;
	}

	public int getPort() {
		return port;
	}

	public char getName() {
		return name;
	}
}
