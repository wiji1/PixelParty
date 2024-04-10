package dev.wiji.pixelparty.powerups;

import de.tr7zw.nbtapi.NBTItem;
import dev.wiji.pixelparty.PixelParty;
import dev.wiji.pixelparty.enums.NBTTag;
import dev.wiji.pixelparty.objects.PowerUp;
import dev.wiji.pixelparty.util.Misc;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class SpeedPotion extends PowerUp {

	public SpeedPotion() {
		super("SpeedPotion");
	}

	@Override
	public void onActivate(Player player, Location location) {
		Potion potion = new Potion(PotionType.SPEED, 2);
		potion.setSplash(true);

		ItemStack potionItem = potion.toItemStack(1);
		ItemMeta meta = potionItem.getItemMeta();
		meta.setDisplayName(Misc.color("&bSpeed Boost"));
		meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);

		List<String> lore = new ArrayList<>();
		lore.add(Misc.color("&7Gives you and nearby players a"));
		lore.add(Misc.color("&7speed boost for 30 seconds!"));

		meta.setLore(lore);
		potionItem.setItemMeta(meta);

		NBTItem nbtItem = new NBTItem(potionItem, true);
		nbtItem.setBoolean(NBTTag.SPEED_POTION.getRef(), true);

		player.getInventory().addItem(potionItem);
	}

	@EventHandler
	public void onPotionSplash(PotionSplashEvent event) {
		ItemStack potion = event.getPotion().getItem();

		NBTItem nbtItem = new NBTItem(potion);
		if(!nbtItem.hasKey(NBTTag.SPEED_POTION.getRef())) return;
		event.setCancelled(true);

		for(LivingEntity player : event.getAffectedEntities()) {
			player.removePotionEffect(PotionEffectType.SPEED);
			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 30, 2));
			reApplySpeed((Player) player);
		}
	}

	public void reApplySpeed(Player player) {
		new BukkitRunnable() {
			@Override
			public void run() {
				player.removePotionEffect(PotionEffectType.SPEED);
				player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
			}
		}.runTaskLater(PixelParty.INSTANCE, 30 * 20);
	}
}
