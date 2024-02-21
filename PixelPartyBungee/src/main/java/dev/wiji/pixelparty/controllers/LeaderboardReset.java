package dev.wiji.pixelparty.controllers;

import dev.wiji.pixelparty.BungeeMain;
import dev.wiji.pixelparty.enums.LeaderboardType;
import dev.wiji.pixelparty.messaging.PluginMessage;
import dev.wiji.pixelparty.objects.GameServer;
import dev.wiji.pixelparty.sql.SQLTable;
import dev.wiji.pixelparty.sql.TableManager;

import java.time.*;

public class LeaderboardReset {

	public LeaderboardReset() {
		new Thread(() -> {
			while(true) {
				checkTime();

				try {
					Thread.sleep(1000 * 60);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	public void checkTime() {
		long weeklyReset = BungeeMain.INSTANCE.getConfiguration().getLong("next-weekly-reset");
		long currentTime = System.currentTimeMillis();

		if(weeklyReset == -1 || currentTime > weeklyReset) {
			BungeeMain.INSTANCE.getConfiguration().set("next-weekly-reset", getNextWeekStartTimestamp());
			BungeeMain.INSTANCE.saveConfig();

			SQLTable leaderboard = TableManager.getTable("LeaderboardDataWeekly");
			if(leaderboard != null) leaderboard.clear();

			for(GameServer gameServer : ServerManager.gameServers) {
				PluginMessage message = new PluginMessage();
				message.writeString("RESET LEADERBOARD");
				message.writeString(LeaderboardType.WEEKLY.toString());
				message.writeLong(getNextWeekStartTimestamp());
				message.setIntendedServer(gameServer.getName()).send();
			}
		}

		long monthlyReset = BungeeMain.INSTANCE.getConfiguration().getLong("next-monthly-reset");

		if(monthlyReset == -1 || currentTime > monthlyReset) {
			BungeeMain.INSTANCE.getConfiguration().set("next-monthly-reset", getNextMonthStartTimestamp());
			BungeeMain.INSTANCE.saveConfig();

			SQLTable leaderboard = TableManager.getTable("LeaderboardDataMonthly");
			if(leaderboard != null) leaderboard.clear();

			for(GameServer gameServer : ServerManager.gameServers) {
				PluginMessage message = new PluginMessage();
				message.writeString("RESET LEADERBOARD");
				message.writeString(LeaderboardType.MONTHLY.toString());
				message.writeLong(getNextMonthStartTimestamp());
				message.setIntendedServer(gameServer.getName()).send();
			}
		}
	}

	public static void sendNextResetTime(GameServer server) {
		new Thread(() -> {
			try {
				Thread.sleep(1000 * 5);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			long lastWeeklyReset = BungeeMain.INSTANCE.getConfiguration().getLong("next-weekly-reset");
			long lastMonthlyReset = BungeeMain.INSTANCE.getConfiguration().getLong("next-monthly-reset");

			PluginMessage message = new PluginMessage();

			message.writeString("NEXT RESET");
			message.writeLong(getNextWeekStartTimestamp());
			message.writeLong(getNextMonthStartTimestamp());
			message.setIntendedServer(server.getName()).send();

		}).start();
	}

	public static long getNextMonthStartTimestamp() {
		LocalDate currentDate = LocalDate.now();
		LocalDate firstDayOfNextMonth = LocalDate.of(currentDate.getYear(), currentDate.getMonth().plus(1), 1);
		LocalDateTime startOfNextMonth = LocalDateTime.of(firstDayOfNextMonth, LocalTime.MIDNIGHT);
		return startOfNextMonth.toEpochSecond(ZoneOffset.UTC) * 1000;
	}

	public static long getNextWeekStartTimestamp() {
		LocalDate currentDate = LocalDate.now();
		LocalDate nextMonday = currentDate.with(DayOfWeek.MONDAY).plusWeeks(1);
		LocalDateTime startOfNextWeek = LocalDateTime.of(nextMonday, LocalTime.MIDNIGHT);

		return startOfNextWeek.toEpochSecond(ZoneOffset.UTC) * 1000;
	}
}
