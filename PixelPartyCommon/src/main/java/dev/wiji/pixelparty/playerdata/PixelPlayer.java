package dev.wiji.pixelparty.playerdata;

import dev.wiji.pixelparty.enums.LeaderboardStatistic;
import dev.wiji.pixelparty.enums.LeaderboardType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PixelPlayer {
	public static List<PixelPlayer> pixelPlayers = new ArrayList<>();

	public UUID uuid;

	public int volume = 50;
	public boolean pausedMusic = false;

	LeaderboardData[] leaderboardData = new LeaderboardData[LeaderboardType.values().length];

	public PixelPlayer(Player player) {
		this.uuid = player.getUniqueId();

		pixelPlayers.add(this);

		for(LeaderboardType type : LeaderboardType.values()) {
			leaderboardData[type.ordinal()] = new LeaderboardData(this, type);
		}
	}

	public void save() {
		for(LeaderboardData data : leaderboardData) {
			data.saveData();
		}
	}

	public void addStat(LeaderboardStatistic stat) {
		for(LeaderboardData leaderboardDatum : leaderboardData) {
			leaderboardDatum.setValue(stat, leaderboardDatum.getValue(stat) + 1);
		}
	}

	public LeaderboardData getLeaderboardData(LeaderboardType type) {
		return leaderboardData[type.ordinal()];
	}

	public int getLeaderboardStat(LeaderboardType type, LeaderboardStatistic stat) {
		return leaderboardData[type.ordinal()].getValue(stat);
	}

	public int getLeaderboardPosition(LeaderboardType type, LeaderboardStatistic stat) {
		return leaderboardData[type.ordinal()].getPosition(stat);
	}

	public static PixelPlayer getPixelPlayer(Player player) {
		for(PixelPlayer pixelPlayer : pixelPlayers) {
			if(pixelPlayer.uuid.equals(player.getUniqueId())) {
				return pixelPlayer;
			}
		}

		return new PixelPlayer(player);
	}
}
