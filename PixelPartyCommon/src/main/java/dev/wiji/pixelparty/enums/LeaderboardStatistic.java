package dev.wiji.pixelparty.enums;

public enum LeaderboardStatistic {

	NORMAL_WINS("normal_wins", "Normal Wins", false, 0),
	HYPER_WINS("hyper_wins", "Hyper Wins", false, 0),
	NORMAL_ELO("normal_elo", "Normal Elo", true, 1000),
	HYPER_ELO("hyper_elo", "Hyper Elo", true, 1000),
		;

	public final String sqlName;
	public final String name;
	public final boolean lifetimeOnly;
	public final int defaultValue;

	LeaderboardStatistic(String sqlName, String name, boolean lifetimeOnly, int defaultValue) {
		this.sqlName = sqlName;
		this.name = name;
		this.lifetimeOnly = lifetimeOnly;
		this.defaultValue = defaultValue;
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
