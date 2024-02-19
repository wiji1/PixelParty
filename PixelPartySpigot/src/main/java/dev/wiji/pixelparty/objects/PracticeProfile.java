package dev.wiji.pixelparty.objects;

import dev.wiji.pixelparty.PixelParty;
import dev.wiji.pixelparty.controllers.PracticeManager;
import dev.wiji.pixelparty.enums.ServerType;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static dev.wiji.pixelparty.controllers.AmbienceManager.CENTER;

public class PracticeProfile {

	private int round = 25;
	private boolean isPrivate = false;
	private List<String> selectedFloors = new ArrayList<>();
	private boolean powerups = true;
	private boolean paused = false;
	private final List<UUID> managerList = new ArrayList<>();

	public PracticeProfile() {
		if(PixelParty.serverType != ServerType.PRACTICE) return;
	}

	public int getRound() {
		return round;
	}

	public Location getSpawnLocation(Player player) {

		if(PracticeManager.spawnMap.containsKey(player.getUniqueId())) {
			Location location = PracticeManager.spawnMap.get(player.getUniqueId()).getBukkitEntity().getLocation().clone();
			location.setY(1);
			return location;
		}

		return CENTER;
	}

	public boolean isPrivate() {
		return isPrivate;
	}

	public List<String> getSelectedFloors() {
		return selectedFloors;
	}

	public boolean powerupsEnabled() {
		return powerups;
	}

	public boolean isPaused() {
		return paused;
	}

	public void setRound(int round) {
		this.round = round;
	}

	public void setPrivate(boolean isPrivate) {
		this.isPrivate = isPrivate;
	}

	public void setSelectedFloors(List<String> selectedFloors) {
		this.selectedFloors = selectedFloors;
	}

	public void setPowerupsEnabled(boolean powerups) {
		this.powerups = powerups;
	}

	public void setPaused(boolean paused) {
		this.paused = paused;
	}

	public List<UUID> getManagerList() {
		return managerList;
	}

	public void addManager(UUID uuid) {
		managerList.add(uuid);
	}

	public void removeManager(UUID uuid) {
		managerList.remove(uuid);
	}
}
