package dev.wiji.pixelparty.controllers;

import de.tr7zw.nbtapi.NBTItem;
import dev.wiji.pixelparty.PixelParty;
import dev.wiji.pixelparty.enums.*;
import dev.wiji.pixelparty.events.MessageEvent;
import dev.wiji.pixelparty.leaderboard.LeaderboardManager;
import dev.wiji.pixelparty.messaging.PluginMessage;
import dev.wiji.pixelparty.objects.PowerUp;
import dev.wiji.pixelparty.objects.PracticeProfile;
import dev.wiji.pixelparty.playerdata.LeaderboardData;
import dev.wiji.pixelparty.playerdata.PixelPlayer;
import dev.wiji.pixelparty.util.Color;
import dev.wiji.pixelparty.util.ELOMatch;
import dev.wiji.pixelparty.util.MetaDataUtil;
import dev.wiji.pixelparty.util.Misc;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class GameManager implements Listener {
	public static final int MAX_ROUNDS = 25;

	public final FloorManager floorManager;
	public final BossBarManager bossBarManager;
	public final QueueManager queueManager;
	public final PlayerManager playerManager;
	public final MusicManager musicManager;

	public List<UUID> alivePlayers = new ArrayList<>();
	public Map<UUID, Integer> placementMap = new HashMap<>();

	public List<PowerUp.PowerUpPickup> powerUps = new ArrayList<>();

	public GameState gameState;
	public int round = 0;
	public double speed = 5;

	public boolean ranked = false;

	public BukkitTask runnable = null;

	public PracticeProfile practiceProfile;

	public GameManager() {
		this.gameState = GameState.LOBBY;
		this.floorManager = new FloorManager();
		this.bossBarManager = new BossBarManager();
		this.queueManager = new QueueManager();
		this.playerManager = new PlayerManager();
		this.musicManager = new MusicManager();

		PixelParty.INSTANCE.getServer().getPluginManager().registerEvents(bossBarManager, PixelParty.INSTANCE);
		PixelParty.INSTANCE.getServer().getPluginManager().registerEvents(queueManager, PixelParty.INSTANCE);
		PixelParty.INSTANCE.getServer().getPluginManager().registerEvents(playerManager, PixelParty.INSTANCE);
		PixelParty.INSTANCE.getServer().getPluginManager().registerEvents(musicManager, PixelParty.INSTANCE);

		this.practiceProfile = new PracticeProfile();
	}

	public void startGame() {
		PluginMessage response = new PluginMessage();
		response.writeString("GAME START");
		response.send();

		musicManager.playMusic();

		ScoreboardHandler.setupTeams();

		PluginMessage lock = new PluginMessage().writeString("LOCK PLAYERS");
		for(UUID alivePlayer : queueManager.queuedPlayers) {
			registerPlayer(Bukkit.getPlayer(alivePlayer));

			lock.writeString(alivePlayer.toString());
		}
		if(ranked) lock.send();

		AmbienceManager.setNight();

		gameState = GameState.INGAME;
		nextRound();
	}

	public void registerPlayer(Player player) {
		alivePlayers.add(player.getUniqueId());
		placementMap.put(player.getUniqueId(), -1);

		player.getInventory().clear();
		spawnPlayer(player);
		player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, false));
		PlayerManager.giveBoots(player);
		MusicManager.giveHeads(player);
		ScoreboardHandler.setToAliveTeam(player);
		if(PixelParty.serverType == ServerType.PRACTICE) {
			PracticeManager.giveSettingsItem(player);
			player.setAllowFlight(true);
		}
	}

	public void nextRound() {
		if(round == MAX_ROUNDS && !isPractice()) {
			endGame();
			return;
		}

		if(gameState == GameState.ENDING) return;

		if(round >= 2 && round <= 25 && Math.random() > 0.7 && practiceProfile.powerupsEnabled()) PowerUp.createRandomPowerUp();

		PowerUp.powerUps.forEach(PowerUp::onNextRoundStart);

		round = isPractice() ? practiceProfile.getRound() : round + 1;
		speed = getRoundSpeed(round);

		floorManager.chooseFloor();
		floorManager.generateBlueprint();
		floorManager.buildFloor();
		musicManager.resumeMusic();
		bossBarManager.updateBar("&bWaiting...");
		countDown(speed);
	}

	public void endGame() {
		runnable.cancel();

		gameState = GameState.ENDING;
		floorManager.chooseFloor();
		musicManager.resumeMusic();
		musicManager.startVolumeChange(true);
		PlayerManager.updatePlayers();
		BossBarManager.bossBar.remove();
		AmbienceManager.setDay();
		AmbienceManager.launchFireworks();

		for(PowerUp.PowerUpPickup powerUp : new ArrayList<>(powerUps)) powerUp.remove();

		if(ranked) {

			LeaderboardStatistic statistic = PixelParty.serverType == ServerType.NORMAL ?
					LeaderboardStatistic.NORMAL_ELO : LeaderboardStatistic.HYPER_ELO;

			ELOMatch match = new ELOMatch();
			for(Map.Entry<UUID, Integer> entry : placementMap.entrySet()) {
				UUID uuid = entry.getKey();
				PixelPlayer pixelPlayer = PixelPlayer.getPixelPlayer(uuid);

				int place = entry.getValue() == -1 ? 1 : entry.getValue();

				match.addPlayer(uuid, place, pixelPlayer.getLeaderboardStat(LeaderboardType.LIFETIME, statistic));
			}

			match.calculateELOs();

			PluginMessage unlock = new PluginMessage().writeString("UNLOCK PLAYERS");
			for(UUID uuid : placementMap.keySet()) {
				PixelPlayer pixelPlayer = PixelPlayer.getPixelPlayer(uuid);
				unlock.writeString(uuid.toString());

				if(round < 15) continue;

				for(LeaderboardData leaderboardDatum : pixelPlayer.leaderboardData) {
					int newElo = match.getELO(uuid);
					leaderboardDatum.setValue(statistic, newElo);
				}

				pixelPlayer.save();
			}

			unlock.send();
			if(round < 15) Bukkit.broadcastMessage(Misc.color("&7&oYour ELO was not effected since the game ended before round 15."));
			else sendEloUpdateMessage(match);
		}


		for(UUID alivePlayer : alivePlayers) {
			Player player = Bukkit.getPlayer(alivePlayer);
			spawnPlayer(player);
			player.getInventory().clear();
			SpectatorManager.setSpectator(player);

			ScoreboardHandler.setToDeadTeam(player);
			PlayerManager.updatePlayers();
			player.teleport(AmbienceManager.CENTER);

			PixelPlayer pixelPlayer = PixelPlayer.getPixelPlayer(player);

			if(PixelParty.serverType == ServerType.NORMAL) {
				pixelPlayer.addStat(LeaderboardStatistic.NORMAL_WINS);
				pixelPlayer.save();
			} else if(PixelParty.serverType == ServerType.HYPER) {
				pixelPlayer.addStat(LeaderboardStatistic.HYPER_WINS);
				pixelPlayer.save();
			}
		}

		TitleManager.displayEndTitle();

		new BukkitRunnable() {
			@Override
			public void run() {
				sendGameEnd();
			}
		}.runTaskLater(PixelParty.INSTANCE, 20 * 10);
	}

	public void countDown(double seconds) {
		final int paddedTime = 3;

		runnable = new BukkitRunnable() {
			int ticks = 0;
			int alertCount = 0;

			final int alerts = getAlertCount(round);

			final int ticksPerAlert = (int) ((seconds * 20) + paddedTime) / (alerts);

			@Override
			public void run() {
				int alertsLeft = alerts - alertCount;

				if(ticks == 0) {
					floorManager.chooseColor();
					setHotBarSlots();
					playSound(Sound.NOTE_BASS, 2, 0.4F + (alertsLeft) * 0.2F);
					bossBarManager.updateBar(generateBarColor(alertsLeft));

					if(practiceProfile.isPaused()) ticks++;
				}

				if(ticks % ticksPerAlert == 0 && alertCount <= alerts) {
					if(!practiceProfile.isPaused()) ticks++;

					if(alertCount == alerts) {
						for(PowerUp.PowerUpPickup powerUpPickup : new ArrayList<>(powerUps)) powerUpPickup.remove();
						floorManager.fillVoid();
						musicManager.pauseMusic();
						GameSound.FLOOR_VANISH.playAll();
						bossBarManager.updateBar("&4\u2716 &f&lFREEZE &4\u2716");
						removePlayersItem();
						alertCount++;
						return;
					}

					playSound(Sound.NOTE_BASS, 2, 0.4F + (alertsLeft) * 0.2F);
					bossBarManager.updateBar(generateBarColor(alertsLeft ));
					givePlayersItem();

					alertCount++;
				}

				if(ticks == ((seconds * 20) + paddedTime) + ((60 - paddedTime) + 3) - 10) {
					for(PowerUp powerUp : PowerUp.powerUps) powerUp.onRoundEnd();
					nextRound();
					cancel();

					if(practiceProfile.isPaused()) ticks++;
				}

				if(!practiceProfile.isPaused()) ticks++;
			}
		}.runTaskTimer(PixelParty.INSTANCE, 80, 1);
	}


	public String generateBarColor(int numBars) {
		char[] barCharacters = {'\u2587', '\u2586', '\u2585', '\u2584', '\u2583'};

		if (numBars < 1 || numBars > barCharacters.length) {
			throw new IllegalArgumentException("Invalid number of bars");
		}

		StringBuilder result = new StringBuilder();
		Color color = Color.fromData(floorManager.chosenColor);

		assert color != null;
		result.append(color.getChatColor());

		for(int i = numBars - 1; i >= 0; i--) {
			result.append(barCharacters[i]);
		}

		result.append(" &f&l").append(color.getName()).append(" ").append(color.getChatColor());

		for(int i = 0; i < numBars; i++) {
			result.append(barCharacters[i]);
		}

		return result.toString();
	}

	public void spawnPlayer(Player player) {
		player.teleport(PixelParty.serverType == ServerType.PRACTICE ?
				practiceProfile.getSpawnLocation(player) : PlayerManager.getSpawnLocation());
	}

	public void playSound(Sound sound, float volume, float pitch) {
		for(Player player : Bukkit.getOnlinePlayers()) {
			player.playSound(player.getLocation(), sound, volume, pitch);
		}
	}

	public void givePlayersItem() {
		ItemStack itemStack = new ItemStack(Material.STAINED_CLAY, 1, floorManager.chosenColor);
		ItemMeta itemMeta = itemStack.getItemMeta();

		Color color = Color.fromData(floorManager.chosenColor);
		assert color != null;

		itemMeta.setDisplayName(ChatColor.BOLD + color.getName());
		itemStack.setItemMeta(itemMeta);

		NBTItem nbtItem = new NBTItem(itemStack, true);
		nbtItem.setBoolean(NBTTag.COLOR_BLOCK.getRef(), true);

		for(UUID alivePlayer : alivePlayers) {
			Player player = Bukkit.getPlayer(alivePlayer);
			PixelPlayer pixelPlayer = PixelPlayer.getPixelPlayer(player);

			if(pixelPlayer.woolFloor) itemStack.setType(Material.WOOL);

			if(PracticeManager.isInSettings(player)) continue;

			player.getInventory().setItem(8, itemStack);
		}
	}

	public void setHotBarSlots() {
		for(UUID alivePlayer : alivePlayers) {
			Player player = Bukkit.getPlayer(alivePlayer);
			if(PracticeManager.isInSettings(player)) continue;
			player.getInventory().setHeldItemSlot(8);
		}
	}

	public void removePlayersItem() {
		for(UUID alivePlayer : alivePlayers) {
			Player player = Bukkit.getPlayer(alivePlayer);

			for(ItemStack content : player.getInventory().getContents()) {
				if(Misc.isAirOrNull(content)) continue;
				NBTItem nbtItem = new NBTItem(content);

				if(nbtItem.hasKey(NBTTag.COLOR_BLOCK.getRef())) player.getInventory().remove(content);
			}

		}
	}

	@EventHandler
	public void onMessage(MessageEvent event) {
		PluginMessage message = event.getMessage();
		List<String> strings = message.getStrings();

		if(strings.size() < 1) return;
		if(strings.get(0).equals("SET RANKED")) {
			LeaderboardStatistic statistic = PixelParty.serverType == ServerType.NORMAL ?
					LeaderboardStatistic.NORMAL_ELO : LeaderboardStatistic.HYPER_ELO;

			LeaderboardManager.setDefaultStatistic(statistic);
			ranked = true;
		}
	}

	public void setPlayerExperience(float progress) {
		for(Player player : Bukkit.getOnlinePlayers()) {
			player.setExp(progress);
		}
	}

	public void sendGameEnd() {
		PluginMessage response = new PluginMessage();
		response.writeString("GAME END");
		response.send();
	}

	public void sendEloUpdateMessage(ELOMatch match) {
		Bukkit.broadcastMessage(Misc.color("&e&m------------&r &bELO Update &e&m------------"));
		for(ELOMatch.ELOPlayer player : match.players) {

			int eloChange = match.getELOChange(player.uuid);
			ChatColor color = eloChange > 0 ? ChatColor.GREEN : ChatColor.RED;
			String change = color + (eloChange > 0 ? "+" : "") + eloChange;

			Bukkit.broadcastMessage(MetaDataUtil.getDisplayName(player.uuid) + Misc.color(" &7- &e" + match.getELO(player.uuid)+ " &7(" + change + "&7)"));
		}
		Bukkit.broadcastMessage(Misc.color("&e&m--------------------------------------"));
	}

	public double getRoundSpeed(int round) {
		if(round >= 24) return 0.5;
		if(round >= 22) return 1;
		if(round >= 20) return 1.5;
		if(round >= 18) return 2;
		if(round >= 16) return 2.5;
		if(round >= 14) return 3;
		if(round >= 12) return 3.25;
		if(round >= 10) return 3.5;
		if(round >= 8) return 3.75;
		if(round >= 6) return 4;
		if(round >= 4) return 4.25;
		return 4.5;
	}

	public int getAlertCount(int round) {
		if(round <= 5) return 5;
		if(round <= 13) return 4;
		if(round <= 17) return 3;
		if(round <= 21) return 2;
		return 1;
	}

	public boolean isPractice() {
		return PixelParty.serverType == ServerType.PRACTICE;
	}

	public enum GameState {
		LOBBY,
		INGAME,
		ENDING
	}
}
