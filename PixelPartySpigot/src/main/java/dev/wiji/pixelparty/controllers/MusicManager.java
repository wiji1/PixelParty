package dev.wiji.pixelparty.controllers;

import dev.wiji.pixelparty.PixelParty;
import dev.wiji.pixelparty.enums.GameSound;
import dev.wiji.pixelparty.enums.Skin;
import dev.wiji.pixelparty.playerdata.PixelPlayer;
import dev.wiji.pixelparty.util.SkinUtil;
import net.raphimc.noteblocklib.NoteBlockLib;
import net.raphimc.noteblocklib.model.Note;
import net.raphimc.noteblocklib.model.Song;
import net.raphimc.noteblocklib.player.ISongPlayerCallback;
import net.raphimc.noteblocklib.player.SongPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MusicManager implements Listener {

	public SongPlayer musicPlayer;

	public double volumeModifier = 0;

	public void playMusic() {

		File musicFolder = new File(PixelParty.INSTANCE.getDataFolder().getPath() + "/songs/");
		File[] musicFiles = musicFolder.listFiles();

		if(musicFiles == null) return;

		File musicFile = musicFiles[(int) (Math.random() * musicFiles.length)];

		try {
			Song<?, ?, ?> song = NoteBlockLib.readSong(musicFile);

			musicPlayer = new SongPlayer(song.getView(), new ISongPlayerCallback() {
				@Override
				public void playNote(Note note) {

					for(Player player : Bukkit.getOnlinePlayers()) {
						player.playSound(player.getLocation(), getSound(note.getInstrument()), getVolume(player), convertToFloat(note.getKey()));
					}
				}

				@Override
				public boolean shouldLoop() {
					return true;
				}
			});

			musicPlayer.play();
			startVolumeChange(false);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void pauseMusic() {
		musicPlayer.setPaused(true);
	}

	public void resumeMusic() {
		musicPlayer.setPaused(false);
	}

	public static float convertToFloat(int number) {

		switch(number) {
			case 33:
				return 0.5F;
			case 34:
				return 0.53F;
			case 35:
				return 0.56F;
			case 36:
				return 0.6F;
			case 37:
				return 0.63F;
			case 38:
				return 0.67F;
			case 39:
				return 0.7F;
			case 40:
				return 0.75F;
			case 41:
				return 0.80F;
			case 42:
				return 0.85F;
			case 43:
				return 0.9F;
			case 44:
				return 0.95F;
			case 45:
				return 1F;
			case 46:
				return 1.06F;
			case 47:
				return 1.12F;
			case 48:
				return 1.20F;
			case 49:
				return 1.26F;
			case 50:
				return 1.34F;
			case 51:
				return 1.42F;
			case 52:
				return 1.5F;
			case 53:
				return 1.6F;
			case 54:
				return 1.68F;
			case 55:
				return 1.78F;
			case 56:
				return 1.88F;
			case 57:
				return 2F;
			default:
				return 1F;
		}
	}

	public Sound getSound(byte instrumentID) {

		switch(instrumentID) {
			case 0:
				return Sound.NOTE_PIANO;
			case 1:
				return Sound.NOTE_BASS;
			case 2:
				return Sound.NOTE_BASS_DRUM;
			case 3:
				return Sound.NOTE_SNARE_DRUM;
			case 4:
				return Sound.NOTE_STICKS;
			case 15:
				return Sound.NOTE_PLING;
		}

		return null;
	}

	public float getVolume(Player player) {
		PixelPlayer pixelPlayer = PixelPlayer.getPixelPlayer(player);
		if(pixelPlayer.pausedMusic) return 0;

		double playerModifier = (double) pixelPlayer.volume / 50;
		float baseVolume = (float) (2.0f * playerModifier);

		return (float) (baseVolume * volumeModifier);
	}

	public void startVolumeChange(boolean decrease) {
		double seconds = 6;

		new BukkitRunnable() {
			int i = 0;
			final int modifier = decrease ? -1 : 1;

			@Override
			public void run() {
				volumeModifier = Math.min(1, volumeModifier + (modifier * (1 / (seconds * 20))));
				if(i++ >= seconds * 20) cancel();
			}
		}.runTaskTimer(PixelParty.INSTANCE, 0, 1);
	}

	public static void giveHeads(Player player) {

		List<ItemStack> heads = new ArrayList<>();
		Skin[] headSkins = {Skin.MINUS, Skin.PAUSE, Skin.PLUS};
		String[] names = {"&bLower Volume", "&bToggle Music", "&bRaise Volume"};

		for(int i = 0; i < headSkins.length ; i++) {
			Skin skin = headSkins[i];
			ItemStack head = SkinUtil.getPlayerSkull(skin);

			ItemMeta meta = head.getItemMeta();
			meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', names[i]));
			head.setItemMeta(meta);

			heads.add(head);
		}

		for(int i = 3; i < 6; i++) {
			player.getInventory().setItem(i, heads.get(i - 3));
		}
	}

	public void increaseVolume(PixelPlayer player) {
		player.volume = Math.min(100, player.volume + 5);

	}

	public void decreaseVolume(PixelPlayer player) {
		player.volume = Math.max(5, player.volume - 5);
	}

	public void toggleMute(PixelPlayer player) {
		player.pausedMusic = !player.pausedMusic;
	}

	@EventHandler
	public void onClick(PlayerInteractEvent event) {
		if(PixelParty.gameManager.gameState != GameManager.GameState.INGAME) return;
		if(event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		if(PracticeManager.isInSettings(event.getPlayer())) return;
		//TODO: Replace this with NBT tags on the heads

		Player player = event.getPlayer();
		PixelPlayer pixelPlayer = PixelPlayer.getPixelPlayer(player);

		int slot = player.getInventory().getHeldItemSlot();

		switch(slot) {
			case 3:
				decreaseVolume(pixelPlayer);
				ActionBarManager.sendActionBarMessage(player, "&e" + pixelPlayer.volume + "%", 1);
				GameSound.MUSIC_TOGGLE.play(player);
				break;
			case 4:
				toggleMute(pixelPlayer);
				ActionBarManager.sendActionBarMessage(player, "&e" + (pixelPlayer.pausedMusic ? "Paused" : "Resumed"), 1);
				GameSound.MUSIC_TOGGLE.play(player);
				break;
			case 5:
				increaseVolume(pixelPlayer);
				ActionBarManager.sendActionBarMessage(player, "&e" + pixelPlayer.volume + "%", 1);
				GameSound.MUSIC_TOGGLE.play(player);
				break;
		}
	}
}
