package dev.wiji.pixelparty.objects;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PixelPlayer {
	public static List<PixelPlayer> pixelPlayers = new ArrayList<>();

	public int volume = 50;
	public boolean pausedMusic = false;

	public UUID uuid;

	public PixelPlayer(Player player) {
		this.uuid = player.getUniqueId();

		pixelPlayers.add(this);
	}

	public static PixelPlayer getPixelPlayer(Player player) {
		for(PixelPlayer pixelPlayer : pixelPlayers) {
			if(pixelPlayer.uuid.equals(player.getUniqueId())) {
				return pixelPlayer;
			}
		}

		return new PixelPlayer(player);
	}
}
