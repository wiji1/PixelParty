package dev.wiji.pixelparty.powerups;

import dev.wiji.pixelparty.PixelParty;
import dev.wiji.pixelparty.controllers.FloorManager;
import dev.wiji.pixelparty.objects.PixelVillager;
import dev.wiji.pixelparty.objects.PowerUp;
import dev.wiji.pixelparty.util.Misc;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Villagers extends PowerUp {
	List<PixelVillager> villagers = new ArrayList<>();
	FloorManager manager = PixelParty.gameManager.floorManager;

	public Villagers() {
		super("Villagers");

		Misc.registerEntity("PixelVillager", 120, PixelVillager.class);
	}

	@Override
	public void onActivate(Player player, Location location) {
		for(int i = 0; i < 50; i++) {
			Location spawnLoc = getRandomLocation(player.getLocation());
			World nmsWorld = ((CraftWorld) player.getWorld()).getHandle();
			PixelVillager villager = new PixelVillager(nmsWorld);

			villager.setLocation(spawnLoc.getX(), spawnLoc.getY(), spawnLoc.getZ(), spawnLoc.getYaw(), spawnLoc.getPitch());
			nmsWorld.addEntity(villager);

			villagers.add(villager);
		}

		Bukkit.broadcastMessage(Misc.color("&cA crowd of villagers has appeared!"));
	}

	@Override
	public void onNextRoundStart() {
		for(PixelVillager villager : villagers) {
			villager.remove();
		}

		villagers.clear();
	}

	public Location getRandomLocation(Location playerLoc) {
		Location location = null;

		while(location == null || !manager.isOnFloor(location)) {
			location = playerLoc.clone().add(Math.random() * 20 - 10, 0, Math.random() * 20 - 10);
			location.setY(0);
		}

		location.setY(1);

		return location;
	}
}
