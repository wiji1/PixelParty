package dev.wiji.pixelparty.enums;


import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public enum GameSound {

	MUSIC_TOGGLE(Sound.CLICK, 0.5f, 1.0f),
	BROADCAST(Sound.NOTE_STICKS, 1, 1),
	FLOOR_VANISH(Sound.BAT_TAKEOFF, 1, 1),
	COLOR_PLACE(Sound.CAT_MEOW, 1, 1),
	ACID_RAIN(Sound.FIZZ, 1, 1),
	PAINT_EGG(Sound.EXPLODE, 1.5f, 1),
	COLOR_COW(Sound.EXPLODE, 1.5f, 0.8f),
	CLICK(Sound.CLICK, 1f, 1.0f),
	ITEM_PLACE(Sound.CHICKEN_EGG_POP, 1, 0.8f),
	ITEM_BREAK(Sound.CHICKEN_EGG_POP, 1, 1.2f),
	ERROR(Sound.ENDERMAN_TELEPORT, 1, 0.5f),
	SUCCESS(Sound.NOTE_PLING, 1, 2),
	;

	private final Sound sound;
	private final float volume;
	private final float pitch;


	GameSound(Sound sound, float volume, float pitch) {
		this.sound = sound;
		this.volume = volume;
		this.pitch = pitch;
	}

	public void play(Location location) {
		location.getWorld().playSound(location, sound, volume, pitch);
	}

	public void play(Player player) {
		player.playSound(player.getLocation(), sound, volume, pitch);
	}

	public void playAll() {
		for(Player player : Bukkit.getOnlinePlayers()) {
			play(player);
		}
	}

	public Sound getSound() {
		return sound;
	}

	public float getVolume() {
		return volume;
	}

	public float getPitch() {
		return pitch;
	}
}
