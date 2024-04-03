package dev.wiji.pixelparty.controllers;

import dev.wiji.pixelparty.BungeeMain;
import dev.wiji.pixelparty.enums.ServerType;
import dev.wiji.pixelparty.inspector.events.MessageEvent;
import dev.wiji.pixelparty.messaging.PluginMessage;
import dev.wiji.pixelparty.objects.GameServer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.*;

public class RankedQueueManager extends QueueManager implements Listener {

	public Map<UUID, ServerType> queuePreference = new HashMap<>();
	public List<UUID> lockedPlayers = new ArrayList<>();

	public RankedQueueManager() {
		super(ServerType.RANKED);
	}

	@Override
	public void queuePlayer(ProxiedPlayer player) {
		if(!queuePreference.containsKey(player.getUniqueId())) {
			player.sendMessage(TextComponent.fromLegacyText(ChatColor.RED +
					"An error has occurred while queuing, please try again later"));
			return;
		}

		if(lockedPlayers.contains(player.getUniqueId())) {
			player.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "You must wait for your previous ranked game to end before re-queuing!"));
			return;
		}

		ServerType preference = queuePreference.get(player.getUniqueId());
		QueueManager manager = QueueManager.getQueue(preference);
		if(manager == null) return;

		for(GameServer queueServer : queueServers) {
			if(queueServer.serverType != preference) continue;
			if(queueServer.getPlayerCount() >= queueServer.maxPlayers) continue;

			queueServer.queuePlayer(player);
			return;
		}

		GameServer transferred = null;

		for(GameServer queueServer : manager.queueServers) {
			if(queueServer.getPlayerCount() > 0) {
				continue;
			}

			queueServer.queuePlayer(player);
			transferred = queueServer;
			break;
		}

		if(transferred != null) {
			PluginMessage message = new PluginMessage();
			message.writeString("SET RANKED");
			message.setIntendedServer(transferred.getName()).send();

			queueServers.add(transferred);
			manager.queueServers.remove(transferred);
			manager.callForServer();
		} else {
			player.sendMessage(TextComponent.fromLegacyText(ChatColor.RED +
					"All servers are currently full, please try again later"));
		}

		queuePreference.remove(player.getUniqueId());
	}

	@EventHandler
	public void onMessage(MessageEvent event) {
		super.onMessage(event);

		PluginMessage message = event.getMessage();
		List<String> strings = message.getStrings();

		if(strings.size() < 2) return;

		if(strings.get(0).equals("RANKED PREFERENCE")) {
			UUID playerUUID = UUID.fromString(strings.get(1));
			ServerType type = ServerType.valueOf(strings.get(2));

			queuePreference.put(playerUUID, type);
		}

		if(strings.get(0).equals("LOCK PLAYERS")) {
			strings.remove(0);
			for(String string : strings) {
				UUID uuid = UUID.fromString(string);

				if(!lockedPlayers.contains(uuid)) lockedPlayers.add(uuid);
			}
		}

		if(strings.get(0).equals("UNLOCK PLAYERS")) {
			strings.remove(0);
			for(String string : strings) {
				UUID uuid = UUID.fromString(string);

				lockedPlayers.remove(uuid);
			}
		}
	}
}
