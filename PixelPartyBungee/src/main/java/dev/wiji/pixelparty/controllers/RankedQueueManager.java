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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RankedQueueManager extends QueueManager implements Listener {

	public Map<UUID, ServerType> queuePreference = new HashMap<>();

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

		ServerType preference = queuePreference.get(player.getUniqueId());
		QueueManager manager = QueueManager.getQueue(preference);
		if(manager == null) return;

		for(GameServer queueServer : queueServers) {
			if(queueServer.serverType != preference) continue;
			if(queueServer.getPlayerCount() >= queueServer.maxPlayers) continue;

			queueServer.queuePlayer(player);
			return;
		}

		int fullServers = 0;
		GameServer transferred = null;

		for(GameServer queueServer : manager.queueServers) {
			if(queueServer.getPlayerCount() >= queueServer.maxPlayers) {
				fullServers++;
				continue;
			}

			queueServer.queuePlayer(player);
			transferred = queueServer;
			break;
		}

		if(transferred != null) {
			queueServers.add(transferred);
			manager.queueServers.remove(transferred);
			manager.callForServer();

			//TODO: Tell server its ranked now
		}

		queuePreference.remove(player.getUniqueId());

		if(fullServers == manager.queueServers.size()) {
			player.sendMessage(TextComponent.fromLegacyText(ChatColor.RED +
					"All servers are currently full, please try again later"));
		} else if(fullServers == manager.queueServers.size() - 1 && waitingServers.isEmpty()) callForServer();
	}

	@EventHandler
	public void onMessage(MessageEvent event) {
		PluginMessage message = event.getMessage();
		List<String> strings = message.getStrings();

		if(strings.size() < 2) return;

		if(strings.get(0).equals("RANKED PREFERENCE")) {
			UUID playerUUID = UUID.fromString(strings.get(1));
			ServerType type = ServerType.valueOf(strings.get(2));

			queuePreference.put(playerUUID, type);
		}
	}
}
