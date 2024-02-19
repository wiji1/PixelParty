package dev.wiji.pixelparty.objects;

import dev.wiji.pixelparty.controllers.DockerManager;
import dev.wiji.pixelparty.controllers.PracticeQueueManager;
import dev.wiji.pixelparty.controllers.QueueManager;
import dev.wiji.pixelparty.controllers.ServerManager;
import dev.wiji.pixelparty.enums.ServerType;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.List;

public class GameServer {
	public String code;
	public ServerType serverType;
	public String containerID;
	public int port;

	public int maxPlayers;

	public GameServer(ServerType serverType, int port) {
		this.serverType = serverType;
		this.port = port;

		this.maxPlayers = serverType.defaultMaxPlayers;

		create();
	}

	public void terminate() {
		DockerManager.terminateContainer(containerID);
		ServerManager.gameServers.remove(this);
		PracticeQueueManager.ownershipMap.remove(getName());

		QueueManager manager = QueueManager.getQueue(serverType);
		assert manager != null;
		if(manager.getTotalServers() == serverType.maxServers - 1) manager.callForServer();
	}

	public List<ProxiedPlayer> getOnlinePlayers() {
		return new ArrayList<>(ProxyServer.getInstance().getServerInfo(getName()).getPlayers());
	}

	public int getPlayerCount() {
		return getOnlinePlayers().size();
	}

	public void queuePlayer(ProxiedPlayer player) {
		player.connect(ProxyServer.getInstance().getServerInfo(getName()));
		player.sendMessage(TextComponent.fromLegacyText(ChatColor.GREEN + "Sending you to " + getName()));
	}

	public ServerInfo getServerInfo() {
		return ProxyServer.getInstance().getServerInfo(getName());
	}

	public void create() {
		this.containerID = DockerManager.createContainer(this);
	}

	public String getName() {
		return serverType.getIdentifier() + "-" + code;
	}
}



