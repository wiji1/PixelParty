package dev.wiji.pixelparty.inventory;

import dev.wiji.pixelparty.controllers.LobbyManager;
import dev.wiji.pixelparty.enums.LeaderboardStatistic;
import dev.wiji.pixelparty.enums.LeaderboardType;
import dev.wiji.pixelparty.enums.ServerType;
import dev.wiji.pixelparty.messaging.PluginMessage;
import dev.wiji.pixelparty.playerdata.PixelPlayer;
import dev.wiji.pixelparty.util.Misc;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

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

			PixelPlayer pixelPlayer = PixelPlayer.getPixelPlayer(player);

			ItemStack normal = new ItemStack(Material.GOLD_RECORD);
			ItemMeta normalMeta = normal.getItemMeta();
			normalMeta.addItemFlags(ItemFlag.values());
			normalMeta.setDisplayName(Misc.color("&eNormal Queue"));
			
			List<String> normalLore = new ArrayList<>();
			normalLore.add("");
			normalLore.add(Misc.color("&7ELO: &e" + pixelPlayer.getLeaderboardStat(LeaderboardType.LIFETIME, LeaderboardStatistic.NORMAL_ELO)));
			normalLore.add("");
			normalLore.add(Misc.color("&eClick to play!"));
			
			normalMeta.setLore(normalLore);
			normal.setItemMeta(normalMeta);

			addTaggedItem(11, () -> normal, event -> {
				sendQueueMessage((Player) event.getWhoClicked(), ServerType.NORMAL);
			}).setItem();

			ItemStack hyper = new ItemStack(Material.RECORD_3);
			ItemMeta hyperMeta = hyper.getItemMeta();
			hyperMeta.addItemFlags(ItemFlag.values());
			hyperMeta.setDisplayName(Misc.color("&dHyper Queue"));

			List<String> hyperLore = new ArrayList<>();
			hyperLore.add("");
			hyperLore.add(Misc.color("&7ELO: &e" + pixelPlayer.getLeaderboardStat(LeaderboardType.LIFETIME, LeaderboardStatistic.HYPER_ELO)));
			hyperLore.add("");
			hyperLore.add(Misc.color("&eClick to play!"));

			hyperMeta.setLore(hyperLore);
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
