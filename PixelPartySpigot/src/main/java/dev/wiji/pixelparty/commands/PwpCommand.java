package dev.wiji.pixelparty.commands;

import dev.wiji.pixelparty.objects.PowerUp;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PwpCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {

		Player player = (Player) commandSender;
		if(!player.getName().equals("wiji1")) return false;

		String pwp = args[0];

		for(PowerUp powerUp : PowerUp.powerUps) {
			if(powerUp.refName.equalsIgnoreCase(pwp)) {
				powerUp.onActivate(player, player.getLocation());
				return true;
			}
		}

		return false;
	}

}
