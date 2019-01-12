package codes.fepi;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.net.SocketException;

public class Main {
	public static void main(String[] args) throws SocketException, JsonProcessingException {
		if (args.length > 0 && args[0].equals("--server")) {
			new Server().start();
		} else {
			new Client().start();
		}
	}
}
