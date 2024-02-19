package dev.wiji.pixelparty.controllers;

import de.tr7zw.nbtapi.NBTItem;
import dev.wiji.pixelparty.PixelParty;
import dev.wiji.pixelparty.messaging.PluginMessage;
import dev.wiji.pixelparty.enums.NBTTag;
import dev.wiji.pixelparty.inventory.SpectatorGUI;
import dev.wiji.pixelparty.inventory.TeleporterGUI;
import dev.wiji.pixelparty.util.Misc;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpectatorManager implements Listener {

	public Map<Player, BukkitTask> teleportingPlayers = new HashMap<>();

	public SpectatorManager() {

	}

	public static void setSpectator(Player player) {
		giveItems(player);
		giveLobbyButton(player);
		player.setAllowFlight(true);
	}

	public static void giveItems(Player player) {
		ItemStack compass = new ItemStack(Material.COMPASS);
		ItemMeta compassMeta = compass.getItemMeta();
		compassMeta.setDisplayName(Misc.color("&b&lTeleporter &7(Right Click)"));
		List<String> compassLore = new ArrayList<>();
		compassLore.add(Misc.color("&7Right-click to spectate players!"));
		compassMeta.setLore(compassLore);
		compass.setItemMeta(compassMeta);

		NBTItem nbtCompass = new NBTItem(compass, true);
		nbtCompass.setBoolean(NBTTag.TELEPORTER.getRef(), true);

		player.getInventory().setItem(0, compass);


		ItemStack settings = new ItemStack(Material.REDSTONE_COMPARATOR);
		ItemMeta settingsMeta = settings.getItemMeta();
		settingsMeta.setDisplayName(Misc.color("&b&lSpectator Settings &7(Right Click)"));
		List<String> settingsLore = new ArrayList<>();
		settingsLore.add(Misc.color("&7Right-click to change your spectator settings!"));
		settingsMeta.setLore(settingsLore);
		settings.setItemMeta(settingsMeta);

		NBTItem nbtSettings = new NBTItem(settings, true);
		nbtSettings.setBoolean(NBTTag.SPECTATOR_SETTINGS.getRef(), true);

		player.getInventory().setItem(4, settings);


		ItemStack playAgain = new ItemStack(Material.PAPER);
		ItemMeta playAgainMeta = playAgain.getItemMeta();
		playAgainMeta.setDisplayName(Misc.color("&b&lPlay Again &7(Right Click)"));
		List<String> playAgainLore = new ArrayList<>();
		playAgainLore.add(Misc.color("&7Right-click to play another game!"));
		playAgainMeta.setLore(playAgainLore);
		playAgain.setItemMeta(playAgainMeta);

		NBTItem nbtPlayAgain = new NBTItem(playAgain, true);
		nbtPlayAgain.setBoolean(NBTTag.PLAY_AGAIN.getRef(), true);

		player.getInventory().setItem(7, playAgain);
	}

	public static void giveLobbyButton(Player player) {

		ItemStack lobby = new ItemStack(Material.BED);
		ItemMeta lobbyMeta = lobby.getItemMeta();
		lobbyMeta.setDisplayName(Misc.color("&c&lReturn to Lobby &7(Right Click)"));
		List<String> lobbyLore = new ArrayList<>();
		lobbyLore.add(Misc.color("&7Right-click to return to the lobby!"));
		lobbyMeta.setLore(lobbyLore);
		lobby.setItemMeta(lobbyMeta);

		NBTItem nbtLobby = new NBTItem(lobby, true);
		nbtLobby.setBoolean(NBTTag.BACK_TO_LOBBY.getRef(), true);

		player.getInventory().setItem(8, lobby);
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		if(event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) return;

		Player player = event.getPlayer();
		ItemStack item = event.getItem();

		if(Misc.isAirOrNull(item)) return;

		NBTItem nbtItem = new NBTItem(item);
		if(nbtItem.hasKey(NBTTag.TELEPORTER.getRef())) {
			TeleporterGUI gui = new TeleporterGUI(player);
			gui.open();
			return;
		}

		if(nbtItem.hasKey(NBTTag.SPECTATOR_SETTINGS.getRef())) {
			SpectatorGUI gui = new SpectatorGUI(player);
			gui.open();
			return;
		}

		if(nbtItem.hasKey(NBTTag.PLAY_AGAIN.getRef())) {
			PluginMessage message = new PluginMessage();
			message.writeString("QUEUE PLAYER");
			message.writeString(player.getUniqueId().toString());
			message.setIntendedServer("PROXY").send();
			return;
		}

		if(nbtItem.hasKey(NBTTag.BACK_TO_LOBBY.getRef())) {

			if(teleportingPlayers.containsKey(player)) {
				player.sendMessage(Misc.color("&c&lTeleport cancelled!"));
				BukkitTask task = teleportingPlayers.get(player);
				task.cancel();
				teleportingPlayers.remove(player);
				return;
			}

			teleportingPlayers.put(player, new BukkitRunnable() {
				@Override
				public void run() {
					teleportingPlayers.remove(player);

					PluginMessage message = new PluginMessage();
					message.writeString("LOBBY PLAYER");
					message.writeString(player.getUniqueId().toString());
					message.setIntendedServer("PROXY").send();

				}
			}.runTaskLater(PixelParty.INSTANCE, 60));

			player.sendMessage(Misc.color("&a&lTeleporting you to the lobby in 3 seconds... Right-click again to cancel the teleport!"));
		}
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		teleportingPlayers.remove(event.getPlayer());
	}
}
