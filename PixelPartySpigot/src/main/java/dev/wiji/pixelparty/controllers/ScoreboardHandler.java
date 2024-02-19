package dev.wiji.pixelparty.controllers;

import dev.wiji.pixelparty.PixelParty;
import dev.wiji.pixelparty.objects.PixelScoreboard;
import dev.wiji.pixelparty.util.Misc;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.minecraft.server.v1_8_R3.Scoreboard;
import net.minecraft.server.v1_8_R3.ScoreboardTeam;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_8_R3.scoreboard.CraftScoreboard;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class ScoreboardHandler implements Listener {

	public static PixelScoreboard preGame;
	public static PixelScoreboard inGame;
	public static PixelScoreboard postGame;

	public static GameManager game = PixelParty.gameManager;

	public ScoreboardHandler() {
		preGame = new PixelScoreboard();
		inGame = new PixelScoreboard();
		postGame = new PixelScoreboard();

		new BukkitRunnable() {
			GameManager.GameState previousState = game.gameState;
			@Override
			public void run() {
				for(int i = 0; i < getLobbyScoreboard().size(); i++) {
					preGame.add(getLobbyScoreboard().get(i), getLobbyScoreboard().size() - i);
				}
				preGame.update();

				for(int i = 0; i < getGameScoreboard().size(); i++) {
					inGame.add(getGameScoreboard().get(i), getGameScoreboard().size() - i);
				}
				inGame.update();

				for(int i = 0; i < getEndScoreboard().size(); i++) {
					postGame.add(getEndScoreboard().get(i), getEndScoreboard().size() - i);
				}
				postGame.update();

				if(previousState != game.gameState) {
					PixelScoreboard scoreboard = getScoreboard(game.gameState);

					for(Player player : Bukkit.getOnlinePlayers()) {
						scoreboard.send(player);
					}
				}

				previousState = game.gameState;
			}
		}.runTaskTimer(PixelParty.INSTANCE, 0, 5);



		for(Group group : PixelParty.LUCKPERMS.getGroupManager().getLoadedGroups()) {
			String prefix = group.getCachedData().getMetaData().getPrefix();
			assert prefix != null;

			Team playerList = Bukkit.getScoreboardManager().getMainScoreboard().registerNewTeam(group.getName());
			playerList.setPrefix(ChatColor.translateAlternateColorCodes('&', prefix));

			Team nameTags = preGame.getScoreboard().registerNewTeam(group.getName());
			nameTags.setPrefix(ChatColor.translateAlternateColorCodes('&', prefix));
		}
	}

	@EventHandler
	public void PlayerJoin(PlayerJoinEvent e) {
		Player player = e.getPlayer();

		User user = PixelParty.LUCKPERMS.getUserManager().getUser(player.getUniqueId());
		assert user != null;
		Group group = PixelParty.LUCKPERMS.getGroupManager().getGroup(user.getPrimaryGroup());

		assert group != null;
		Team playerList = Bukkit.getScoreboardManager().getMainScoreboard().getTeam(group.getName());
		playerList.addPlayer(player);

		Team nameTags = preGame.getScoreboard().getTeam(group.getName());
		nameTags.addPlayer(player);

		PixelScoreboard scoreboard = getScoreboard(game.gameState);
		scoreboard.send(player);
	}

	public List<String> getLobbyScoreboard() {
		List<String> scoreboard = new ArrayList<>();
		scoreboard.add(getDate());
		scoreboard.add(" ");
		scoreboard.add("&fPlayers: &a" + game.queueManager.queuedPlayers.size() + "/" + QueueManager.MAX_PLAYERS);
		scoreboard.add("  ");
		scoreboard.add(game.queueManager.timer == null ? "Waiting..." : "Starting in &a" + game.queueManager.seconds + "s");
		scoreboard.add("   ");
		scoreboard.add(getFooter());

		return scoreboard;
	}

	public List<String> getGameScoreboard() {
		List<String> scoreboard = new ArrayList<>();
		scoreboard.add(getDate());
		scoreboard.add(" ");
		scoreboard.add("&fPlayers: &e" + game.alivePlayers.size());
		scoreboard.add("  ");
		scoreboard.add("Round: &e" + game.round + "&7/" + GameManager.MAX_ROUNDS);

		String speed = game.speed % 1 == 0 ? String.valueOf((int) game.speed) : String.valueOf(game.speed);
		scoreboard.add("Round Speed: &e" + speed + "s");
		scoreboard.add("   ");
		scoreboard.add(getFooter());

		return scoreboard;
	}

	public List<String> getEndScoreboard() {
		List<String> scoreboard = new ArrayList<>();
		scoreboard.add(getDate());
		scoreboard.add("Final Round: &e" + game.round + "&7/" + GameManager.MAX_ROUNDS);
		scoreboard.add(game.alivePlayers.size() == 1 ? "Winner:" : "Winners:");
		for(int i = 0; i < game.alivePlayers.size(); i++) {
			if(i == 11) break;

			OfflinePlayer player = Bukkit.getOfflinePlayer(game.alivePlayers.get(i));
			scoreboard.add(" " + Misc.getDisplayName(player));
		}
		scoreboard.add("  ");
		scoreboard.add(getFooter());

		return scoreboard;
	}

	public static void setupTeams() {
		Scoreboard[] boards = {
				((CraftScoreboard) inGame.getScoreboard()).getHandle(),
				((CraftScoreboard) postGame.getScoreboard()).getHandle()
		};

		for(Scoreboard board : boards) {
			board.createTeam("alive");

			ScoreboardTeam alive = board.getTeam("alive");
			for(UUID alivePlayer : PixelParty.gameManager.alivePlayers) {
				Player player = Bukkit.getPlayer(alivePlayer);
				board.addPlayerToTeam(player.getName(), "alive");
			}

			alive.setCanSeeFriendlyInvisibles(true);

			board.createTeam("dead");
			ScoreboardTeam dead = board.getTeam("dead");
			dead.setCanSeeFriendlyInvisibles(true);
		}
	}

	public static void setToAliveTeam(Player player) {
		Scoreboard[] boards = {
				((CraftScoreboard) inGame.getScoreboard()).getHandle(),
				((CraftScoreboard) postGame.getScoreboard()).getHandle()
		};

		for(Scoreboard board : boards) {
			board.addPlayerToTeam(player.getName(), "alive");
		}

		PlayerManager.updatePlayers();
	}

	public static void setToDeadTeam(Player player) {
		Scoreboard[] boards = {
				((CraftScoreboard) inGame.getScoreboard()).getHandle(),
				((CraftScoreboard) postGame.getScoreboard()).getHandle()
		};

		for(Scoreboard board : boards) {
			ScoreboardTeam alive = board.getTeam("alive");
			board.removePlayerFromTeam(player.getName(), alive);
			board.addPlayerToTeam(player.getName(), "dead");
		}

		PlayerManager.updatePlayers();
	}

	public PixelScoreboard getScoreboard(GameManager.GameState state) {
		switch(state) {
			case LOBBY:
				return preGame;
			case INGAME:
				return inGame;
			case ENDING:
				return postGame;
			default:
				return null;
		}
	}

	public String getDate() {
		Format f = new SimpleDateFormat("MM/dd/yy");
		String strDate = f.format(new Date());
		return "&7" + strDate +  "  &8" + PixelParty.INSTANCE.getServerName() + " ";
	}

	public String getFooter() {
		return "&emc.wiji.dev";
	}
}
