package dev.wiji.pixelparty.controllers;

import dev.wiji.pixelparty.messaging.PluginMessage;
import dev.wiji.pixelparty.enums.ServerType;
import dev.wiji.pixelparty.inspector.events.MessageEvent;
import dev.wiji.pixelparty.objects.GameServer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class QueueManager implements Listener {
	public static List<QueueManager> queueManagers = new ArrayList<>();

	public ServerType serverType;

	public List<GameServer> queueServers = new LinkedList<>();
	public List<GameServer> activeServers = new LinkedList<>();
	public List<GameServer> waitingServers = new LinkedList<>();

	public QueueManager(ServerType serverType) {
		this.serverType = serverType;

		callForServer();
		queueManagers.add(this);
	}

	public void queuePlayer(ProxiedPlayer player) {
		int fullServers = 0;

		for(GameServer queueServer : queueServers) {
			if(queueServer.getPlayerCount() >= queueServer.maxPlayers) {
				fullServers++;
				continue;
			}

			queueServer.queuePlayer(player);
			break;
		}

		if(fullServers == queueServers.size()) {
			player.sendMessage(TextComponent.fromLegacyText(ChatColor.RED +
					"All servers are currently full, please try again later"));
		} else if(fullServers == queueServers.size() - 1 && waitingServers.isEmpty()) callForServer();
	}

	public void callForServer() {
		if(getTotalServers() >= serverType.maxServers) return;

		GameServer server = ServerManager.createServer(serverType);
		waitingServers.add(server);
	}

	public int getTotalServers() {
		return queueServers.size() + activeServers.size() + waitingServers.size();
	}

	public int getTotalPlayers() {
		int totalPlayers = 0;
		for(GameServer server : queueServers) {
			totalPlayers += server.getPlayerCount();
		}
		for(GameServer server : activeServers) {
			totalPlayers += server.getPlayerCount();
		}
		return totalPlayers;
	}

	@EventHandler
	public void onMessage(MessageEvent event) {
		PluginMessage message = event.getMessage();
		List<String> strings = message.getStrings();

		GameServer server = ServerManager.getServer(message.originServer);
		if(server == null || server.serverType != serverType) return;

		if(!queueServers.contains(server) && !activeServers.contains(server) && !waitingServers.contains(server)) return;

		if(strings.size() < 1) return;

		if(strings.get(0).equals("SERVER START")) {
			LeaderboardReset.sendNextResetTime(server);

			System.out.println("Server started: " + server.serverType.name());
			waitingServers.remove(server);
			queueServers.add(server);
			return;
		}

		if(strings.get(0).equals("GAME START")) {
			queueServers.remove(server);
			activeServers.add(server);
			return;
		}

		if(strings.get(0).equals("GAME END")) {
			activeServers.remove(server);

			for(ProxiedPlayer player : server.getOnlinePlayers()) {
				ServerManager.connectToLobby(player);
			}

			new Thread(() -> {
				 try { Thread.sleep(250); } catch(InterruptedException e) { e.printStackTrace(); }

				server.terminate();
			}).start();
		}

		if(strings.get(0).equals("QUEUE PLAYER")) {
			String playerUUID = strings.get(1);

			ProxiedPlayer player = ProxyServer.getInstance().getPlayer(UUID.fromString(playerUUID));
			if(player == null) return;

			queuePlayer(player);
		}
	}

	public static void queuePlayer(ProxiedPlayer player, ServerType serverType) {
		for(QueueManager queueManager : queueManagers) {
			if(queueManager.serverType == serverType) {
				queueManager.queuePlayer(player);
				return;
			}
		}
	}

	public static QueueManager getQueue(ServerType serverType) {
		for(QueueManager queueManager : queueManagers) {
			if(queueManager.serverType == serverType) {
				return queueManager;
			}
		}

		return null;
	}
}
