package dev.wiji.pixelparty.leaderboard;

import dev.wiji.pixelparty.enums.LeaderboardStatistic;
import dev.wiji.pixelparty.enums.LeaderboardType;
import dev.wiji.pixelparty.holograms.Hologram;
import dev.wiji.pixelparty.holograms.RefreshMode;
import dev.wiji.pixelparty.holograms.ViewMode;
import dev.wiji.pixelparty.playerdata.LeaderboardPosition;
import dev.wiji.pixelparty.playerdata.PixelPlayer;
import dev.wiji.pixelparty.util.Misc;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Leaderboard {

	public org.bukkit.Location location;
	public LeaderboardStatistic defaultStatistic;
	public LeaderboardType defaultType;

	public Hologram hologram;

	public List<UUID> overriddenPlayers = new ArrayList<>();

	public Leaderboard(Location location) {
		this(location, null, null);
	}

	public Leaderboard(Location location, LeaderboardStatistic statistic, LeaderboardType type) {
		this.location = location;
		this.defaultStatistic = statistic;
		this.defaultType = type;

		this.hologram = new Hologram(location, ViewMode.ALL, RefreshMode.MANUAL) {

			@Override
			public List<String> getStrings(Player player) {
				List<String> strings = new ArrayList<>();
				PixelPlayer pixelPlayer = PixelPlayer.getPixelPlayer(player);

				LeaderboardStatistic statistic = pixelPlayer.currentStatistic;
				LeaderboardType type = pixelPlayer.currentType;

				if(defaultStatistic != null && !overriddenPlayers.contains(player.getUniqueId())) statistic = defaultStatistic;
				if(defaultType != null && !overriddenPlayers.contains(player.getUniqueId())) type = defaultType;

				strings.add("&b&n" + statistic.name);
				strings.add("&7&o" + type.displayName);
				for(LeaderboardPosition leaderboardPosition : LeaderboardManager.getLeaderboardPositions(statistic, type)) {
					if(leaderboardPosition == null) {
						strings.add("&c&l" + "EMPTY");
						continue;
					}

					String nameText = Misc.getNameAndRank(leaderboardPosition.uuid);
					strings.add("&7" + leaderboardPosition.position + ". " + nameText + " &7 - &e" + leaderboardPosition.value);
				}

				int position = pixelPlayer.getLeaderboardPosition(type, statistic);
				if(position > 10) {
					int value = pixelPlayer.getLeaderboardStat(type, statistic);
					String nameText = Misc.getNameAndRank(player);
					strings.add("&7" + position + ". " + nameText + " &7 - &e" + value);
				}

				strings.add(LeaderboardManager.getResetString(type));
				strings.add("&6Click to change settings!");

				return strings;
			}
		};

		hologram.setClickable(true);
	}


}
