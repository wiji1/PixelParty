package dev.wiji.pixelparty.controllers;

import dev.wiji.pixelparty.objects.PixelPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerDataManager implements Listener {

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerJoin(PlayerJoinEvent event) {

		PixelPlayer.getPixelPlayer(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onLeave(PlayerQuitEvent event) {
		PixelPlayer pixelPlayer = PixelPlayer.getPixelPlayer(event.getPlayer());
		PixelPlayer.pixelPlayers.remove(pixelPlayer);
	}
}
