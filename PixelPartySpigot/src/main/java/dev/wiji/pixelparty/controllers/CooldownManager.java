package dev.wiji.pixelparty.controllers;

import dev.wiji.pixelparty.PixelParty;
import dev.wiji.pixelparty.enums.CooldownType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CooldownManager {
	public static List<Cooldown> cooldowns = new ArrayList<>();

	public static void addCooldown(Player player, CooldownType type, int ticks) {
		addCooldown(player.getUniqueId(), type, ticks);
	}

	public static void addCooldown(UUID player, CooldownType type, int ticks) {
		Cooldown previous = getCooldown(player, type);

		if(previous != null) previous.remove();
		cooldowns.add(new Cooldown(player, type, ticks));
	}

	public static Cooldown getCooldown(Player player, CooldownType type) {
		return getCooldown(player.getUniqueId(), type);
	}

	public static Cooldown getCooldown(UUID player, CooldownType type) {
		for(Cooldown cooldown : cooldowns) {
			if(cooldown.getPlayer().equals(player) && cooldown.getType().equals(type)) {
				return cooldown;
			}
		}

		return null;
	}

	public static boolean isOnCooldown(Player player, CooldownType type) {
		return getCooldown(player, type) != null;
	}

	private static class Cooldown {
		private final UUID player;
		private final CooldownType type;
		private final BukkitTask runnable;

		public Cooldown(UUID player, CooldownType type, int ticks) {
			this.player = player;
			this.type = type;

			runnable = new BukkitRunnable() {
				@Override
				public void run() {
					remove();
				}
			}.runTaskLater(PixelParty.INSTANCE, ticks);
		}

		public UUID getPlayer() {
			return player;
		}

		public CooldownType getType() {
			return type;
		}

		private void remove() {
			runnable.cancel();
			cooldowns.remove(Cooldown.this);
		}
	}
}
