package dev.wiji.pixelparty.powerups;

import dev.wiji.pixelparty.objects.PowerUp;
import dev.wiji.pixelparty.util.Misc;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Hunger extends PowerUp {

	List<Player> hungerPlayers = new ArrayList<>();

	public Hunger() {
		super("Hunger");
	}

	@Override
	public void onActivate(Player player, Location location) {
		hungerPlayers.add(player);
		player.setFoodLevel(3);
		player.sendMessage(Misc.color("&cYou'll have no hunger until the next round!"));
	}

	@Override
	public void onRoundEnd() {
		for(Player hungerPlayer : hungerPlayers) {
			hungerPlayer.setFoodLevel(20);
		}
		hungerPlayers.clear();
	}
}
