package dev.wiji.pixelparty.inventory;

import dev.wiji.pixelparty.messaging.PluginMessage;
import org.bukkit.entity.Player;

public class ServerSelectorGUI extends GUI {
	PracticeSelectorPanel practiceSelectorPanel;

	public ServerSelectorGUI(Player player, PluginMessage data) {
		super(player);

		practiceSelectorPanel = new PracticeSelectorPanel(this, data);
		setHomePanel(practiceSelectorPanel);
	}
}
