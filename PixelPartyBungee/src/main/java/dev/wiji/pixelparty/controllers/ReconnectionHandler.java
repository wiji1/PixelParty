package dev.wiji.pixelparty.controllers;

import dev.wiji.pixelparty.BungeeMain;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ReconnectHandler;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class ReconnectionHandler implements ReconnectHandler {

	@Override
	public ServerInfo getServer(ProxiedPlayer player) {
		ServerInfo server = BungeeMain.lobbyManager.getLobbyServer();

		if(server == null) {
			player.disconnect(TextComponent.fromLegacyText(ChatColor.RED + "There are currently no lobbies available, " +
					"please try again in a moment!"));
		}

		return server;
	}

	@Override
	public void setServer(ProxiedPlayer player) {

	}

	@Override
	public void save() {

	}

	@Override
	public void close() {

	}
}
