package dev.wiji.pixelparty.playerdata;

import dev.wiji.pixelparty.enums.Group;
import dev.wiji.pixelparty.enums.LeaderboardStatistic;
import dev.wiji.pixelparty.enums.LeaderboardType;
import dev.wiji.pixelparty.misc.Pair;
import dev.wiji.pixelparty.sql.*;
import org.bukkit.entity.Player;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PixelPlayer {
	public static List<PixelPlayer> pixelPlayers = new ArrayList<>();

	public UUID uuid;
	public Group userGroup = Group.DEFAULT;
	public LeaderboardStatistic currentStatistic = LeaderboardStatistic.NORMAL_WINS;
	public LeaderboardType currentType = LeaderboardType.LIFETIME;

	public Integer volume = 50;
	public Boolean pausedMusic = false;

	public Boolean woolFloor = false;

	public transient LeaderboardData[] leaderboardData = new LeaderboardData[LeaderboardType.values().length];

	public transient List<Pair<Integer, Integer>> loadedChunks = new ArrayList<>();

	public PixelPlayer(Player player) {
		this(player.getUniqueId());
	}
	public PixelPlayer(UUID uuid) {
		this.uuid = uuid;

		pixelPlayers.add(this);

		for(LeaderboardType type : LeaderboardType.values()) {
			leaderboardData[type.ordinal()] = new LeaderboardData(this, type);
		}

		SQLTable table = TableManager.getTable("PlayerData");
		if(table == null) throw new RuntimeException("PlayerData table failed to register!");

		ResultSet rs = table.selectRow(new Constraint("uuid", uuid.toString()));

		try {
			if(!rs.next()) return;
		} catch(SQLException e) { throw new RuntimeException(e); }

		for(Field field : getClass().getDeclaredFields()) {
			if(Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) continue;

			Class<?> type = field.getType();
			String name = field.getName();
			boolean toDeserialize = false;

			Method method;
			TableStructure.Clazz clazz;

			try {
				clazz = TableStructure.Clazz.valueOf(type.getSimpleName());
			} catch(IllegalArgumentException e) {
				clazz = TableStructure.Clazz.String;
				toDeserialize = true;
			}

			try {
				method = ResultSet.class.getMethod("get" + (clazz.name().equals("Integer") ? "Int" : clazz.name()), String.class);
			} catch(NoSuchMethodException e) { throw new RuntimeException(e); }

			try {
				Object value = method.invoke(rs, name);
				if(toDeserialize) {
					Method fromString = type.getMethod("fromString", String.class);
					value = fromString.invoke(null, (String) value);
				}
				field.set(this, value);
			} catch(Exception e) { throw new RuntimeException(e); }

		}

		try {
			rs.close();
		} catch(SQLException e) { throw new RuntimeException(e); }
	}

	public void save() {
		for(LeaderboardData data : leaderboardData) {
			data.saveData();
		}

		SQLTable table = TableManager.getTable("PlayerData");
		if(table == null) throw new RuntimeException("PlayerData table failed to register!");

		List<QueryStorage> queries = new ArrayList<>();
		queries.add(new Constraint("uuid", uuid.toString()));

		for(Field field : getClass().getDeclaredFields()) {
			if(Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) continue;
			if(field.getName().equals("uuid")) continue;

			Class<?> type = field.getType();
			Object value;

			try {
				value = field.get(this);
			} catch(IllegalAccessException ex) { throw new RuntimeException(ex); }

			try {
				TableStructure.Clazz.valueOf(type.getSimpleName());
			} catch(IllegalArgumentException e) {
				if(!Serializable.class.isAssignableFrom(type)) throw new RuntimeException("Field " + field.getName() + " is not serializable");
				value = value.toString();
			}

			queries.add(new Value(field.getName(), value));
		}

		table.updateRow(queries.toArray(new QueryStorage[0]));
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

	public Group getGroup() {
		return userGroup;
	}

	public static PixelPlayer getPixelPlayer(Player player) {
		return getPixelPlayer(player.getUniqueId());
	}

	public static PixelPlayer getPixelPlayer(UUID uuid) {
		for(PixelPlayer pixelPlayer : pixelPlayers) {
			if(pixelPlayer.uuid.equals(uuid)) {
				return pixelPlayer;
			}
		}

		return new PixelPlayer(uuid);
	}
}
