package dev.wiji.pixelparty.powerups;

import dev.wiji.pixelparty.objects.PowerUp;
import dev.wiji.pixelparty.util.Misc;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class Pumpkin extends PowerUp {
	public List<Player> pumpkinPlayers = new ArrayList<>();

	public Pumpkin() {
		super("Pumpkin");
	}

	@Override
	public void onActivate(Player player, Location location) {
		pumpkinPlayers.add(player);

		ItemStack pumpkin = new ItemStack(Material.PUMPKIN);
		ItemMeta pumpkinMeta = pumpkin.getItemMeta();
		pumpkinMeta.setDisplayName(Misc.color("&6Cursed Pumpkin"));
		List<String> lore = new ArrayList<>();
		lore.add(Misc.color("&7Will be removed when the next"));
		lore.add(Misc.color("&7round starts."));
		pumpkinMeta.setLore(lore);
		pumpkin.setItemMeta(pumpkinMeta);

		player.getInventory().setHelmet(pumpkin);
		player.sendMessage(Misc.color("&cYou'll wear a pumpkin on your head until the next round!"));
	}

	@Override
	public void onRoundEnd() {
		for(Player pumpkinPlayer : pumpkinPlayers) {
			pumpkinPlayer.getInventory().setHelmet(new ItemStack(Material.AIR));
		}

		pumpkinPlayers.clear();
	}
}
