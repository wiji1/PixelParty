package dev.wiji.pixelparty.enums;

public enum NBTTag {

	ITEM_TAG("pp-item"),
	ENDER_PEARL("pp-ender-pearl"),
	FEATHER("pp-feather"),
	JUMP_POTION("pp-jump-potion"),
	SPEED_POTION("pp-speed-potion"),
	ACID_RAIN("pp-acid-rain"),
	PAINT_EGG("pp-paint-egg"),
	TELEPORTER("pp-teleporter"),
	SPECTATOR_SETTINGS("pp-spectator-settings"),
	PLAY_AGAIN("pp-play-again"),
	BACK_TO_LOBBY("pp-back-to-lobby"),
	PRACTICE_SETTINGS("pp-practice-settings"),
	EXIT_SETTINGS("pp-exit-settings"),
	PRIVACY_SETTINGS("pp-privacy-settings"),
	FLOOR_SELECT("pp-floor-select"),
	SPAWN_SELECT("pp-spawn-select"),
	COLOR_SELECT("pp-color-select"),
	DECREASE_SPEED("pp-decrease-speed"),
	INCREASE_SPEED("pp-increase-speed"),
	PAUSE_GAME("pp-pause-game"),
	TOGGLE_POWERUPS("pp-toggle-powerups"),
	;

	private final String ref;

	NBTTag(String ref) {
		this.ref = ref;
	}

	public String getRef() {
		return ref;
	}
}
