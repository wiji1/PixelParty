package dev.wiji.pixelparty.controllers;

import dev.wiji.pixelparty.PixelParty;
import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class AmbienceManager {
	public static final Location CENTER = new Location(Bukkit.getWorld("world"), 0, 1, 0);
	public static final int MAP_RADIUS = 32;
	public static final int PARTICLE_HEIGHT = 2;

	public static final long DAY_TIME = 13000;
	public static final long NIGHT_TIME = 18000;

	public static final int FIREWORK_SPAWN_RADIUS = 35;
	public static final Location FIREWORK_SPAWN_CORNER_1 = new Location(Bukkit.getWorld("world"), -35, 1, -35);


	public static void init() {
		CENTER.getWorld().setGameRuleValue("doDaylightCycle", "false");
		CENTER.getWorld().setTime(DAY_TIME);

		new BukkitRunnable() {
			@Override
			public void run() {
				displayAmbientParticles();
			}
		}.runTaskTimer(PixelParty.INSTANCE, 0, 15);

	}


	public static void displayAmbientParticles() {
		for(Player player : Bukkit.getOnlinePlayers()) {

			for(int i = 0; i < 50; i++) {
				int x = (int) (CENTER.getX() + (Math.random() * MAP_RADIUS * 2) - MAP_RADIUS);
				int z = (int) (CENTER.getZ() + (Math.random() * MAP_RADIUS * 2) - MAP_RADIUS);
				int y = (int) (CENTER.getY() + (Math.random() * PARTICLE_HEIGHT * 2) - PARTICLE_HEIGHT);

				player.spigot().playEffect(new Location(CENTER.getWorld(), x, y, z), Effect.FIREWORKS_SPARK, 0, 0, 0, 0, 0, 0, 1, 250);
			}
		}
	}

	public static void setNight() {
		new BukkitRunnable() {
			final int totalTicks = 200;
			int i = 0;

			@Override
			public void run() {

				if(i == totalTicks) {
					this.cancel();
					return;
				}

				long time = CENTER.getWorld().getTime();
				time += (NIGHT_TIME - DAY_TIME) / totalTicks;
				CENTER.getWorld().setTime(time);

				i++;
			}
		}.runTaskTimer(PixelParty.INSTANCE, 0, 1);
	}

	public static void setDay() {
		new BukkitRunnable() {
			final int totalTicks = 200;
			int i = 0;

			@Override
			public void run() {

				if(i == totalTicks) {
					this.cancel();
					return;
				}

				long time = CENTER.getWorld().getFullTime();
				time -= (NIGHT_TIME - DAY_TIME) / totalTicks;
				CENTER.getWorld().setFullTime(time);

				i++;
			}
		}.runTaskTimer(PixelParty.INSTANCE, 0, 1);
	}

	public static void launchFireworks() {
		List<Location> launchLocations = new ArrayList<>();

		for(int i = 0; i < FIREWORK_SPAWN_RADIUS; i+=5) {
			Location corner1 = new Location(CENTER.getWorld(), -1 * FIREWORK_SPAWN_RADIUS, 1, -1 * FIREWORK_SPAWN_RADIUS);
			Location corner2 = new Location(CENTER.getWorld(), FIREWORK_SPAWN_RADIUS, 1, FIREWORK_SPAWN_RADIUS);
			Location corner3 = new Location(CENTER.getWorld(), -1 * FIREWORK_SPAWN_RADIUS, 1, FIREWORK_SPAWN_RADIUS);
			Location corner4 = new Location(CENTER.getWorld(), FIREWORK_SPAWN_RADIUS, 1, -1 * FIREWORK_SPAWN_RADIUS);

			launchLocations.add(corner1.clone().add(i, 0, 0));
			launchLocations.add(corner1.clone().add(0, 0, i));

			launchLocations.add(corner2.clone().subtract(i, 0, 0));
			launchLocations.add(corner2.clone().subtract(0, 0, i));

			launchLocations.add(corner3.clone().add(i, 0, 0));
			launchLocations.add(corner3.clone().subtract(0, 0, i));

			launchLocations.add(corner4.clone().subtract(i, 0, 0));
			launchLocations.add(corner4.clone().add(0, 0, i));
		}


		new BukkitRunnable() {
			@Override
			public void run() {
				Location launchLocation = launchLocations.get((int) (Math.random() * launchLocations.size()));

				FireworkEffect.Type[] types = FireworkEffect.Type.values();
				FireworkEffect.Type type = types[(int) (Math.random() * types.length)];

				int r = (int) (Math.random() * 255);
				int g = (int) (Math.random() * 255);
				int b = (int) (Math.random() * 255);
				Color color = Color.fromRGB(r, g, b);

				Firework fw = launchLocation.getWorld().spawn(launchLocation, Firework.class);
				FireworkMeta fm = fw.getFireworkMeta();

				//randomize power
				fm.setPower((int) (Math.random() * 2) + 1);

				fm.addEffects(FireworkEffect.builder().with(type).withColor(color).build());
				fw.setFireworkMeta(fm);
			}
		}.runTaskTimer(PixelParty.INSTANCE, 0, 5);
	}



}
