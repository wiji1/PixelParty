package dev.wiji.pixelparty.playerdata;

import dev.wiji.pixelparty.enums.LeaderboardStatistic;

import java.util.UUID;

public class LeaderboardPosition {
	public final UUID uuid;
	public final LeaderboardStatistic statistic;
	public int position;
	public int value;

	public LeaderboardPosition(UUID uuid, LeaderboardStatistic statistic, int position, int value) {
		this.uuid = uuid;
		this.statistic = statistic;
		this.position = position;
		this.value = value;
	}
}