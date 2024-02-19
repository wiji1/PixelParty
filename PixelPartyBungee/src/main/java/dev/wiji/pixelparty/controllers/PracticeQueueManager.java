package dev.wiji.pixelparty.controllers;

import dev.wiji.pixelparty.messaging.PluginMessage;
import dev.wiji.pixelparty.enums.ServerType;
import dev.wiji.pixelparty.inspector.events.MessageEvent;
import dev.wiji.pixelparty.objects.GameServer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.event.EventHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PracticeQueueManager extends QueueManager {

	public static Map<String, UUID> ownershipMap = new HashMap<>();

	public PracticeQueueManager() {
		super(ServerType.PRACTICE);
	}

	@Override
	public void queuePlayer(ProxiedPlayer player) {
		int fullServers = 0;

		for(GameServer queueServer : queueServers) {
			if(queueServer.getPlayerCount() >= queueServer.maxPlayers) {
				fullServers++;
				continue;
			}

			queueServer.queuePlayer(player);
			ownershipMap.put(queueServer.getName(), player.getUniqueId());
			break;
		}

		if(fullServers == queueServers.size()) {
			player.sendMessage(TextComponent.fromLegacyText(ChatColor.RED +
					"All servers are currently full, please try again later"));
		} else if(fullServers == queueServers.size() - 1 && waitingServers.isEmpty()) callForServer();
	}

	@EventHandler
	public void onPracticeMessage(MessageEvent event) {
		PluginMessage message = event.getMessage();
		List<String> strings = message.getStrings();

		if(strings.get(0).equals("OWNERSHIP CHANGE")) {
			UUID playerUUID = UUID.fromString(strings.get(1));

			ownershipMap.put(message.originServer, playerUUID);
		}

		if(strings.get(0).equals("REQUEST PRACTICE DATA")) {
			PluginMessage response = new PluginMessage();
			for(Map.Entry<String, UUID> entry : ownershipMap.entrySet()) {
				response.writeString(entry.getKey() + "/" + entry.getValue().toString());
			}
			response.writeInt(ServerType.PRACTICE.maxServers);
			response.replyTo(message);
		}
	}

}
