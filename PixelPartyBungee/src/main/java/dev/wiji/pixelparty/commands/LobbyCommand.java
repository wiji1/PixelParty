package dev.wiji.pixelparty.commands;

import dev.wiji.pixelparty.BungeeMain;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class LobbyCommand extends Command {
	public LobbyCommand(String name) {
		super(name, "", "hub", "l");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!(sender instanceof ProxiedPlayer)) return;
		ProxiedPlayer player = (ProxiedPlayer) sender;

		String serverName = player.getServer().getInfo().getName();
		if(serverName.contains("LOB")) {
			player.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "You are already in the lobby!"));
			return;
		}

		BungeeMain.lobbyManager.queuePlayer(player);
	}
}
