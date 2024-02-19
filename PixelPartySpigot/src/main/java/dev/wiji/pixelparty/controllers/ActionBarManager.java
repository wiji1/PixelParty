package dev.wiji.pixelparty.controllers;

import dev.wiji.pixelparty.PixelParty;
import dev.wiji.pixelparty.util.Misc;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ActionBarManager {
	public static List<ActionBar> actionBars = new ArrayList<>();

	public static void sendActionBarMessage(Player player, String message) {
		sendActionBarMessage(player, message, 0);
	}
	public static void sendActionBarMessage(Player player, String message, int priority) {
		if(hasHigherPriority(player, priority)) return;

		actionBars.add(new ActionBar(player.getUniqueId(), priority, message));
	}

	public static boolean hasHigherPriority(Player player, int priority) {
		for(ActionBar actionBar : actionBars) {
			if(actionBar.getOwner().equals(player.getUniqueId()) && actionBar.getPriority() > priority) {
				return true;
			}
		}

		return false;
	}

	public static void sendActionBarMessageRaw(Player player, String message) {
		PacketPlayOutChat packet = new PacketPlayOutChat(IChatBaseComponent.ChatSerializer.a("{\"text\":\"" +
				ChatColor.translateAlternateColorCodes('&', message) + "\"}"), (byte) 2);
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
	}

	public static class ActionBar {

		private final UUID owner;
		private final int priority;
		private final String message;

		public ActionBar(UUID owner, int priority, String message) {
			this.owner = owner;
			this.priority = priority;
			this.message = message;

			send();

			new BukkitRunnable() {
				@Override
				public void run() {
					actionBars.remove(ActionBar.this);
				}
			}.runTaskLater(PixelParty.INSTANCE, 20 * 2);
		}

		public UUID getOwner() {
			return owner;
		}

		public int getPriority() {
			return priority;
		}

		public String getMessage() {
			return message;
		}

		public void send() {
			sendActionBarMessageRaw(Misc.getPlayer(owner), getMessage());
		}
	}
}
