package dev.wiji.pixelparty.enums;

public enum LeaderboardStatistic {

	NORMAL_WINS("normal_wins", "Normal Wins"),
	HYPER_WINS("hyper_wins", "Hyper Wins");

	public final String sqlName;
	public final String name;

	LeaderboardStatistic(String sqlName, String name) {
		this.sqlName = sqlName;
		this.name = name;
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
