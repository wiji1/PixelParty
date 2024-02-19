package dev.wiji.pixelparty.commands;

import dev.wiji.pixelparty.PixelParty;
import net.raphimc.noteblocklib.player.SongPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TestCommand implements CommandExecutor {

	SongPlayer player;

	@Override
	public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {

		if(args.length == 0) {
			if(Bukkit.getOnlinePlayers().size() == 1) PixelParty.gameManager.startGame();
			else PixelParty.gameManager.queueManager.startTimer();
//			PixelParty.INSTANCE.gameManager.startGame();
		}

		if(args.length == 1) {
			Player player = (Player) commandSender;
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
