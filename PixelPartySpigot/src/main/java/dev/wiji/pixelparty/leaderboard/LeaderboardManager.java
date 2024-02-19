package dev.wiji.pixelparty.leaderboard;

import dev.wiji.pixelparty.enums.LeaderboardStatistic;
import dev.wiji.pixelparty.enums.LeaderboardType;
import dev.wiji.pixelparty.holograms.Hologram;
import dev.wiji.pixelparty.holograms.RefreshMode;
import dev.wiji.pixelparty.holograms.ViewMode;
import dev.wiji.pixelparty.playerdata.LeaderboardPosition;
import dev.wiji.pixelparty.sql.Field;
import dev.wiji.pixelparty.sql.SQLTable;
import dev.wiji.pixelparty.sql.TableManager;
import dev.wiji.pixelparty.util.Misc;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LeaderboardManager implements Listener {

	public static final Location HOLOGRAM_LOCATION = new Location(Bukkit.getWorld("lobby"), 0, 68, 32);
	public static LeaderboardStatistic currentStatistic;

	public LeaderboardManager() {
		currentStatistic = LeaderboardStatistic.NORMAL_WINS;

		new Hologram(HOLOGRAM_LOCATION, ViewMode.ALL, RefreshMode.MANUAL) {

			@Override
			public List<String> getStrings(Player player) {
				List<String> strings = new ArrayList<>();

				strings.add("&b" + currentStatistic.name);
				for(LeaderboardPosition leaderboardPosition : getLeaderboardPositions(currentStatistic, LeaderboardType.LIFETIME)) {
					if(leaderboardPosition == null) {
						strings.add("&c&l" + "EMPTY");
						continue;
					}

					OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(leaderboardPosition.uuid);
					String nameText = Misc.getNameAndRank(offlinePlayer);
					strings.add("&7" + leaderboardPosition.position + ". " + nameText + " &7 - &e" + leaderboardPosition.value);
				}

				return strings;
			}
		};
	}

	public LeaderboardPosition[] getLeaderboardPositions(LeaderboardStatistic stat, LeaderboardType type) {
		SQLTable table = TableManager.getTable(type);

		if(table == null) throw new RuntimeException("SQL Table failed to register!");

		ResultSet rs = table.getTop(10, new Field(stat.sqlName));
		LeaderboardPosition[] positions = new LeaderboardPosition[10];

		try {
			int i = 0;
			while(rs.next()) {
				UUID uuid = UUID.fromString(rs.getString("uuid"));

				positions[i] = new LeaderboardPosition(uuid, stat, i + 1, rs.getInt(stat.sqlName));
				i++;
			}

			rs.close();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}

		return positions;
	}
}
