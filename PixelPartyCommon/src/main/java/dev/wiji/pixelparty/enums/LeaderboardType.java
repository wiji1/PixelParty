package dev.wiji.pixelparty.enums;

public enum LeaderboardType {
	WEEKLY("Weekly"),
	MONTHLY("Monthly"),
	LIFETIME("Lifetime");

	public final String displayName;

	LeaderboardType(String name) {
		this.displayName = name;
	}
}
