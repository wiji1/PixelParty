package dev.wiji.pixelparty.controllers;

import dev.wiji.pixelparty.objects.PixelBossBar;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;

public class BossBarManager implements Listener {
	public static PixelBossBar bossBar;

	public BossBarManager() {
		bossBar = new PixelBossBar(ChatColor.translateAlternateColorCodes('&', "&eWaiting for players"), 1);
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		updatePlayers();
	}

	@EventHandler
	public void onQuit(PlayerJoinEvent event) {
		updatePlayers();
	}

	public void updateBar(String text) {
		bossBar.updateText(ChatColor.translateAlternateColorCodes('&', text));
		bossBar.updateProgress(1);

		for(Player player : Bukkit.getOnlinePlayers()) {
			ActionBarManager.sendActionBarMessage(player, text);
		}
	}

	public void updatePlayers() {
		bossBar.updatePlayers(new ArrayList<>(Bukkit.getOnlinePlayers()));
	}
}
