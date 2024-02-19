package dev.wiji.pixelparty.powerups;

import de.tr7zw.nbtapi.NBTItem;
import dev.wiji.pixelparty.PixelParty;
import dev.wiji.pixelparty.controllers.CooldownManager;
import dev.wiji.pixelparty.enums.CooldownType;
import dev.wiji.pixelparty.enums.GameSound;
import dev.wiji.pixelparty.enums.NBTTag;
import dev.wiji.pixelparty.objects.PowerUp;
import dev.wiji.pixelparty.util.Misc;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

import static dev.wiji.pixelparty.controllers.AmbienceManager.CENTER;

public class AcidRain extends PowerUp {

	@Override
	public void onActivate(Player player, Location location) {
		ItemStack acidRain = new ItemStack(Material.MAGMA_CREAM);
		ItemMeta meta = acidRain.getItemMeta();
		meta.setDisplayName(Misc.color("&6&lAcid Rain"));

		List<String> lore = new ArrayList<>();
		lore.add(Misc.color("&7Acid will pour from above,"));
		lore.add(Misc.color("&7splattering paint everywhere!"));

		meta.setLore(lore);
		acidRain.setItemMeta(meta);

		NBTItem nbtItem = new NBTItem(acidRain, true);
		nbtItem.setBoolean(NBTTag.ACID_RAIN.getRef(), true);

		player.getInventory().addItem(acidRain);
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		ItemStack item = player.getInventory().getItemInHand();

		if(event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		if(CooldownManager.isOnCooldown(player, CooldownType.ITEM_USE)) return;
		if(Misc.isAirOrNull(item)) return;

		NBTItem nbtItem = new NBTItem(item);
		if(!nbtItem.hasKey(NBTTag.ACID_RAIN.getRef())) return;

		for(int i = 0; i < 200; i++) spawnSnowball();

		if(item.getAmount() > 1) item.setAmount(item.getAmount() - 1);
		else player.getInventory().remove(item);

		CooldownManager.addCooldown(player, CooldownType.ITEM_USE, 5);
		Bukkit.broadcastMessage(Misc.color("&cAcid pours from the sky! &7Random colors appear where it hits the ground!"));
	}

	@EventHandler
	public void onSnowballLand(ProjectileHitEvent event) {
		Projectile projectile = event.getEntity();
		if(!(projectile instanceof Snowball)) return;

		Location blockLocation = projectile.getLocation();
		blockLocation.setY(0);

		Block changeBlock = blockLocation.getBlock();
		if(changeBlock.getType() != Material.STAINED_CLAY) return;
		GameSound.ACID_RAIN.play(changeBlock.getLocation());

		PixelParty.gameManager.floorManager.randomizeBlock(changeBlock);
	}

	public void spawnSnowball() {
		int min = -32;
		int max = 32;

		int xRand = (int) (Math.random() * (max - min) + min);
		int zRand = (int) (Math.random() * (max - min) + min);

		int yRand = (int) (Math.random() * 40 + 40);

		World world = CENTER.getWorld();
		Location spawnLoc = new Location(world, xRand, yRand, zRand);
		Snowball snowball = (Snowball) world.spawnEntity(spawnLoc, EntityType.SNOWBALL);
		snowball.setVelocity(new Vector(0, -1, 0));

	}

}
