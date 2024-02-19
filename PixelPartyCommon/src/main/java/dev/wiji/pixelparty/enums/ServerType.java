package dev.wiji.pixelparty.enums;

public enum ServerType {
	PRACTICE(1, 5, "world"),
	NORMAL(16, 5, "world"),
	HYPER(16, 5, "world"),
	LOBBY(25, 3, "lobby");

	public final int defaultMaxPlayers;
	public final int maxServers;
	public final String worldName;

	ServerType(int defaultMaxPlayers, int maxServers, String worldName) {
		this.defaultMaxPlayers = defaultMaxPlayers;
		this.maxServers = maxServers;
		this.worldName = worldName;
	}
	
	public String getIdentifier() {
		return name().substring(0, 3);
	}

	public static ServerType getServerType(String serverName) {
		for(ServerType type : values()) {
			if(serverName.contains(type.getIdentifier())) {
				return type;
			}
		}
		return null;
	}
}
