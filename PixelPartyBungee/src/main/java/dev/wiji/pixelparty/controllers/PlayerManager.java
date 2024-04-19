package dev.wiji.pixelparty.controllers;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PlayerManager implements Listener {

	@EventHandler
	public void onPing(ProxyPingEvent event) {
		int version = event.getConnection().getVersion();
		version = Math.max(version, 47);
		event.getResponse().setVersion(new ServerPing.Protocol("PixelProxy 1.8-1.20", version));


		event.getResponse().setDescriptionComponent(new TextComponent(ChatColor.translateAlternateColorCodes('&', "&9PixelParty Practice &7-> &bwiji.dev")));
	}
}
