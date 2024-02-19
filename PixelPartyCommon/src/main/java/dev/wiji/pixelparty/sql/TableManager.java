package dev.wiji.pixelparty.sql;

import dev.wiji.pixelparty.PixelPartyPlugin;
import dev.wiji.pixelparty.enums.LeaderboardType;

import java.util.ArrayList;
import java.util.List;

public class TableManager {
	public static PixelPartyPlugin plugin;
	private static final List<SQLTable> tables = new ArrayList<>();

	public static void registerTables(PixelPartyPlugin instance) {
		plugin = instance;

		for(LeaderboardType value : LeaderboardType.values()) {
			new SQLTable(ConnectionInfo.LEADERBOARD_DATA, "LeaderboardData" + value.displayName,
					new TableStructure(
							new TableColumn(String.class, "uuid", false, true),
							new TableColumn(Integer.class, "normal_wins", false, false),
							new TableColumn(Integer.class, "hyper_wins", false, false)
					)
			);
		}
	}

	protected static void registerTable(SQLTable table) {
		tables.add(table);
	}

	public static SQLTable getTable(String tableName) {
		for(SQLTable table : tables) {
			if(table.tableName.equals(tableName)) return table;
		}
		return null;
	}

	public static SQLTable getTable(LeaderboardType type) {
		return getTable("LeaderboardData" + type.name());
	}
}
