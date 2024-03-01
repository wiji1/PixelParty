package dev.wiji.pixelparty.inventory;

import dev.wiji.pixelparty.PixelParty;
import dev.wiji.pixelparty.enums.GameSound;
import dev.wiji.pixelparty.enums.Skin;
import dev.wiji.pixelparty.objects.PracticeProfile;
import dev.wiji.pixelparty.util.MetaDataUtil;
import dev.wiji.pixelparty.util.Misc;
import dev.wiji.pixelparty.util.SkinUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class PrivacyGUI extends GUI{
	public PrivacyPanel panel;

	public PrivacyGUI(Player player) {
		super(player);

		panel = new PrivacyPanel(this);
		setHomePanel(panel);
	}

	public static class PrivacyPanel extends InventoryPanel {

		int currentSlot;

		public PrivacyPanel(GUI gui) {
			super(gui);

			setItems();
		}

		public void setItems() {
			currentSlot = 10;

			PracticeProfile profile = PixelParty.gameManager.practiceProfile;
			List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
			players.remove(player);

			for(Player player : players) {
				ItemStack head = MetaDataUtil.getPlayerSkull(MetaDataUtil.getCachedSkin(player.getUniqueId()));

				boolean manager = profile.getManagerList().contains(player.getUniqueId());
				ChatColor color = manager ? ChatColor.GREEN : ChatColor.GRAY;

				ItemMeta meta = head.getItemMeta();
				meta.setDisplayName(MetaDataUtil.getNameAndRank(player));
				List<String> lore = new ArrayList<>();
				lore.add("");
				lore.add(Misc.color("&7Role: " + color + (manager ? "Manager" : "Visitor")));
				lore.add("");
				lore.add(Misc.color("&eClick to toggle role!"));
				meta.setLore(lore);
				head.setItemMeta(meta);

				addTaggedItem(getSlot(), () -> head, (event) -> {
					boolean stillManager = profile.getManagerList().contains(player.getUniqueId());
					if(stillManager) profile.getManagerList().remove(player.getUniqueId());
					else profile.getManagerList().add(player.getUniqueId());
					GameSound.SUCCESS.play(player);
					setItems();
				}).setItem();

			}

			ItemStack visibility = MetaDataUtil.getPlayerSkull(Skin.PRIVACY);
			ItemMeta meta = visibility.getItemMeta();
			meta.setDisplayName(Misc.color("&eServer Visibility"));
			List<String> lore = new ArrayList<>();
			lore.add("");
			lore.add(Misc.color("&7Visibility: " + (profile.isPrivate() ? "&cPrivate" : "&aPublic")));
			lore.add("");
			lore.add(Misc.color("&eClick to toggle!"));
			meta.setLore(lore);
			visibility.setItemMeta(meta);

			addTaggedItem(getInventory().getSize() - (5 + (players.size() == 0 ? 9 : 0)), () -> visibility, (event) -> {
				GameSound.SUCCESS.play(player);
				profile.setPrivate(!profile.isPrivate());
				setItems();
			}).setItem();
		}

		@Override
		public String getName() {
			return "Privacy Settings";
		}

		@Override
		public int getRows() {
			int players = Bukkit.getOnlinePlayers().size() - 1;
			return players == 0 ? 3 : (int) (Math.ceil(players / 7D) + 3);
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

		public int getSlot() {
			int toReturn = currentSlot;
			currentSlot++;
			if(currentSlot % 9 == 8) currentSlot += 2;

			return toReturn;
		}
	}
}
