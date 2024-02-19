package dev.wiji.pixelparty.controllers;

import dev.wiji.pixelparty.PixelParty;
import dev.wiji.pixelparty.util.Misc;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class TitleManager {

	public static void displayEndTitle() {
		List<UUID> alivePlayers = PixelParty.gameManager.alivePlayers;

		String title;
		String subtitle;

		if(alivePlayers.size() == 1) {
			title = Misc.getDisplayName(alivePlayers.get(0));
			subtitle = "&7is the winner of this game!";
		} else if(alivePlayers.size() == 2) {
			title = Misc.getDisplayName(alivePlayers.get(0)) + " &f& " +
					Misc.getDisplayName(alivePlayers.get(1));
			subtitle = "&7are the winners of this game!";
		} else {
			title = "&aIT'S A TIE";
			subtitle = "&e" + alivePlayers.size() + " &7players won this game!";
		}

		displayTitle(title, 10 * 20);
		displaySubTitle(subtitle, 10 * 20);
	}


	public static void displayTitle(String message, int length) {

		for(Player player : Bukkit.getOnlinePlayers()) {
			IChatBaseComponent chatTitle = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" +
					ChatColor.translateAlternateColorCodes('&', message) + "\"}");

			PacketPlayOutTitle title = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, chatTitle);
			PacketPlayOutTitle titleLength = new PacketPlayOutTitle(5, length, 5);

			((CraftPlayer) player).getHandle().playerConnection.sendPacket(title);
			((CraftPlayer) player).getHandle().playerConnection.sendPacket(title);
		}

	}

	public static void displaySubTitle(String message, int length) {
		for(Player player : Bukkit.getOnlinePlayers()) {
			IChatBaseComponent chatSubTitle = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" +
					ChatColor.translateAlternateColorCodes('&', message) + "\"}");

			PacketPlayOutTitle subtitle = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, chatSubTitle);
			PacketPlayOutTitle subTitleLength = new PacketPlayOutTitle(5, length, 5);

			((CraftPlayer) player).getHandle().playerConnection.sendPacket(subtitle);
			((CraftPlayer) player).getHandle().playerConnection.sendPacket(subTitleLength);
		}
	}
}
