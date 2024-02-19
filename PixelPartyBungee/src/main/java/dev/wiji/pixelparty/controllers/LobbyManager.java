package dev.wiji.pixelparty.controllers;

import dev.wiji.pixelparty.BungeeMain;
import dev.wiji.pixelparty.messaging.PluginMessage;
import dev.wiji.pixelparty.enums.ServerType;
import dev.wiji.pixelparty.inspector.events.MessageEvent;
import dev.wiji.pixelparty.objects.GameServer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.List;
import java.util.UUID;

public class LobbyManager extends QueueManager implements Listener {

	public LobbyManager() {
		super(ServerType.LOBBY);

		new Thread(() -> {
			while(true) {
				try {
					Thread.sleep(1000 * 60 * 5);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				checkToClose();
			}
		}).start();

		new Thread(() -> {
			while(true) {
				try {
					Thread.sleep(1000 * 10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				PluginMessage message = new PluginMessage();
				message.writeString("PLAYER COUNTS");

				for(ServerType value : ServerType.values()) {
					QueueManager queueManager = QueueManager.getQueue(value);
					if(queueManager == null) continue;

					message.writeString(value.name() + " " + queueManager.getTotalPlayers());
				}

				for(GameServer activeServer : activeServers) {
					message.setIntendedServer(activeServer.getName()).send();
				}
			}
		}).start();
	}

	@Override
	public void queuePlayer(ProxiedPlayer player) {
		int fullServers = 0;
		int partiallyFullServers = 0;

		for(GameServer lobby : activeServers) {
			if(lobby.getPlayerCount() >= lobby.maxPlayers) {
				fullServers++;
				continue;
			} else if(lobby.getPlayerCount() >= lobby.maxPlayers / 2) partiallyFullServers++;

			lobby.queuePlayer(player);
		}

		if(fullServers == activeServers.size()) {
			player.disconnect(TextComponent.fromLegacyText(ChatColor.RED +
					"All lobbies are currently full, please try again later"));
		} else if(fullServers == activeServers.size() - 1 && partiallyFullServers > 0) {
			callForServer();
		}

	}

	public ServerInfo getLobbyServer() {
		int leastPlayers = Integer.MAX_VALUE;
		ServerInfo leastPlayersServer = null;

		for(GameServer lobby : activeServers) {
			if(lobby.getPlayerCount() < leastPlayers) {
				leastPlayers = lobby.getPlayerCount();
				leastPlayersServer = lobby.getServerInfo();
			}
		}

		return leastPlayersServer;
	}

	public void checkToClose() {
		int totalPlayers = 0;

		for(GameServer lobby : activeServers) totalPlayers += lobby.getPlayerCount();

		if(totalPlayers < activeServers.size() * activeServers.get(0).maxPlayers - activeServers.get(0).maxPlayers / 2) {
			int lobbiesToClose = activeServers.size() - (totalPlayers / activeServers.get(0).maxPlayers + 1);

			for(int i = 0; i < lobbiesToClose; i++) {
				GameServer lobby = activeServers.get(i);
				activeServers.remove(lobby);

				for(ProxiedPlayer onlinePlayer : lobby.getOnlinePlayers()) {
					queuePlayer(onlinePlayer);
				}

				lobby.terminate();
			}
		}
	}

	@EventHandler
	public void onMessage(MessageEvent event) {
		PluginMessage message = event.getMessage();
		List<String> strings = message.getStrings();

		GameServer server = ServerManager.getServer(message.originServer);
		assert server != null;

		if(strings.size() < 1) return;

		if(strings.get(0).equals("LOBBY PLAYER")) {
			ProxiedPlayer player = BungeeMain.INSTANCE.getProxy().getPlayer(UUID.fromString(strings.get(1)));
			if(player == null) return;

			queuePlayer(player);
		}

		if(!activeServers.contains(server) && !waitingServers.contains(server)) return;

		if(strings.get(0).equals("SERVER START")) {
			waitingServers.remove(server);
			activeServers.add(server);
			return;
		}
	}
}
