package dev.wiji.pixelparty.powerups;

import de.tr7zw.nbtapi.NBTItem;
import dev.wiji.pixelparty.enums.NBTTag;
import dev.wiji.pixelparty.objects.PowerUp;
import dev.wiji.pixelparty.util.Misc;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class EnderPearl extends PowerUp {

	@Override
	public void onActivate(Player player, Location location) {
		ItemStack pearl = new ItemStack(Material.ENDER_PEARL);
		ItemMeta meta = pearl.getItemMeta();
		meta.setDisplayName(Misc.color("&d&lEnder Pearl"));

		List<String> lore = new ArrayList<>();
		lore.add(Misc.color("&7Throw this ender pearl on the"));
		lore.add(Misc.color("&7ground when you're falling to be"));
		lore.add(Misc.color("&7teleported back onto the dance"));
		lore.add(Misc.color("&7floor!"));

		meta.setLore(lore);
		pearl.setItemMeta(meta);

		NBTItem nbtItem = new NBTItem(pearl, true);
		nbtItem.setBoolean(NBTTag.ENDER_PEARL.getRef(), true);

		player.getInventory().addItem(nbtItem.getItem());
	}

	@EventHandler
	public void onEnderPearlLand(ProjectileHitEvent event) {
		Projectile pearl = event.getEntity();

		if(!(pearl instanceof org.bukkit.entity.EnderPearl) || !(pearl.getShooter() instanceof Player)) return;
		Player player = (Player) pearl.getShooter();

		 if(!isInBounds(pearl.getLocation())) return;

		 Location teleportLoc = getHitBlockCenter(pearl.getLocation(), pearl.getVelocity());
		 teleportLoc.setYaw(player.getLocation().getYaw());
		 teleportLoc.setPitch(0);

		 player.teleport(teleportLoc);
	}

	@EventHandler
	public void onTeleport(PlayerTeleportEvent event) {
		if(event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
			event.setCancelled(true);
		}
	}

	public boolean isInBounds(Location location) {
		return !(Math.abs(location.getX()) > 34 || Math.abs(location.getZ()) > 34);
	}

	public static Location getHitBlockCenter(Location location, Vector vector) {

		return new Location(location.getWorld(), location.getX() + vector.getX(), 1,
				location.getZ() + vector.getZ());
	}
}
