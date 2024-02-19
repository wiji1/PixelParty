package dev.wiji.pixelparty.commands;

import dev.wiji.pixelparty.PixelParty;
import dev.wiji.pixelparty.messaging.PluginMessage;
import dev.wiji.pixelparty.enums.ServerType;
import dev.wiji.pixelparty.inventory.ServerSelectorGUI;
import dev.wiji.pixelparty.util.Misc;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MenuCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {

		if(!(commandSender instanceof Player)) return false;
		Player player = (Player) commandSender;


		ServerType type = PixelParty.serverType;
		if(type != ServerType.LOBBY) {
			player.sendMessage(Misc.color("&cThis message can only be run from the lobby!"));
			return false;
		}

		PluginMessage message = new PluginMessage();
		message.setIntendedServer("PROXY");
		message.writeString("REQUEST PRACTICE DATA");
		message.request((response) -> {
			ServerSelectorGUI gui = new ServerSelectorGUI(player, response);
			gui.open();
		});


		return false;
	}

}
