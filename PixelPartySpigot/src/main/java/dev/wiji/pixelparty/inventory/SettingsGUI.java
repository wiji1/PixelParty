package dev.wiji.pixelparty.inventory;

import dev.wiji.pixelparty.enums.GameSound;
import dev.wiji.pixelparty.playerdata.PixelPlayer;
import dev.wiji.pixelparty.util.Misc;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class SettingsGUI extends GUI {
	public SettingsPanel panel;

	public SettingsGUI(Player player) {
		super(player);

		panel = new SettingsPanel(this);
		setHomePanel(panel);
	}

	public static class SettingsPanel extends InventoryPanel {
		PixelPlayer pixelPlayer = PixelPlayer.getPixelPlayer(gui.player);

		public SettingsPanel(GUI gui) {
			super(gui);

			placeItems();
		}

		@Override
		public String getName() {
			return "Settings";
		}

		@Override
		public int getRows() {
			return 3;
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

		public void placeItems() {
			ItemStack wool = new ItemStack(Material.WOOL, 1, (short) 3);
			ItemMeta woolMeta = wool.getItemMeta();
			woolMeta.setDisplayName(Misc.color("&bWool Floors"));

			List<String> lore = new ArrayList<>();
			lore.add("");
			lore.add(Misc.color("&7Status: " + (pixelPlayer.woolFloor ? "&aEnabled" : "&cDisabled")));
			lore.add("");
			lore.add(Misc.color("&eClick to toggle!"));

			woolMeta.setLore(lore);
			wool.setItemMeta(woolMeta);

			addTaggedItem(13, () -> wool, (event) -> {
				pixelPlayer.woolFloor = !pixelPlayer.woolFloor;
				GameSound.SUCCESS.play(gui.player);
				pixelPlayer.save();
				placeItems();
			}).setItem();
		}

	}
}
