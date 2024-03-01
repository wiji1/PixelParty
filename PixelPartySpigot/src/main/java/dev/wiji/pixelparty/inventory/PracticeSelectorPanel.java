package dev.wiji.pixelparty.inventory;

import dev.wiji.pixelparty.controllers.LobbyManager;
import dev.wiji.pixelparty.enums.ServerType;
import dev.wiji.pixelparty.messaging.PluginMessage;
import dev.wiji.pixelparty.util.MetaDataUtil;
import dev.wiji.pixelparty.util.Misc;
import dev.wiji.pixelparty.util.SkinUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class PracticeSelectorPanel extends InventoryPanel{

	public PluginMessage serverData;

	public PracticeSelectorPanel(GUI gui, PluginMessage serverData) {
		super(gui, true);

		this.serverData = serverData;

		for(String string : serverData.getStrings()) {
			String[] split = string.split("/");
			String serverName = split[0];
			UUID owner = UUID.fromString(split[1]);

			PluginMessage request = new PluginMessage().setIntendedServer(serverName);
			request.writeString("REQUEST TO JOIN").writeString(player.getUniqueId().toString());
			request.request((pluginMessage -> {
				boolean canJoin = pluginMessage.getBooleans().get(0);
				ChatColor color = canJoin ? ChatColor.GREEN : ChatColor.RED;
				int players = pluginMessage.getIntegers().get(0);

				ItemStack head = MetaDataUtil.getPlayerSkull(Objects.requireNonNull(MetaDataUtil.getCachedSkin(owner)));
				SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
				skullMeta.setDisplayName(color + serverName);

				List<String> lore = new ArrayList<>();
				lore.add(Misc.color("&7Owner: ") + MetaDataUtil.getDisplayName(owner));
				lore.add("");
				lore.add(Misc.color("&7Players: &e" + players));
				lore.add("");
				lore.add(color + (canJoin ? "Click to join" : "Server is private!"));

				skullMeta.setLore(lore);
				head.setItemMeta(skullMeta);

				addTaggedItem(getSlot(), () -> head, (event) -> {
					if(!canJoin) {
						player.sendMessage(Misc.color("&cThis server is private!"));
						//TODO: Play sound
						return;
					}

					PluginMessage joinRequest = new PluginMessage();
					joinRequest.writeString("JOIN SERVER").writeString(serverName).writeString(player.getUniqueId().toString());
					joinRequest.send();
				}).setItem();
			}));
		}

		int serversLeft = serverData.getIntegers().get(0) - serverData.getStrings().size();

		ItemStack createServer = new ItemStack(Material.STAINED_CLAY, 1, (byte) 5);
		ItemMeta createServerMeta = createServer.getItemMeta();
		ChatColor color = serversLeft == 0 ? ChatColor.RED : ChatColor.GREEN;
		createServerMeta.setDisplayName(Misc.color(color + "Create Server"));
		List<String> lore = new ArrayList<>();
		lore.add(Misc.color("&7Create your own practice server"));
		lore.add("");
		lore.add(Misc.color("&7Available Servers: " + color + serversLeft));
		lore.add("");
		lore.add(color + (serversLeft == 0 ? "No servers available" : "Click to create"));
		createServerMeta.setLore(lore);
		createServer.setItemMeta(createServerMeta);

		buildInventory();

		addTaggedItem(getInventory().getSize() - (5 + (serverData.getStrings().size() == 0 ? 9 : 0)), () -> createServer, (event) -> {
			LobbyManager.queuePlayer(player, ServerType.PRACTICE);
		}).setItem();
	}

	int currentSlot = 10;
	public int getSlot() {
		int toReturn = currentSlot;
		currentSlot++;
		if(currentSlot % 9 == 8) currentSlot += 2;

		return toReturn;
	}

	@Override
	public String getName() {
		return "Practice Servers";
	}

	@Override
	public int getRows() {
		int items = serverData.getStrings().size();

		return items == 0 ? 3 : (int) (Math.ceil(items / 7D) + 3);
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
