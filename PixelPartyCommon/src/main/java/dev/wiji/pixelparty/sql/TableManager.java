package dev.wiji.pixelparty.sql;

import dev.wiji.pixelparty.PixelPartyPlugin;

import java.util.ArrayList;
import java.util.List;

public class TableManager {
	public static PixelPartyPlugin plugin;
	private static final List<SQLTable> tables = new ArrayList<>();

	public static void registerTables(PixelPartyPlugin instance) {
		plugin = instance;

		for(TableType value : TableType.values()) {
			new SQLTable(ConnectionInfo.LEADERBOARD_DATA, "LeaderboardData" + value.name(),
					new TableStructure(
							new TableColumn(String.class, "uuid", false, true),
							new TableColumn(Integer.class, "normal_wins", false, false),
							new TableColumn(Integer.class, "hyper_wins", false, false)
					));
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

	public static enum TableType {
		WEEKLY("Weekly"),
		MONTHLY("Monthly"),
		LIFETIME("Lifetime");

		final String name;

		TableType(String name) {
			this.name = name;
		}
	}
}
