package dev.wiji.pixelparty.inventory;

import dev.wiji.pixelparty.PixelParty;
import dev.wiji.pixelparty.enums.GameSound;
import dev.wiji.pixelparty.objects.Floor;
import dev.wiji.pixelparty.util.Misc;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FloorSelectGUI extends GUI {
	public FloorSelectPanel panel;

	public FloorSelectGUI(Player player) {
		super(player);

		panel = new FloorSelectPanel(this);

		setHomePanel(panel);
	}

	public static class FloorSelectPanel extends InventoryPanel {

		public FloorSelectPanel(GUI gui) {
			super(gui);

			setItems();
		}

		public void setItems() {
			for(int i = 0; i < PixelParty.gameManager.floorManager.floors.size(); i++) {
				Floor floor = PixelParty.gameManager.floorManager.floors.get(i);

				boolean isSelected = PixelParty.gameManager.practiceProfile.getSelectedFloors().contains(floor.name);

				ItemStack item = new ItemStack(Material.MAP);
				ItemMeta meta = item.getItemMeta();

				if(isSelected) {
					meta.addEnchant(Enchantment.DURABILITY, 1, true);
					meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
				}

				meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

				ChatColor color = isSelected ? ChatColor.GREEN : ChatColor.RED;

				meta.setDisplayName(color + floor.name);
				List<String> lore = new ArrayList<>();
				lore.add("");
				lore.add(Misc.color("&eClick to toggle!"));

				meta.setLore(lore);
				item.setItemMeta(meta);

				addTaggedItem(i, () -> item, (event) -> {
					boolean stillSelected = PixelParty.gameManager.practiceProfile.getSelectedFloors().contains(floor.name);
					if(stillSelected) PixelParty.gameManager.practiceProfile.getSelectedFloors().remove(floor.name);
					else PixelParty.gameManager.practiceProfile.getSelectedFloors().add(floor.name);
					setItems();

					GameSound.CLICK.play(player);
				}).setItem();

				ItemStack selectAll = new ItemStack(Material.STAINED_CLAY, 1, (byte) 5);
				ItemMeta selectAllMeta = selectAll.getItemMeta();
				selectAllMeta.setDisplayName(ChatColor.GREEN + "Select All");
				selectAll.setItemMeta(selectAllMeta);

				addTaggedItem(getInventory().getSize() - 7, () -> selectAll, (event) -> {
					PixelParty.gameManager.practiceProfile.getSelectedFloors().clear();
					PixelParty.gameManager.practiceProfile.getSelectedFloors().addAll(
							PixelParty.gameManager.floorManager.floors.stream().map(floor1 -> floor1.name).collect(Collectors.toList()));
					GameSound.SUCCESS.play(player);
					setItems();
				}).setItem();

				ItemStack deselectAll = new ItemStack(Material.STAINED_CLAY, 1, (byte) 14);
				ItemMeta deselectAllMeta = deselectAll.getItemMeta();
				deselectAllMeta.setDisplayName(ChatColor.RED + "Deselect All");
				deselectAll.setItemMeta(deselectAllMeta);

				addTaggedItem(getInventory().getSize() - 3, () -> deselectAll, (event) -> {
					PixelParty.gameManager.practiceProfile.getSelectedFloors().clear();
					GameSound.SUCCESS.play(player);
					setItems();
				}).setItem();

				for(int j = getInventory().getSize() - 18; j < getInventory().getSize() - 9; j++) {
					ItemStack filler = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) 7);
					ItemMeta fillerMeta = filler.getItemMeta();
					fillerMeta.setDisplayName(" ");
					filler.setItemMeta(fillerMeta);
					getInventory().setItem(j, filler);
				}
			}
		}

		@Override
		public String getName() {
			return "Select Floor";
		}

		@Override
		public int getRows() {
			return (int) Math.ceil((double) PixelParty.gameManager.floorManager.floors.size() / 9) + 2;
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


	}
}
