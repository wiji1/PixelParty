package dev.wiji.pixelparty.controllers;

import dev.wiji.pixelparty.PixelParty;
import dev.wiji.pixelparty.enums.Group;
import dev.wiji.pixelparty.enums.ServerType;
import dev.wiji.pixelparty.enums.Skin;
import dev.wiji.pixelparty.events.MessageEvent;
import dev.wiji.pixelparty.inventory.ServerSelectorGUI;
import dev.wiji.pixelparty.messaging.PluginMessage;
import dev.wiji.pixelparty.objects.PacketPlayer;
import dev.wiji.pixelparty.playerdata.PixelPlayer;
import dev.wiji.pixelparty.util.Misc;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.scoreboard.Team;

import java.util.*;

public class LobbyManager implements Listener {

	public static PluginMessage latestPracticeQueue = null;

	public static final Location SPAWN_LOCATION = new Location(Bukkit.getWorld("lobby"), 0.5, 70, 0.5);
	public static List<UUID> queueCooldown = new ArrayList<>();

	public static List<PacketPlayer> lobbyNPCs = new ArrayList<>();

	public static Map<ServerType, Integer> playerCounts = new HashMap<>();

	public LobbyManager() {
		for(Group group : Group.values()) {

			Team playerList = Bukkit.getScoreboardManager().getMainScoreboard().registerNewTeam(group.name());
			playerList.setPrefix(group.getChatColor().toString());
		}

		lobbyNPCs.add(new PacketPlayer(Misc.color("&e&lNORMAL"), Skin.NORMAL) {
			@Override
			public Location getLocation() {
				return new Location(Bukkit.getWorld("lobby"), 5.5, 69, 10.5, 153, 0);
			}

			@Override
			public List<String> getHologramText() {
				return Collections.singletonList("&e" + playerCounts.get(ServerType.NORMAL) + " Playing");
			}

			@Override
			public void onRightClick(Player player) {
				queuePlayer(player, ServerType.NORMAL);
			}
		});


		lobbyNPCs.add(new PacketPlayer(Misc.color("&b&lPRACTICE"), Skin.PRACTICE) {
			@Override
			public Location getLocation() {
				return new Location(Bukkit.getWorld("lobby"), -4.5, 69, 10.5, -153, 0);
			}

			@Override
			public List<String> getHologramText() {
				return Collections.singletonList("&e" + playerCounts.get(ServerType.PRACTICE) + " Playing");
			}

			@Override
			public void onRightClick(Player player) {
				PluginMessage message = new PluginMessage();
				message.setIntendedServer("PROXY");
				message.writeString("REQUEST PRACTICE DATA");
				message.request((response) -> {
					ServerSelectorGUI gui = new ServerSelectorGUI(player, response);
					gui.open();
				});
			}
		});

		lobbyNPCs.add(new PacketPlayer(Misc.color("&a&lSETTINGS"), Skin.SETTINGS) {
			@Override
			public Location getLocation() {
				return new Location(Bukkit.getWorld("lobby"), 7.5, 69, -6.5, 45, 0);
			}

			@Override
			public void onRightClick(Player player) {
				player.sendMessage(Misc.color("&cThis feature is coming soon!"));
			}
		});

		lobbyNPCs.add(new PacketPlayer(Misc.color("&6&lTOURNAMENTS"), Skin.TOURNAMENT) {
			@Override
			public Location getLocation() {
				return new Location(Bukkit.getWorld("lobby"), -6.5, 69, -6.5, -45, 0);
			}

			@Override
			public void onRightClick(Player player) {
				player.sendMessage(Misc.color("&cThis feature is coming soon!"));
			}
		});
	}

	public static void queuePlayer(Player player, ServerType serverType) {
		if(queueCooldown.contains(player.getUniqueId())) return;

		PluginMessage pluginMessage = new PluginMessage().setIntendedServer("PROXY");
		pluginMessage.writeString("QUEUE").writeString(player.getUniqueId().toString()).writeString(serverType.name());
		pluginMessage.send();

		queueCooldown.add(player.getUniqueId());

		Bukkit.getScheduler().runTaskLater(PixelParty.INSTANCE, () -> queueCooldown.remove(player.getUniqueId()), 20 * 5);
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();

		PacketInjector.addPlayer(player);
		PacketManager.sendTabHeaders(player);

		player.teleport(SPAWN_LOCATION);
		event.setJoinMessage(null);

		Bukkit.getScheduler().runTaskLater(PixelParty.INSTANCE, () -> {
			for(PacketPlayer lobbyNPC : lobbyNPCs) lobbyNPC.spawnForPlayer(player);
		}, 5);

		PixelPlayer pixelPlayer = PixelPlayer.getPixelPlayer(player);
		Group group = pixelPlayer.getGroup();

		Team playerList = Bukkit.getScoreboardManager().getMainScoreboard().getTeam(group.name());
		playerList.addPlayer(player);
	}

	@EventHandler
	public void onMessage(MessageEvent event) {
		PluginMessage message = event.getMessage();
		List<String> strings = message.getStrings();

		if(strings.size() < 1) return;

		if(strings.get(0).equals("PLAYER COUNTS")) {
			for(String string : strings.subList(1, strings.size())) {
				String[] split = string.split(" ");
				playerCounts.put(ServerType.valueOf(split[0]), Integer.parseInt(split[1]));
			}
		}
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		event.setQuitMessage(null);
	}

	@EventHandler
	public void onWeatherChange(WeatherChangeEvent event){
		event.setCancelled(event.toWeatherState());
	}

	@EventHandler
	public void onHungerLoss(FoodLevelChangeEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void onDamage(EntityDamageEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void onDamage(EntityDamageByEntityEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void onAchievement(PlayerAchievementAwardedEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void onBreak(BlockBreakEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void onPlace(BlockPlaceEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void onClick(PlayerInteractEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void onInventoryInteract(InventoryClickEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void onDrop(PlayerDropItemEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void onMove(PlayerMoveEvent event) {
		if(event.getTo().getY() < 0) {
			event.getPlayer().teleport(SPAWN_LOCATION);
			return;
		}

		if(event.getTo().distance(SPAWN_LOCATION) > 150) {
			event.getPlayer().teleport(SPAWN_LOCATION);
		}
	}

	@EventHandler
	public void onPortalEnter(EntityPortalEnterEvent event) {
		if(!(event.getEntity() instanceof Player)) return;
		Player player = (Player) event.getEntity();

		queuePlayer(player, ServerType.NORMAL);
	}

	@EventHandler
	public void onPortal(PlayerPortalEvent event) {
		event.setCancelled(true);
	}

}
