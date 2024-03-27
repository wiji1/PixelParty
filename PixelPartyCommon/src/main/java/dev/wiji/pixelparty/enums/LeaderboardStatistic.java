package dev.wiji.pixelparty.enums;

public enum LeaderboardStatistic {

	NORMAL_WINS("normal_wins", "Normal Wins", false),
	HYPER_WINS("hyper_wins", "Hyper Wins", false),
	NORMAL_ELO("normal_elo", "Normal Elo", true),
	HYPER_ELO("hyper_elo", "Hyper Elo", true),
		;

	public final String sqlName;
	public final String name;
	public final boolean lifetimeOnly;

	LeaderboardStatistic(String sqlName, String name, boolean lifetimeOnly) {
		this.sqlName = sqlName;
		this.name = name;
		this.lifetimeOnly = lifetimeOnly;
	}

	public String toString() {
		return sqlName;
	}

	public static LeaderboardStatistic fromString(String text) {
		for(LeaderboardStatistic value : values()) {
			if(value.sqlName.equalsIgnoreCase(text)) {
				return value;
			}
		}
		return null;
	}
}
