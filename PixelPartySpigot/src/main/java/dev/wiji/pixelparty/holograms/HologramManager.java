package dev.wiji.pixelparty.holograms;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;

import java.util.ArrayList;
import java.util.List;

public class HologramManager implements Listener {

	public static List<Hologram> holograms = new ArrayList<>();

	public static void registerHologram(Hologram hologram) {
		holograms.add(hologram);

		if(hologram.viewMode != ViewMode.ALL) return;
		hologram.setPermittedViewers(new ArrayList<>(Bukkit.getOnlinePlayers()));
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		for(Hologram hologram : holograms) {
			if(hologram.viewMode != ViewMode.ALL) continue;

			hologram.addPermittedViewer(event.getPlayer());
		}
	}

	@EventHandler
	public void onLeave(PlayerQuitEvent event) {
		for(Hologram hologram : holograms) {
			if(hologram.viewMode != ViewMode.ALL) continue;

			hologram.removePermittedViewer(event.getPlayer());
		}
	}

	@EventHandler
	public void onShutdown(PluginDisableEvent event) {
		for(Player player : Bukkit.getOnlinePlayers()) {
			for(Hologram hologram : holograms) {
				for(TextLine textLine : hologram.textLines) {
					textLine.removeLine(player);
				}
			}
		}
	}

	public HologramManager() {

	}


	public static ChatColor getLeaderboardColor(int i) {
		switch(i) {
			case 3: return ChatColor.GOLD;
			case 2: return ChatColor.WHITE;
			case 1: return ChatColor.YELLOW;
		}
		return ChatColor.GRAY;
	}
}
