package dev.wiji.pixelparty.objects;

import dev.wiji.pixelparty.PixelParty;
import dev.wiji.pixelparty.util.MetaDataUtil;
import dev.wiji.pixelparty.util.Misc;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class PowerUp implements Listener {

	public static List<PowerUp> powerUps = new ArrayList<>();

	public abstract void onActivate(Player player, Location location);

	public void onRoundEnd() {
		//Optional Override for select PowerUps
	}

	public void onNextRoundStart() {
		//Optional Override for select PowerUps
	}

	public static void createRandomPowerUp() {
		new PowerUpPickup();
	}

	public static void registerPowerUp(PowerUp powerUp) {
		Bukkit.getPluginManager().registerEvents(powerUp, PixelParty.INSTANCE);
		powerUps.add(powerUp);
	}

	public static class PowerUpPickup {
		public Location location;

		public PowerUpPickup() {
			Misc.broadcast("&3\u2726 &bA &d&lPOWER-UP &bhas spawned! &7Punch the beacon to collect it!");

			Random random = new Random();
			int x = random.nextInt(64) - 32;
			int z = random.nextInt(64) - 32;

			this.location = new Location(Bukkit.getWorld("world"), x, 1, z);
			location.getBlock().setType(Material.BEACON);

			PixelParty.gameManager.powerUps.add(this);
		}

		public void onInteract(Player player) {
			//Get random powerup
			PowerUp powerUp = powerUps.get(new Random().nextInt(powerUps.size()));
			powerUp.onActivate(player, location);

			String playerName = MetaDataUtil.getNameAndRank(player);
			Misc.broadcast("&3\u2726 " + playerName + " &bcollected the &d&lPOWER-UP&b!");
			remove();
		}

		public void remove() {
			location.getBlock().setType(Material.AIR);
			PixelParty.gameManager.powerUps.remove(this);
		}
	}

}
