package dev.wiji.pixelparty.sql;

import dev.wiji.pixelparty.PixelPartyPlugin;
import dev.wiji.pixelparty.enums.LeaderboardStatistic;
import dev.wiji.pixelparty.enums.LeaderboardType;
import dev.wiji.pixelparty.playerdata.PixelPlayer;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class TableManager {
	public static PixelPartyPlugin plugin;
	private static final List<SQLTable> tables = new ArrayList<>();

	public static void registerTables(PixelPartyPlugin instance) {
		plugin = instance;

		new SQLTable(ConnectionInfo.PIXEL_PARTY, "PlayerCache",
			new TableStructure(
					new TableColumn(String.class, "uuid", false, true),
					new TableColumn(String.class, "name", false, false),
					new TableColumn(String.class, "user_group", false, false),
					new TableColumn(String.class, "skin_texture", false, false, 1000),
					new TableColumn(String.class, "skin_signature", false, false, 1000)
			)

		);

		for(LeaderboardType value : LeaderboardType.values()) {
			List<TableColumn> colums = new ArrayList<>();
			colums.add(new TableColumn(String.class, "uuid", false, true));

			for(LeaderboardStatistic stat : LeaderboardStatistic.values()) {
				if(value != LeaderboardType.LIFETIME && stat.lifetimeOnly) continue;
				colums.add(new TableColumn(Integer.class, stat.sqlName, false, false));
			}

			new SQLTable(ConnectionInfo.PIXEL_PARTY, "LeaderboardData" + value.displayName,
					new TableStructure(colums.toArray(new TableColumn[0]))
			);
		}


		List<TableColumn> pixelPlayerFields = new ArrayList<>();

		int validFields = 0;

		for(Field field : PixelPlayer.class.getDeclaredFields()) {
			if(Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) continue;

			Class<?> type = field.getType();

			try {
				TableStructure.Clazz.valueOf(type.getSimpleName());
			} catch(IllegalArgumentException e) {
				type = String.class;
			}

			pixelPlayerFields.add(new TableColumn(type, field.getName(), false, validFields == 0));
			validFields++;
		}

		new SQLTable(ConnectionInfo.PIXEL_PARTY, "PlayerData",
			new TableStructure(pixelPlayerFields.toArray(new TableColumn[0]))
		);
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
		return getTable("LeaderboardData" + type.displayName);
	}
}
