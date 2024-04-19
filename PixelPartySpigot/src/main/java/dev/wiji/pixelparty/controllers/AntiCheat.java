package dev.wiji.pixelparty.controllers;

import dev.wiji.pixelparty.PixelParty;
import dev.wiji.pixelparty.enums.ServerType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class AntiCheat implements Listener {

	public AntiCheat() {
		if(PixelParty.serverType == ServerType.LOBBY) return;
		GameManager gameManager = PixelParty.gameManager;

		new BukkitRunnable() {
			@Override
			public void run() {
				if(gameManager.gameState != GameManager.GameState.INGAME) return;

				Bukkit.getOnlinePlayers().forEach(player -> checkPlayer(player));
			}
		}.runTaskTimer(PixelParty.INSTANCE, 0, 5);
	}

	public void checkPlayer(Player player) {
//		if(player.isFlying()) return;

		Location location = player.getLocation().clone();
		location.setY(0);
		FloorManager manager = PixelParty.gameManager.floorManager;

		if(manager.isOnFloor(location) || manager.buffer) return;

		//Change to simply be a y level check and change the death mechanic to kill everyone under the floor

		if(player.getVelocity().getY() < 0.1) {
			Bukkit.broadcastMessage("Kicking " + player.getName() + " for cheating");
		}
	}

	//Criteria for kick:
	/*
	1. Player is above area without floor beneath
	2. Player has no/slow downward velocity
	3. Player is not anti-cheat exempt

	 */
}
