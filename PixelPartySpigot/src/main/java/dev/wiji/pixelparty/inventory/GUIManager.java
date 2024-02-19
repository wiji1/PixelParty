package dev.wiji.pixelparty.inventory;

import de.tr7zw.nbtapi.NBTItem;
import dev.wiji.pixelparty.PixelParty;
import dev.wiji.pixelparty.enums.NBTTag;
import dev.wiji.pixelparty.util.Misc;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;

public class GUIManager implements Listener {
	private static ItemStack back;
	private static ItemStack previousPage;
	private static ItemStack nextPage;

	public static void setDefaultItemStacks(ItemStack back, ItemStack previousPage, ItemStack nextPage) {
		GUIManager.back = back;
		GUIManager.previousPage = previousPage;
		GUIManager.nextPage = nextPage;
	}

	public static ItemStack getBackItemStack() {
		if(back == null)
			throw new RuntimeException("Back item is null. Set it with AGUIManager.setDefaultItemStacks()");
		return back.clone();
	}

	public static ItemStack getPreviousPageItemStack() {
		if(previousPage == null)
			throw new RuntimeException("Previous page item is null. Set it with AGUIManager.setDefaultItemStacks()");
		return previousPage.clone();
	}

	public static ItemStack getNextPageItemStack() {
		if(nextPage == null)
			throw new RuntimeException("Next page item is null. Set it with AGUIManager.setDefaultItemStacks()");
		return nextPage.clone();
	}

	@EventHandler
	private static void onClick(InventoryClickEvent event) {
		InventoryHolder holder = event.getInventory().getHolder();
		if(!(holder instanceof InventoryPanel)) return;
		InventoryPanel guiPanel = (InventoryPanel) holder;
		if(guiPanel.cancelClicks) event.setCancelled(true);

		Player player = (Player) event.getWhoClicked();
		if(event.getClickedInventory() == null || event.getCurrentItem() == null) return;

		guiPanel.onClick(event);

		ItemStack itemStack = event.getCurrentItem();
		if(Misc.isAirOrNull(itemStack)) return;
		NBTItem nbtItem = new NBTItem(itemStack);
		if(!nbtItem.hasKey(NBTTag.ITEM_TAG.getRef())) return;
		String tag = nbtItem.getString(NBTTag.ITEM_TAG.getRef());
		for(Map.Entry<String, InventoryPanel.TaggedItem> entry : guiPanel.taggedItemMap.entrySet()) {
			String testTag = entry.getKey();
			InventoryPanel.TaggedItem taggedItem = entry.getValue();
			if(!tag.equals(testTag)) continue;
			if(taggedItem.getCallback() != null) taggedItem.getCallback().accept(event);
			break;
		}
	}

	@EventHandler
	private static void onOpen(InventoryOpenEvent event) {
		InventoryHolder holder = event.getInventory().getHolder();
		if(!(holder instanceof InventoryPanel)) return;
		((InventoryPanel) holder).onOpen(event);
		((Player) event.getPlayer()).updateInventory();
	}

	@EventHandler
	private static void onClose(InventoryCloseEvent event) {
		InventoryHolder holder = event.getInventory().getHolder();
		if(!(holder instanceof InventoryPanel)) return;
		((InventoryPanel) holder).onClose(event);
		((Player) event.getPlayer()).updateInventory();

		new BukkitRunnable() {
			@Override
			public void run() {
				((Player) event.getPlayer()).updateInventory();
			}
		}.runTaskLater(PixelParty.INSTANCE, 1L);
	}
}
