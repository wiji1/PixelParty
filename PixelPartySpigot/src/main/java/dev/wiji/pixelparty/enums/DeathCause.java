package dev.wiji.pixelparty.enums;

public enum DeathCause {

	FALL("fell to their death"),
	FORFEIT("forfeited the game");


	final String message;

	DeathCause(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
}
