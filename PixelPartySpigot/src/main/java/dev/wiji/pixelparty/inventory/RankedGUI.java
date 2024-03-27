package dev.wiji.pixelparty.inventory;

import dev.wiji.pixelparty.controllers.LobbyManager;
import dev.wiji.pixelparty.enums.ServerType;
import dev.wiji.pixelparty.messaging.PluginMessage;
import dev.wiji.pixelparty.util.Misc;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class RankedGUI extends GUI {

	InventoryPanel rankedPanel;

	public RankedGUI(Player player) {
		super(player);

		rankedPanel = new RankedPanel(this);
		setHomePanel(rankedPanel);
	}

	public static class RankedPanel extends InventoryPanel {

		public RankedPanel(GUI gui) {
			super(gui);

			ItemStack normal = new ItemStack(Material.GOLD_RECORD);
			ItemMeta normalMeta = normal.getItemMeta();
			normalMeta.setDisplayName(Misc.color("&eNormal Queue"));
			normal.setItemMeta(normalMeta);

			addTaggedItem(11, () -> normal, event -> {
				sendQueueMessage((Player) event.getWhoClicked(), ServerType.NORMAL);
			}).setItem();

			ItemStack hyper = new ItemStack(Material.RECORD_3);
			ItemMeta hyperMeta = hyper.getItemMeta();
			hyperMeta.setDisplayName(Misc.color("&dHyper Queue"));
			hyper.setItemMeta(hyperMeta);

			addTaggedItem(15, () -> hyper, event -> {
				sendQueueMessage((Player) event.getWhoClicked(), ServerType.HYPER);
			}).setItem();
		}

		public void sendQueueMessage(Player player, ServerType serverType) {
			PluginMessage message = new PluginMessage();
			message.writeString("RANKED PREFERENCE").writeString(player.getUniqueId().toString());
			message.writeString(serverType.name()).send();

			LobbyManager.queuePlayer(player, ServerType.RANKED);
		}

		@Override
		public String getName() {
			return "Ranked Queue";
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
	}
}
