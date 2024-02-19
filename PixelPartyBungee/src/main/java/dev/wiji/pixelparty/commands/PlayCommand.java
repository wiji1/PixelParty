package dev.wiji.pixelparty.commands;

import dev.wiji.pixelparty.controllers.QueueManager;
import dev.wiji.pixelparty.enums.ServerType;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class PlayCommand extends Command {

    public static int port = 25569;

    public PlayCommand(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {

        if(!(commandSender instanceof ProxiedPlayer)) {
            commandSender.sendMessage(new TextComponent("Only players can use this command!"));
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) commandSender;

        ServerType requested = ServerType.NORMAL;

        if(strings.length > 0) {
            try {
                requested = ServerType.valueOf(strings[0].toUpperCase());
            } catch (IllegalArgumentException e) {
                player.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "Invalid server type"));
                return;
            }
        }

        player.sendMessage(TextComponent.fromLegacyText(ChatColor.GRAY + "Finding you a server (This might take a moment)"));
        QueueManager.queuePlayer(player, requested);


        port++;
    }
}
