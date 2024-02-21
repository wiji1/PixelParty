package dev.wiji.pixelparty.enums;

public enum LeaderboardType {
	WEEKLY("Weekly"),
	MONTHLY("Monthly"),
	LIFETIME("Lifetime");

	public final String displayName;

	LeaderboardType(String name) {
		this.displayName = name;
	}

	public String toString() {
		return name();
	}

	public static LeaderboardType fromString(String text) {
		for(LeaderboardType value : values()) {
			if(value.name().equalsIgnoreCase(text)) {
				return value;
			}
		}

		return null;
	}

}
