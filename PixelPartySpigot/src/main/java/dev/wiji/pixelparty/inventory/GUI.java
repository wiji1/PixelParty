package dev.wiji.pixelparty.inventory;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class GUI {
	public Player player;

	private InventoryPanel homePanel;
	private final Map<String, InventoryPanel> panelMap = new HashMap<>();

	public GUI(Player player) {
		this.player = player;
	}

	public void setHomePanel(InventoryPanel homePanel) {

		this.homePanel = homePanel;
	}

	public void addPanel(String refName, InventoryPanel panel) {

		panelMap.put(refName, panel);
	}

	public void open() {

		if(homePanel != null) player.openInventory(homePanel.getInventory());
	}

	public InventoryPanel getHomePanel() {

		return homePanel;
	}

	public InventoryPanel getPanel(String refName) {

		for(Map.Entry<String, InventoryPanel> entry : panelMap.entrySet()) {
			if(entry.getKey().equalsIgnoreCase(refName)) return entry.getValue();
		}
		return homePanel;
	}
}
