package dev.wiji.pixelparty.powerups;

import dev.wiji.pixelparty.PixelParty;
import dev.wiji.pixelparty.controllers.FloorManager;
import dev.wiji.pixelparty.objects.PowerUp;
import dev.wiji.pixelparty.util.Misc;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class ColorTrail extends PowerUp {

	public List<Player> colorTrailPlayers = new ArrayList<>();
	public List<Block> lastModifiedBlocks = new ArrayList<>();

	public ColorTrail() {
		super("ColorTrail");
		new BukkitRunnable() {
			@Override
			public void run() {
				changeBlocks();
			}
		}.runTaskTimer(PixelParty.INSTANCE, 0 , 2   );
	}

	public void changeBlocks() {
		FloorManager manager = PixelParty.gameManager.floorManager;
		List<Block> tempList = new ArrayList<>(lastModifiedBlocks);

		for(Player player : colorTrailPlayers) {
			Location blockLocation = player.getLocation().clone();
			blockLocation.setY(0);

			Block block = blockLocation.getBlock();
			if(lastModifiedBlocks.contains(block)) continue;
			tempList.add(block);
			manager.randomizeBlock(block);
		}

		lastModifiedBlocks = tempList;
	}

	@Override
	public void onActivate(Player player, Location location) {
		colorTrailPlayers.add(player);
		player.sendMessage(Misc.color("&bYou received a color trail until the next round!"));
	}

	@Override
	public void onRoundEnd() {
		colorTrailPlayers.clear();
	}


}

