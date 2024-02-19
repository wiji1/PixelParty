package dev.wiji.pixelparty.inventory;

import dev.wiji.pixelparty.util.Misc;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;

public class SpectatorGUI extends GUI {
	public SpectatorPanel spectatorPanel;

	public SpectatorGUI(Player player) {
		super(player);

		spectatorPanel = new SpectatorPanel(this);
		setHomePanel(spectatorPanel);

	}

	public static class SpectatorPanel extends InventoryPanel {

		public SpectatorPanel(GUI gui) {
			super(gui);

			addItems();
		}

		@Override
		public String getName() {
			return "Spectator Settings";
		}

		@Override
		public int getRows() {
			return 4;
		}

		@Override
		public void onClick(InventoryClickEvent event) {

		}

		@Override
		public void onOpen(InventoryOpenEvent event) {
		}

		@Override
		public void onClose(InventoryCloseEvent event) {

		}

		public void addItems() {
			Material[] boots = {Material.LEATHER_BOOTS, Material.CHAINMAIL_BOOTS, Material.IRON_BOOTS, Material.GOLD_BOOTS, Material.DIAMOND_BOOTS};

			for(int i = 0; i < boots.length; i++) {

				ItemStack item = new ItemStack(boots[i]);
				ItemMeta meta = item.getItemMeta();
				meta.setDisplayName(Misc.color(i == 0 ? "&aNo Speed" : ("&aSpeed " + Misc.toRoman(i))));
				item.setItemMeta(meta);

				int speedIndex = i;
				addTaggedItem(11 + i, () -> item, event -> {
					player.removePotionEffect(PotionEffectType.SPEED);
					if(speedIndex != 0) player.addPotionEffect(PotionEffectType.SPEED.createEffect(Integer.MAX_VALUE, speedIndex - 1));

					if(speedIndex == 0) player.sendMessage(Misc.color("&cYou no longer have any speed effects!"));
					else player.sendMessage(Misc.color("&aYou now have Speed " + Misc.toRoman(speedIndex) + "!"));

					player.closeInventory();
				}).setItem();
			}

			ItemStack close = new ItemStack(Material.BARRIER);
			ItemMeta backMeta = close.getItemMeta();
			backMeta.setDisplayName(Misc.color("&cClose"));
			close.setItemMeta(backMeta);

			addTaggedItem(31, () -> close, (clickEvent) -> player.closeInventory()).setItem();
		}
	}
}
