package dev.wiji.pixelparty.powerups;

import de.tr7zw.nbtapi.NBTItem;
import dev.wiji.pixelparty.controllers.CooldownManager;
import dev.wiji.pixelparty.enums.CooldownType;
import dev.wiji.pixelparty.enums.NBTTag;
import dev.wiji.pixelparty.objects.PowerUp;
import dev.wiji.pixelparty.util.Misc;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class LeapFeather extends PowerUp {

	@Override
	public void onActivate(Player player, Location location) {
		ItemStack feather = new ItemStack(Material.FEATHER);
		ItemMeta meta = feather.getItemMeta();
		meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
		meta.setDisplayName(Misc.color("&f&lLeap Feather"));

		List<String> lore = new ArrayList<>();
		lore.add(Misc.color("&7Fling yourself in the"));
		lore.add(Misc.color("&7direction you're looking at the"));
		lore.add(Misc.color("&7press of a button!"));

		meta.setLore(lore);
		feather.setItemMeta(meta);

		NBTItem nbtItem = new NBTItem(feather, true);
		nbtItem.setBoolean(NBTTag.FEATHER.getRef(), true);

		player.getInventory().addItem(nbtItem.getItem());
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		ItemStack item = player.getInventory().getItemInHand();

		if(event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		if(CooldownManager.isOnCooldown(player, CooldownType.ITEM_USE)) return;
		if(Misc.isAirOrNull(item)) return;

		NBTItem nbtItem = new NBTItem(item);
		if(!nbtItem.hasKey(NBTTag.FEATHER.getRef())) return;

		player.setVelocity(player.getLocation().getDirection().multiply(2).add(new Vector(0, 0.2, 0)));

		if(item.getAmount() > 1) item.setAmount(item.getAmount() - 1);
		else player.getInventory().remove(item);

		CooldownManager.addCooldown(player, CooldownType.ITEM_USE, 5);
	}
}
