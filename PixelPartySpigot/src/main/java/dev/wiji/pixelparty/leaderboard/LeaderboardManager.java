package dev.wiji.pixelparty.leaderboard;

import dev.wiji.pixelparty.PixelParty;
import dev.wiji.pixelparty.enums.LeaderboardStatistic;
import dev.wiji.pixelparty.enums.LeaderboardType;
import dev.wiji.pixelparty.enums.ServerType;
import dev.wiji.pixelparty.events.MessageEvent;
import dev.wiji.pixelparty.events.PacketReceiveEvent;
import dev.wiji.pixelparty.holograms.TextLine;
import dev.wiji.pixelparty.inventory.LeaderboardGUI;
import dev.wiji.pixelparty.messaging.PluginMessage;
import dev.wiji.pixelparty.playerdata.LeaderboardPosition;
import dev.wiji.pixelparty.playerdata.PixelPlayer;
import dev.wiji.pixelparty.sql.Field;
import dev.wiji.pixelparty.sql.SQLTable;
import dev.wiji.pixelparty.sql.TableManager;
import dev.wiji.pixelparty.util.Misc;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class LeaderboardManager implements Listener {

	public static List<Leaderboard> leaderboards = new ArrayList<>();

	public static final Location LOBBY_LEADERBOARD = new Location(Bukkit.getWorld("lobby"), 0, 66.5, 25);
	public static final Location GAME_LEADERBOARD = new Location(Bukkit.getWorld("world"), -53, 2.5, 0.5);

	public static long weeklyResetTime = -1;
	public static long monthlyResetTime = -1;


	public LeaderboardManager() {
		new BukkitRunnable() {
			@Override
			public void run() {
				updateLeaderboards();
			}
		}.runTaskTimer(PixelParty.INSTANCE, 20 * 60, 20 * 60);

		ServerType serverType = PixelParty.serverType;
		Location spawnLoc = serverType == ServerType.LOBBY ? LOBBY_LEADERBOARD : GAME_LEADERBOARD;
		LeaderboardType type = serverType == ServerType.LOBBY ? null : LeaderboardType.LIFETIME;

		LeaderboardStatistic stat;


		switch(serverType) {
			case LOBBY:
				stat = null;
				break;
			case NORMAL:
				stat = LeaderboardStatistic.NORMAL_WINS;
				break;
			case HYPER:
				stat = LeaderboardStatistic.HYPER_WINS;
				break;
			default:
				stat = null;
				type = null;
		}

		registerLeaderboard(new Leaderboard(spawnLoc, stat, type));
	}


	public static void registerLeaderboard(Leaderboard leaderboard) {
		leaderboards.add(leaderboard);
	}
	public static void updateLeaderboards() {
		for(Leaderboard leaderboard : leaderboards) {
			leaderboard.hologram.updateHologram();
		}
	}

	public static String getResetString(LeaderboardType type) {
		if(type == LeaderboardType.LIFETIME) return Misc.color("&7Never Resets.");

		long time = type == LeaderboardType.WEEKLY ? weeklyResetTime : monthlyResetTime;
		if(time == -1) return Misc.color("&7Unknown");

		long timeUntil = time - System.currentTimeMillis();

		int days = (int) (timeUntil / 1000 / 60 / 60 / 24);
		int hours = (int) (timeUntil / 1000 / 60 / 60 % 24);
		int minutes = (int) (timeUntil / 1000 / 60 % 60);

		StringBuilder sb = new StringBuilder();
		sb.append("&7Resets in &e");
		if(days > 0) sb.append(days).append("d ");
		if(hours > 0) sb.append(hours).append("h ");
		if(minutes > 0) sb.append(minutes).append("m");

		return Misc.color(sb.toString());
	}

	@EventHandler
	public void onPacketReceived(PacketReceiveEvent event) {
		if(!Objects.equals(event.getPacketType().name(), "USE_ENTITY")) return;

		try {
			java.lang.reflect.Field entityID = event.getPacket().getClass().getDeclaredField("a");
			entityID.setAccessible(true);

			for(Leaderboard leaderboard : leaderboards) {
				for(TextLine textLine : leaderboard.hologram.getTextLines()) {
					if(textLine.getEntityId() == (int) entityID.get(event.getPacket())) {
						LeaderboardGUI gui = new LeaderboardGUI(event.getPlayer(), leaderboard);
						gui.open();
						return;
					}
				}
			}

		} catch(NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@EventHandler
	public void onMessage(MessageEvent event) {

		PluginMessage message = event.getMessage();
		List<String> strings = message.getStrings();
		List<Long> longs = message.getLongs();

		if(strings.size() < 1) return;

		if(strings.get(0).equals("LEADERBOARD")) {
			LeaderboardType type = LeaderboardType.valueOf(strings.get(1));

			for(PixelPlayer pixelPlayer : PixelPlayer.pixelPlayers) {
				pixelPlayer.leaderboardData[type.ordinal()].clear();
			}

			if(type == LeaderboardType.WEEKLY) weeklyResetTime = longs.get(0);
			else monthlyResetTime = longs.get(0);

			updateLeaderboards();
		}

		if(strings.get(0).equals("NEXT RESET")) {
			weeklyResetTime = longs.get(0);
			monthlyResetTime = longs.get(1);
		}
	}

	public static LeaderboardPosition[] getLeaderboardPositions(LeaderboardStatistic stat, LeaderboardType type) {
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
