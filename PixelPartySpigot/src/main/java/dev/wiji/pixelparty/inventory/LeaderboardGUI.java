package dev.wiji.pixelparty.inventory;

import dev.wiji.pixelparty.enums.GameSound;
import dev.wiji.pixelparty.enums.LeaderboardStatistic;
import dev.wiji.pixelparty.enums.LeaderboardType;
import dev.wiji.pixelparty.leaderboard.Leaderboard;
import dev.wiji.pixelparty.playerdata.PixelPlayer;
import dev.wiji.pixelparty.util.Misc;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LeaderboardGUI extends GUI {
	public LeaderboardPanel panel;

	public LeaderboardGUI(Player player, Leaderboard leaderboard) {
		super(player);

		panel = new LeaderboardPanel(this, leaderboard);
		setHomePanel(panel);
	}

	public static class LeaderboardPanel extends InventoryPanel {
		public LeaderboardType type;
		public LeaderboardStatistic statistic;

		public Leaderboard leaderboard;

		public LeaderboardPanel(GUI gui, Leaderboard leaderboard) {
			super(gui);

			this.leaderboard = leaderboard;

			PixelPlayer pixelPlayer = PixelPlayer.getPixelPlayer(gui.player);
			type = pixelPlayer.currentType;
			statistic = pixelPlayer.currentStatistic;

			LeaderboardStatistic defaultStatistic = leaderboard.defaultStatistic;
			LeaderboardType defaultType = leaderboard.defaultType;
			List<UUID> overriddenPlayers = leaderboard.overriddenPlayers;

			if(defaultStatistic != null && !overriddenPlayers.contains(player.getUniqueId())) statistic = defaultStatistic;
			if(defaultType != null && !overriddenPlayers.contains(player.getUniqueId())) type = defaultType;

			placeItems();
		}

		@Override
		public String getName() {
			return "Leaderboard Settings";
		}

		@Override
		public int getRows() {
			return 5;
		}

		@Override
		public void onClick(InventoryClickEvent event) {
			if(event.getClickedInventory().getHolder() != this) return;
			int slot = event.getSlot();

			if(slot == 11) {
				GameSound.CLICK.play(player);
				if(event.getClick() == ClickType.LEFT) {
					statistic = LeaderboardStatistic.values()[(statistic.ordinal() + 1) %
							LeaderboardStatistic.values().length];
				} else if(event.getClick() == ClickType.RIGHT) {
					statistic = LeaderboardStatistic.values()[(statistic.ordinal() - 1 +
							LeaderboardStatistic.values().length) % LeaderboardStatistic.values().length];
				}
				placeItems();
			} else if(slot == 15) {
				GameSound.CLICK.play(player);
				if(event.getClick() == ClickType.LEFT) {
					type = LeaderboardType.values()[(type.ordinal() + 1) % LeaderboardType.values().length];
				} else if(event.getClick() == ClickType.RIGHT) {
					type = LeaderboardType.values()[(type.ordinal() - 1 + LeaderboardType.values().length) %
							LeaderboardType.values().length];
				}
				placeItems();
			}
		}

		@Override
		public void onOpen(InventoryOpenEvent event) {

		}

		@Override
		public void onClose(InventoryCloseEvent event) {

		}

		public void placeItems() {
			ItemStack statisticItem = new ItemStack(Material.SLIME_BALL);
			ItemMeta statisticMeta = statisticItem.getItemMeta();
			statisticMeta.setDisplayName(Misc.color("&aSelect Statistic!"));
			List<String> statisticLore = new ArrayList<>();
			statisticLore.add("");
			statisticLore.add(Misc.color("  &7..."));
			for(LeaderboardStatistic stat : LeaderboardStatistic.values()) {
				StringBuilder sb = new StringBuilder();
				sb.append(stat == statistic ? "&a\u279F" : " ");
				sb.append(" &7").append(stat.name);
				statisticLore.add(Misc.color(sb.toString()));
			}
			statisticLore.add(Misc.color("  &7..."));
			statisticLore.add("");
			statisticLore.add(Misc.color("&eLeft/Right click to change!"));
			statisticMeta.setLore(statisticLore);
			statisticItem.setItemMeta(statisticMeta);

			getInventory().setItem(11, statisticItem);

			ItemStack typeItem = new ItemStack(Material.WATCH);
			ItemMeta typeMeta = typeItem.getItemMeta();
			typeMeta.setDisplayName(Misc.color("&aSelect Time!"));
			List<String> typeLore = new ArrayList<>();
			typeLore.add("");
			for(LeaderboardType type : LeaderboardType.values()) {
				String sb = (type == this.type ? "&a\u279F" : " ") +
						" &7" + type.displayName;
				typeLore.add(Misc.color(sb));
			}
			typeLore.add("");
			typeLore.add(Misc.color("&eLeft/Right click to change!"));
			typeMeta.setLore(typeLore);
			typeItem.setItemMeta(typeMeta);

			getInventory().setItem(15, typeItem);


			ItemStack confirmItem = new ItemStack(Material.STAINED_CLAY, 1, (byte) 13);
			ItemMeta confirmMeta = confirmItem.getItemMeta();
			confirmMeta.setDisplayName(Misc.color("&aApply changes"));
			confirmItem.setItemMeta(confirmMeta);

			addTaggedItem(30, () -> confirmItem, (event) -> {
				GameSound.SUCCESS.play(player);
				PixelPlayer pixelPlayer = PixelPlayer.getPixelPlayer(player);
				pixelPlayer.currentType = type;
				pixelPlayer.currentStatistic = statistic;
				pixelPlayer.save();
				if(!leaderboard.overriddenPlayers.contains(player.getUniqueId()))
					leaderboard.overriddenPlayers.add(player.getUniqueId());
				leaderboard.hologram.updateHologram(player);
				player.closeInventory();
			}).setItem();

			ItemStack cancelItem = new ItemStack(Material.STAINED_CLAY, 1, (byte) 14);
			ItemMeta cancelMeta = cancelItem.getItemMeta();
			cancelMeta.setDisplayName(Misc.color("&cDiscard changes"));
			cancelItem.setItemMeta(cancelMeta);

			addTaggedItem(32, () -> cancelItem, (event) -> {
				GameSound.ERROR.play(player);
				player.closeInventory();
			}).setItem();
		}
	}
}
