package dev.wiji.pixelparty.controllers;

import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PlayerManager implements Listener {

	@EventHandler
	public void onJoin(PostLoginEvent event) {
//		BungeeMain.lobbyManager.queuePlayer(event.getPlayer());


	}
}
