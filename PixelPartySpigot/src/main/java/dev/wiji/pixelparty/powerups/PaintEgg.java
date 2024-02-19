package dev.wiji.pixelparty.powerups;

import de.tr7zw.nbtapi.NBTItem;
import dev.wiji.pixelparty.PixelParty;
import dev.wiji.pixelparty.controllers.FloorManager;
import dev.wiji.pixelparty.enums.GameSound;
import dev.wiji.pixelparty.enums.NBTTag;
import dev.wiji.pixelparty.objects.PowerUp;
import dev.wiji.pixelparty.util.Misc;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class PaintEgg extends PowerUp {

	public 	FloorManager floorManager = PixelParty.gameManager.floorManager;


	@Override
	public void onActivate(Player player, Location location) {
		ItemStack egg = new ItemStack(Material.EGG);
		ItemMeta meta = egg.getItemMeta();
		meta.setDisplayName(Misc.color("&b&lPaint-filled Egg"));

		List<String> lore = new ArrayList<>();
		lore.add(Misc.color("&7Throw this egg on the ground"));
		lore.add(Misc.color("&7to splatter paint!"));

		meta.setLore(lore);
		egg.setItemMeta(meta);

		NBTItem nbtItem = new NBTItem(egg, true);
		nbtItem.setBoolean(NBTTag.PAINT_EGG.getRef(), true);

		player.getInventory().addItem(nbtItem.getItem());
	}

	@EventHandler
	public void onEggLand(ProjectileHitEvent event) {
		Projectile egg = event.getEntity();

		if(!(egg instanceof Egg) || !(egg.getShooter() instanceof Player)) return;

		Location eggLoc = floorManager.getHitBlockCenter(egg.getLocation(), egg.getVelocity());
		eggLoc.setY(0);

		if(!floorManager.isOnFloor(eggLoc)) return;

		event.getEntity().getWorld().playEffect(eggLoc, Effect.EXPLOSION_HUGE, 1);
		GameSound.PAINT_EGG.play(eggLoc);

		floorManager.paint(eggLoc, floorManager.chosenColor);
	}

	@EventHandler
	public void onEggLand(EntitySpawnEvent event) {
		if(event.getEntity() instanceof Chicken) event.setCancelled(true);
	}
}
