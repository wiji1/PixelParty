package dev.wiji.pixelparty.inventory;

import de.tr7zw.nbtapi.NBTItem;
import dev.wiji.pixelparty.enums.NBTTag;
import dev.wiji.pixelparty.util.Misc;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class InventoryPanel implements InventoryHolder {

	public Player player;
	public GUI gui;
	public InventoryPanel previousGUI;

	private Inventory inventory;
//	public AInventoryBuilder inventoryBuilder;

	public boolean cancelClicks = true;

	@Override
	public Inventory getInventory() {
		return inventory;
	}

	public final Map<String, TaggedItem> taggedItemMap = new HashMap<>();
	private TaggedItem backItem;

	public InventoryPanel(GUI gui) {
		this(gui, false);
	}

	public InventoryPanel(GUI gui, boolean lateBuild) {
		this.player = gui.player;
		this.gui = gui;

		if(!lateBuild) buildInventory();
	}

	public abstract String getName();
	public abstract int getRows();


	public abstract void onClick(InventoryClickEvent event);

	/**
	 * Called when the GUI is opened.
	 */
	public abstract void onOpen(InventoryOpenEvent event);

	/**
	 * Called when the GUI is closed.
	 */
	public abstract void onClose(InventoryCloseEvent event);

	public void openPanel(InventoryPanel guiPanel) {

		guiPanel.previousGUI = this;
		guiPanel.player.openInventory(guiPanel.getInventory());
	}

	public void openPreviousGUI() {
		if(previousGUI == null) return;

		previousGUI.player.openInventory(previousGUI.getInventory());
		previousGUI = null;
	}

	public void updateInventory() {

		player.updateInventory();
	}

	private static int getSlots(int rows) {

		return Math.max(Math.min(rows, 6), 1) * 9;
	}

	public void buildInventory() {
		boolean reOpen = inventory != null && player.getOpenInventory().getTopInventory().getHolder() == this;

		inventory = Bukkit.createInventory(this, getSlots(getRows()),
				ChatColor.translateAlternateColorCodes('&', getName()));
//		inventoryBuilder = new AInventoryBuilder(inventory);

		if(reOpen) player.openInventory(inventory);
	}

	public TaggedItem getGUIItem(String tag) {
		return taggedItemMap.get(tag);
	}

	public TaggedItem addTaggedItem(int slot, Supplier<ItemStack> itemStack, Consumer<InventoryClickEvent> callback) {
		UUID uuid = UUID.randomUUID();
		TaggedItem taggedItem = new TaggedItem(slot, uuid.toString(), itemStack, callback);
		taggedItemMap.put(uuid.toString(), taggedItem);
		return taggedItem;
	}

	public String getTagFromItem(ItemStack itemStack) {
		if(Misc.isAirOrNull(itemStack)) return null;
		NBTItem nbtItem = new NBTItem(itemStack, true);
		if(!nbtItem.hasKey(NBTTag.ITEM_TAG.getRef())) return null;
		return nbtItem.getString(NBTTag.ITEM_TAG.getRef());
	}

	public void addBackButton(int slot) {
		addBackButton(addTaggedItem(slot, GUIManager::getBackItemStack, event -> openPreviousGUI()));
	}

	public void addBackButton(TaggedItem taggedItem) {
		backItem = taggedItem;
	}

	public void setInventory() {
		if(backItem != null) backItem.setItem();
	}

	public class TaggedItem {
		private final int slot;
		private final String tag;
		private final Supplier<ItemStack> itemStack;
		private final Consumer<InventoryClickEvent> callback;

		public TaggedItem(String tag, Supplier<ItemStack> itemStack, Consumer<InventoryClickEvent> callback) {
			this(-1, tag, itemStack, callback);
		}

		public TaggedItem(int slot, String tag, Supplier<ItemStack> itemStack, Consumer<InventoryClickEvent> callback) {
			this.slot = slot;
			this.tag = tag;
			this.itemStack = itemStack;
			this.callback = callback;
		}

		public TaggedItem setItem() {
			if(slot == -1) return this;
			getInventory().setItem(slot, getTaggedItemStack());
			return this;
		}

		public TaggedItem removeItem() {
			if(slot == -1) return this;
			getInventory().setItem(slot, new ItemStack(Material.AIR));
			return this;
		}

		public String getTag() {
			return tag;
		}

		public ItemStack getNormalItemStack() {
			return itemStack.get().clone();
		}

		public ItemStack getTaggedItemStack() {
			ItemStack itemStack = getNormalItemStack();
			NBTItem nbtItem = new NBTItem(itemStack, true);
			nbtItem.setString(NBTTag.ITEM_TAG.getRef(), tag);
			return itemStack;
		}

		public Consumer<InventoryClickEvent> getCallback() {
			return callback;
		}
	}
}
