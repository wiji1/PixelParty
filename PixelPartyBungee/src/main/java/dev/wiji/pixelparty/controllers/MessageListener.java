package dev.wiji.pixelparty.controllers;

import dev.wiji.pixelparty.messaging.PluginMessage;
import dev.wiji.pixelparty.enums.ServerType;
import dev.wiji.pixelparty.inspector.events.MessageEvent;
import dev.wiji.pixelparty.objects.GameServer;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.List;
import java.util.UUID;

public class MessageListener implements Listener {

	@EventHandler
	public void onMessage(MessageEvent event) {
		PluginMessage message = event.getMessage();
		List<String> strings = message.getStrings();

		if(strings.get(0).equals("QUEUE")) {
			UUID playerUUID = UUID.fromString(strings.get(1));
			ProxiedPlayer player = ProxyServer.getInstance().getPlayer(playerUUID);
			if(player == null) return;

			ServerType requested = ServerType.valueOf(strings.get(2));
			QueueManager.queuePlayer(player, requested);
		}

		if(strings.get(0).equals("JOIN SERVER")) {
			String server = strings.get(1);
			GameServer gameServer = ServerManager.getServer(server);
			if(gameServer == null) return;

			UUID playerUUID = UUID.fromString(strings.get(2));
			ProxiedPlayer player = ProxyServer.getInstance().getPlayer(playerUUID);

			if(player == null) return;
			gameServer.queuePlayer(player);
		}
	}
}
