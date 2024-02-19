package dev.wiji.pixelparty.controllers;

import dev.wiji.pixelparty.util.Misc;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatManager implements Listener {

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);

        String originalMessage = event.getMessage();

        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',
                Misc.getNameAndRank(event.getPlayer()) + "&f: " + originalMessage));
    }

}
