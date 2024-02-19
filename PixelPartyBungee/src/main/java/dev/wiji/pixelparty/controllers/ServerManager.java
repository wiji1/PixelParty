package dev.wiji.pixelparty.controllers;

import dev.wiji.pixelparty.BungeeMain;
import dev.wiji.pixelparty.enums.ServerType;
import dev.wiji.pixelparty.objects.GameServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.List;

public class ServerManager {
	public static final int STARTING_PORT = 25566;
	public static final int MAX_PORT = 25580;

	public static List<GameServer> gameServers = new ArrayList<>();

	public static GameServer createServer(ServerType serverType) {
		int port = getOpenPort();
		GameServer server = new GameServer(serverType, port);
		gameServers.add(server);
		return server;
	}

	public static boolean checkCodeAvailability(String code) {
		if(code == null) return false;

		for(GameServer gameServer : gameServers) {
			if(gameServer.code.equals(code)) return false;
		}
		return true;
	}

	public static GameServer getServer(String serverName) {
		for(GameServer gameServer : gameServers) {
			if(gameServer.getName().equals(serverName)) return gameServer;
		}

		return null;
	}

	public static void connectToLobby(ProxiedPlayer player) {
		BungeeMain.lobbyManager.queuePlayer(player);
	}

	public static int getOpenPort() {
		outer:
		for(int i = STARTING_PORT; i < MAX_PORT; i++) {

			for(GameServer gameServer : gameServers) {
				if(gameServer.port == i) continue outer;
			}

			return i;
		}

		throw new RuntimeException("No open ports available!");
	}


}
