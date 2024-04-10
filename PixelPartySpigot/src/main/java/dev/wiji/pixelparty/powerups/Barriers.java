package dev.wiji.pixelparty.powerups;

import dev.wiji.pixelparty.objects.PowerUp;
import dev.wiji.pixelparty.util.Misc;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Barriers extends PowerUp {

	public Barriers() {
		super("Barriers");
	}

	public List<Location> barrierLocations = new ArrayList<>();

	@Override
	public void onActivate(Player player, Location location) {
		Random random = new Random();
		int barrierCount = random.nextInt(4) + 1;

		for(int i = 0; i < barrierCount; i++) {
			Location barrierLocation = getRandomLocation(location.getWorld());
			spawnBarrier(barrierLocation);
			barrierLocations.add(barrierLocation);
		}

		if(barrierCount == 1) Bukkit.broadcastMessage(Misc.color("&cA glass barrier has spawned on the dance floor!"));
		else Bukkit.broadcastMessage(Misc.color("&cSeveral barriers have spawned on the dance floor!"));

	}

	@Override
	public void onRoundEnd() {
		for(Location barrierLocation : barrierLocations) {
			//TODO: This is not at the end of the round, but longer
			removeBarrier(barrierLocation);
		}
		barrierLocations.clear();
	}

	public Location getRandomLocation(World world) {
		Random random = new Random();

		Location proposed = null;
		int players = 0;
		boolean nearBarrier = false;

		while(players > 0 || proposed == null || nearBarrier) {
			int x = random.nextInt(60) - 30;
			int z = random.nextInt(60) - 30;
			proposed = new Location(world, x, 1, z);
			players = world.getNearbyEntities(proposed, 3, 3, 3).size();

			nearBarrier = false;
			for(Location barrierLocation : barrierLocations) {
				if(barrierLocation.distance(proposed) < 5) {
					nearBarrier = true;
					break;
				}
			}
		}

		return proposed;
	}

	public void removeBarrier(Location location) {
		spawnBarrier(location, true);
		//TODO: Play barrier break sound
	}

	public void spawnBarrier(Location location) {
		spawnBarrier(location, false);
	}

	public void spawnBarrier(Location location, boolean remove) {
		Location start = location.clone().add(-1, 0, -1);

		for(int x = 0; x < 3; x++) {
			for(int z = 0; z < 3; z++) {
				for(int y = 0; y < 3; y++) {
					Location loc = start.clone().add(x, y, z);
					loc.getBlock().setType(remove ? Material.AIR : Material.GLASS);
				}
			}
		}

	}


}
