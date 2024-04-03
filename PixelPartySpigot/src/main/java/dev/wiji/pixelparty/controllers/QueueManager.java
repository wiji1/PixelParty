package dev.wiji.pixelparty.controllers;

import dev.wiji.pixelparty.PixelParty;
import dev.wiji.pixelparty.messaging.PluginMessage;
import dev.wiji.pixelparty.enums.GameSound;
import dev.wiji.pixelparty.enums.ServerType;
import dev.wiji.pixelparty.events.MessageEvent;
import dev.wiji.pixelparty.objects.PracticeProfile;
import dev.wiji.pixelparty.util.MetaDataUtil;
import dev.wiji.pixelparty.util.Misc;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class QueueManager implements Listener {
	public static final int MAX_PLAYERS = 16;
	public static final int START_THRESHOLD = 10;

	public static final Location SPAWN_LOCATION = new Location(Bukkit.getWorld("world"), -46, 4.5, 0, -90, 0);

	public List<UUID> queuedPlayers = new ArrayList<>();

	public int seconds = 30;
	public BukkitTask timer = null;

	public void startTimer() {
		timer = new BukkitRunnable() {

			@Override
			public void run() {
				if(seconds == 30) {
					Misc.broadcast("&eThe game starts in &a" + seconds + "&e seconds!");
					GameSound.BROADCAST.playAll();
				}

				seconds--;

				if(seconds == 20) Misc.broadcast("&eThe game starts in " + seconds + " seconds!");
				if(seconds == 10) Misc.broadcast("&eThe game starts in &c" + seconds + "&e seconds!");
				if(seconds <= 5 && seconds > 0) {
					Misc.broadcast("&eThe game starts in &c" + seconds + "&e seconds!");
				}

				if((seconds <= 5 && seconds > 0) || seconds % 10 == 0) GameSound.BROADCAST.playAll();

				if(seconds == 0) {
					cancel();
					PixelParty.gameManager.startGame();
				}
			}
		}.runTaskTimer(PixelParty.INSTANCE, 10, 20);
	}

	@EventHandler
	public void onJoin(AsyncPlayerPreLoginEvent event) {
		if(PixelParty.gameManager.gameState != GameManager.GameState.LOBBY) return;

		if(queuedPlayers.size() >= MAX_PLAYERS) {
			event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "The game is full!");
		}
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		PacketInjector.addPlayer(event.getPlayer());
		PacketManager.sendTabHeaders(event.getPlayer());

		//Used for practice mode to determine who created the game instance.
		if(PixelParty.owner == null) PixelParty.owner = event.getPlayer();

		if(PixelParty.serverType != ServerType.PRACTICE && PixelParty.gameManager.gameState != GameManager.GameState.LOBBY) return;

		event.getPlayer().teleport(SPAWN_LOCATION);
		SpectatorManager.giveLobbyButton(event.getPlayer());
		if(PixelParty.serverType == ServerType.PRACTICE) PracticeManager.givePrivacyItemStack(event.getPlayer());

		event.setJoinMessage(null);

		queuedPlayers.add(event.getPlayer().getUniqueId());
		Misc.broadcast(MetaDataUtil.getDisplayName(event.getPlayer()) + "&e has joined (&b" +
				queuedPlayers.size() + "&e/&b" + MAX_PLAYERS + "&e)");

		if(PixelParty.gameManager.gameState != GameManager.GameState.LOBBY) {
			PixelParty.gameManager.registerPlayer(event.getPlayer());
			return;
		}

		if(PixelParty.serverType == ServerType.PRACTICE && queuedPlayers.size() == 1) {
			startTimer();
			return;
		}

		if(queuedPlayers.size() >= START_THRESHOLD && timer == null) startTimer();

		if(queuedPlayers.size() >= MAX_PLAYERS && seconds > 10) seconds = 10;
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		PacketInjector.removePlayer(event.getPlayer());

		event.setQuitMessage(null);
		if(PixelParty.gameManager.gameState != GameManager.GameState.LOBBY && PixelParty.serverType != ServerType.PRACTICE) return;

		queuedPlayers.remove(event.getPlayer().getUniqueId());
		Misc.broadcast(MetaDataUtil.getDisplayName(event.getPlayer()) + "&e has quit!");

		if(PixelParty.serverType == ServerType.PRACTICE) {
			PixelParty.gameManager.alivePlayers.remove(event.getPlayer().getUniqueId());
			return;
		}

		if(queuedPlayers.size() < START_THRESHOLD && timer != null) {
			timer.cancel();
			timer = null;
			seconds = 30;

			Misc.broadcast("&cGame start canceled!");
		}
	}

	@EventHandler
	public void onMessage(MessageEvent event) {
		List<String> strings = event.getMessage().getStrings();
		if(strings.size() < 1) return;

		if(strings.get(0).equals("REQUEST TO JOIN")) {
			PracticeProfile profile = PixelParty.gameManager.practiceProfile;
			if(profile == null) return;

			PluginMessage response = new PluginMessage();
			response.writeString("JOIN RESPONSE");
			response.writeBoolean(!profile.isPrivate()).writeInt(Bukkit.getOnlinePlayers().size());
			response.replyTo(event.getMessage());
		}

	}
}
