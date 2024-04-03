package dev.wiji.pixelparty.enums;

public enum ServerType {
	PRACTICE(1, 5, "world", "&bPractice"),
	NORMAL(16, 5, "world", "&eNormal"),
	HYPER(16, 5, "world", "&dHyper"),
	LOBBY(25, 3, "lobby", null),
	RANKED(16, -1, "world", "&cRanked");


	public final int defaultMaxPlayers;
	public final int maxServers;
	public final String worldName;
	public final String displayName;

	ServerType(int defaultMaxPlayers, int maxServers, String worldName, String displayName) {
		this.defaultMaxPlayers = defaultMaxPlayers;
		this.maxServers = maxServers;
		this.worldName = worldName;
		this.displayName = displayName;
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
