package dev.wiji.pixelparty.controllers;

import dev.wiji.pixelparty.PixelParty;
import dev.wiji.pixelparty.enums.LeaderboardStatistic;
import dev.wiji.pixelparty.enums.LeaderboardType;
import dev.wiji.pixelparty.enums.ServerType;
import dev.wiji.pixelparty.leaderboard.Leaderboard;
import dev.wiji.pixelparty.playerdata.PixelPlayer;
import dev.wiji.pixelparty.util.MetaDataUtil;
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
        PixelPlayer player = PixelPlayer.getPixelPlayer(event.getPlayer());

        int elo = player.getLeaderboardStat(LeaderboardType.LIFETIME,
                PixelParty.serverType == ServerType.NORMAL ? LeaderboardStatistic.NORMAL_ELO : LeaderboardStatistic.HYPER_ELO);

        String eloString = "&e" + elo + " ";
        boolean ranked = PixelParty.gameManager.ranked;

        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', (ranked ? eloString : "") +
                MetaDataUtil.getNameAndRank(event.getPlayer().getUniqueId()) + "&f: " + originalMessage));
    }

}
