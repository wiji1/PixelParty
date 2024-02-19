package dev.wiji.pixelparty.inventory;

import com.mojang.authlib.GameProfile;
import dev.wiji.pixelparty.PixelParty;
import dev.wiji.pixelparty.util.Misc;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class TeleporterGUI extends GUI {
	TeleporterPanel playerTrackerPanel;

	public TeleporterGUI(Player player) {
		super(player);

		playerTrackerPanel = new TeleporterPanel(this);
		setHomePanel(playerTrackerPanel);
	}

	public static class TeleporterPanel extends InventoryPanel {
		BukkitTask task;
		int currentPlayers;

		public TeleporterPanel(GUI gui) {
			super(gui);

			currentPlayers = PixelParty.gameManager.alivePlayers.size();

			placeItems();
		}

		@Override
		public String getName() {
			return "Teleporter";
		}

		@Override
		public int getRows() {
			return (int) Math.ceil(PixelParty.gameManager.alivePlayers.size() / 7D) + 2;
		}

		@Override
		public void onClick(InventoryClickEvent event) {

		}

		@Override
		public void onOpen(InventoryOpenEvent event) {
			placeItems();

			task = new BukkitRunnable() {
				@Override
				public void run() {
					if(currentPlayers == PixelParty.gameManager.alivePlayers.size()) return;
					update();
				}
			}.runTaskTimer(PixelParty.INSTANCE, 10, 10);
		}

		@Override
		public void onClose(InventoryCloseEvent event) {
			task.cancel();
		}

		public void update() {
			buildInventory();
		}

		public void placeItems() {
			int currentSlot = 10;

			for(UUID aliveUUID : PixelParty.gameManager.alivePlayers) {
				Player alivePlayer = Misc.getPlayer(aliveUUID);

				if(alivePlayer == player) continue;
				assert alivePlayer != null;

				EntityPlayer nmsPlayer = ((CraftPlayer) alivePlayer).getHandle();
				GameProfile gameProfile = nmsPlayer.getProfile();

				ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
				SkullMeta headMeta = (SkullMeta) head.getItemMeta();
				headMeta.setDisplayName(Misc.getNameAndRank(alivePlayer));

				List<String> lore = new ArrayList<>(Collections.singletonList(Misc.color("&7Left-click to spectate!")));
				headMeta.setLore(lore);

				try {
					Field profileField = headMeta.getClass().getDeclaredField("profile");
					profileField.setAccessible(true);
					profileField.set(headMeta, gameProfile);

				} catch (IllegalArgumentException | NoSuchFieldException | SecurityException | IllegalAccessException error) {
					error.printStackTrace();
				}

				head.setItemMeta(headMeta);

				addTaggedItem(currentSlot, () -> head, (clickEvent) -> {
					SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
					Player teleportTo = Bukkit.getPlayer(skullMeta.getOwner());
					if(teleportTo == null || teleportTo.isOnline()) return;

					player.teleport(teleportTo);
				}).setItem();

				currentSlot++;
				if(currentSlot % 9 == 8) currentSlot += 2;
			}

			ItemStack close = new ItemStack(Material.BARRIER);
			ItemMeta backMeta = close.getItemMeta();
			backMeta.setDisplayName(Misc.color("&cClose"));
			close.setItemMeta(backMeta);

			addTaggedItem(getInventory().getSize() - 5, () -> close, (clickEvent) -> player.closeInventory()).setItem();
		}
	}
}
