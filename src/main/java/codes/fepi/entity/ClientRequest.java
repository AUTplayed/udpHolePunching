package codes.fepi.entity;

public class ClientRequest {
	private char name;
	private char target;

	public ClientRequest() {
	}

	public ClientRequest(char name, char target) {
		this.name = name;
		this.target = target;
	}

	public char getName() {
		return name;
	}

	public char getTarget() {
		return target;
	}
}
