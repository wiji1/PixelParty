package dev.wiji.pixelparty.powerups;

import dev.wiji.pixelparty.PixelParty;
import dev.wiji.pixelparty.controllers.FloorManager;
import dev.wiji.pixelparty.objects.PowerUp;
import dev.wiji.pixelparty.util.Misc;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class MagicCarpet extends PowerUp {

	public List<Player> carpetPlayers = new ArrayList<>();
	public List<Location> oldCarpetBlocks = new ArrayList<>();
	public List<Location> carpetBlocks = new ArrayList<>();

	public FloorManager manager = PixelParty.gameManager.floorManager;

	public MagicCarpet() {
		super("MagicCarpet");

		new BukkitRunnable() {
			@Override
			public void run() {
				oldCarpetBlocks = new ArrayList<>(carpetBlocks);
				carpetBlocks.clear();

				for(Player carpetPlayer : carpetPlayers) {
					getBlocks(carpetPlayer);
					Location newLocation = carpetPlayer.getLocation().clone();
					newLocation.setY(1);
					if(carpetPlayer.getLocation().getY() < 1) carpetPlayer.teleport(newLocation);
				}

				for(Location oldCarpetBlock : oldCarpetBlocks) {
					if(carpetBlocks.contains(oldCarpetBlock)) continue;

					if(manager.shouldBeAir(oldCarpetBlock.getBlock())) oldCarpetBlock.getBlock().setType(Material.AIR);
				}

				for(Location carpetBlock : carpetBlocks) {
					byte color = manager.getBlockColor(carpetBlock.getBlock());
					carpetBlock.getBlock().setTypeIdAndData(159, color, true);
				}
			}
		}.runTaskTimer(PixelParty.INSTANCE, 0, 2);
	}

	@Override
	public void onActivate(Player player, Location location) {
		carpetPlayers.add(player);
		player.sendMessage(Misc.color("&bA magic carpet will fly below you until the next round!"));
	}

	@Override
	public void onRoundEnd() {
		carpetPlayers.clear();
		carpetBlocks.clear();
		oldCarpetBlocks.clear();
	}

	public void getBlocks(Player player) {
		Location playerLocation = player.getLocation().clone();
		playerLocation.setX(playerLocation.getBlockX());
		playerLocation.setY(0);
		playerLocation.setZ(playerLocation.getBlockZ());

		Location backLeft = playerLocation.add(-1, 0, -1);

		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 3; j++) {
				Location current = backLeft.clone().add(i, 0, j);

				double rand = Math.random();
				player.getWorld().playEffect(current.clone().add(0, rand > 0.5 ? 1 : 2, 0), Effect.WITCH_MAGIC, 1);

				if(!manager.isOnFloor(current)) continue;
				if(!manager.shouldBeAir(current.getBlock())) continue;

				carpetBlocks.add(current);
			}
		}
	}
}
