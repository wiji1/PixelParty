package dev.wiji.pixelparty.powerups;

import dev.wiji.pixelparty.objects.PowerUp;
import dev.wiji.pixelparty.util.Misc;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Random;

public class Teleport extends PowerUp {

	public Teleport() {
		super("Teleport");
	}

	@Override
	public void onActivate(Player player, Location location) {
		Random random = new Random();
		int randX = random.nextInt(64) - 32;
		int randZ = random.nextInt(64) - 32;

		Location teleport = new Location(location.getWorld(), randX, 1, randZ);
		teleport.setPitch(player.getLocation().getPitch());
		teleport.setYaw(player.getLocation().getYaw());

		player.teleport(teleport);
		//TODO: Play teleport sound

		player.sendMessage(Misc.color("&cYou've been teleported to a random spot on the dance floor!"));
	}
}
