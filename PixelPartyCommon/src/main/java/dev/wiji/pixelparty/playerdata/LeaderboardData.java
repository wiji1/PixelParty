package dev.wiji.pixelparty.playerdata;

import dev.wiji.pixelparty.enums.LeaderboardStatistic;
import dev.wiji.pixelparty.enums.LeaderboardType;
import dev.wiji.pixelparty.sql.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LeaderboardData {

	private final LeaderboardType leaderboardType;
	private final PixelPlayer pixelPlayer;

	private final List<LeaderboardPosition> leaderboardPositions = new ArrayList<>();

	public LeaderboardData(PixelPlayer player, LeaderboardType type) {
		this.pixelPlayer = player;
		this.leaderboardType = type;

		fetchData();
	}

	public int getPosition(LeaderboardStatistic statistic) {
		for(LeaderboardPosition leaderboardPosition : leaderboardPositions) {
			if(leaderboardPosition.statistic == statistic) {
				return leaderboardPosition.position;
			}
		}
		return -1;
	}

	public int getValue(LeaderboardStatistic statistic) {
		for(LeaderboardPosition leaderboardPosition : leaderboardPositions) {
			if(leaderboardPosition.statistic == statistic) {
				return leaderboardPosition.value;
			}
		}
		return 0;
	}

	public void setValue(LeaderboardStatistic statistic, int value) {
		for(LeaderboardPosition leaderboardPosition : leaderboardPositions) {
			if(leaderboardPosition.statistic == statistic) {
				leaderboardPosition.value = value;
				return;
			}
		}
	}

	public void clear() {
		leaderboardPositions.clear();
		saveData();
	}

	public void fetchData() {

		SQLTable table = TableManager.getTable(leaderboardType);
		if(table == null) throw new RuntimeException("SQL Table failed to register!");

		int[] statistics = new int[LeaderboardStatistic.values().length];
		int[] positions = new int[LeaderboardStatistic.values().length];

		try {
			ResultSet rs = table.selectRow(new Constraint("uuid", pixelPlayer.uuid.toString()));
			boolean exists = rs.next();

			for(int i = 0; i < LeaderboardStatistic.values().length; i++) {
				String name = LeaderboardStatistic.values()[i].sqlName;

				statistics[i] = exists ? rs.getInt(name) : 0;
				positions[i] = exists ? table.getPosition(new Field(name), new Constraint("uuid",
						pixelPlayer.uuid.toString())) : -1;
			}

			rs.close();
		} catch(SQLException e) { throw new RuntimeException(e); }

		for(LeaderboardStatistic stat : LeaderboardStatistic.values()) {
			leaderboardPositions.add(new LeaderboardPosition(pixelPlayer.uuid, stat, positions[stat.ordinal()], statistics[stat.ordinal()]));
		}
	}

	public void saveData() {
		SQLTable table = TableManager.getTable(leaderboardType);
		if(table == null) throw new RuntimeException("SQL Table failed to register!");

		List<QueryStorage> queryStorage = new ArrayList<>();
		queryStorage.add(new Constraint("uuid", pixelPlayer.uuid.toString()));

		for(LeaderboardPosition leaderboardPosition : leaderboardPositions) {
			queryStorage.add(new Value(leaderboardPosition.statistic.sqlName, leaderboardPosition.value));
		}

		table.updateRow(queryStorage.toArray(new QueryStorage[0]));
	}
}
