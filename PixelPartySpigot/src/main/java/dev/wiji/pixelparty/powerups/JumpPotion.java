package dev.wiji.pixelparty.powerups;

import de.tr7zw.nbtapi.NBTItem;
import dev.wiji.pixelparty.enums.NBTTag;
import dev.wiji.pixelparty.objects.PowerUp;
import dev.wiji.pixelparty.util.Misc;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.List;

public class JumpPotion extends PowerUp {

	@Override
	public void onActivate(Player player, Location location) {
		Potion potion = new Potion(PotionType.JUMP, 2);
		potion.setSplash(true);

		ItemStack potionItem = potion.toItemStack(1);
		ItemMeta meta = potionItem.getItemMeta();
		meta.setDisplayName(Misc.color("&a&lJump Boost"));

		List<String> lore = new ArrayList<>();
		lore.add(Misc.color("&7Gives you and nearby players a"));
		lore.add(Misc.color("&7jump boost for 30 seconds!"));

		meta.setLore(lore);
		potionItem.setItemMeta(meta);

		NBTItem nbtItem = new NBTItem(potionItem, true);
		nbtItem.setBoolean(NBTTag.JUMP_POTION.getRef(), true);

		player.getInventory().addItem(potionItem);
	}

	@EventHandler
	public void onPotionSplash(PotionSplashEvent event) {
		ItemStack potion = event.getPotion().getItem();

		NBTItem nbtItem = new NBTItem(potion);
		if(!nbtItem.hasKey(NBTTag.JUMP_POTION.getRef())) return;
		event.setCancelled(true);

		for(LivingEntity player : event.getAffectedEntities()) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 20 * 30, 1));
		}
	}
}
