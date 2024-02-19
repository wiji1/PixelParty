package dev.wiji.pixelparty.controllers;

import dev.wiji.pixelparty.PixelParty;
import dev.wiji.pixelparty.messaging.PluginMessage;
import dev.wiji.pixelparty.enums.*;
import dev.wiji.pixelparty.objects.PowerUp;
import dev.wiji.pixelparty.objects.PracticeProfile;
import dev.wiji.pixelparty.util.Misc;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityMetadata;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.UUID;

public class PlayerManager implements Listener {
	public static final int DEATH_HEIGHT = -80;

	@EventHandler
	public void onMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		GameManager game = PixelParty.gameManager;

		if(player.getLocation().getY() <= DEATH_HEIGHT) {
			if(PixelParty.serverType == ServerType.PRACTICE) {
				PracticeProfile profile = PixelParty.gameManager.practiceProfile;
				player.teleport(profile.getSpawnLocation(player));
				return;
			}

			if(game.alivePlayers.contains(player.getUniqueId())) {
				if(game.gameState == GameManager.GameState.ENDING) {
					game.spawnPlayer(player);
					return;
				}
				
				for(UUID aliveUUID : game.alivePlayers) {
					Player alivePlayer = Bukkit.getPlayer(aliveUUID);
					if(alivePlayer.getLocation().getY() > 0) {
						killPlayer(player, DeathCause.FALL);
						if(game.alivePlayers.size() == 1) game.endGame();
						return;
					}
				}

				game.endGame();
			} else player.teleport(QueueManager.SPAWN_LOCATION);
		}

	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		GameManager game = PixelParty.gameManager;

		if(PixelParty.serverType == ServerType.PRACTICE) {
			if(Bukkit.getOnlinePlayers().size() == 1) game.sendGameEnd();
			else if(event.getPlayer() == PixelParty.owner) {
				for(Player player : Bukkit.getOnlinePlayers()) {
					if(player != PixelParty.owner) {
						PixelParty.owner = player;
						break;
					}
				}

				PluginMessage message = new PluginMessage().setIntendedServer("PROXY");
				message.writeString("OWNERSHIP CHANGE").writeString(PixelParty.owner.getUniqueId().toString()).send();
			}

			return;
		}

		if(game.gameState != GameManager.GameState.INGAME) return;

		if(game.alivePlayers.contains(event.getPlayer().getUniqueId())) killPlayer(event.getPlayer(), DeathCause.FORFEIT);
		if(game.alivePlayers.size() <= 1) game.endGame();
	}

	@EventHandler
	public void onArmorStandInteract(PlayerArmorStandManipulateEvent event) {
		event.setCancelled(true);
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
		if(event.getClickedBlock() == null) return;

		for(PowerUp.PowerUpPickup powerUp : PixelParty.gameManager.powerUps) {
			if(powerUp.location.equals(event.getClickedBlock().getLocation())) {

				event.setCancelled(true);

				if(PracticeManager.isInSettings(event.getPlayer())) {
					event.getPlayer().sendMessage(Misc.color("&cYou cannot collect Powerups while in settings mode!"));
					return;
				}

				powerUp.onInteract(event.getPlayer());

				//TODO: Play powerup sound and do particles

				CooldownManager.addCooldown(event.getPlayer(), CooldownType.POWER_UP_COLLECT, 2 * 20);
				CooldownManager.addCooldown(event.getPlayer(), CooldownType.ITEM_USE, 5);
				return;
			}
		}

		if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

		ItemStack held = event.getPlayer().getItemInHand();
		Block block = event.getClickedBlock();

		if(held.getType() == block.getType() && held.getData().getData() == block.getData()) {
			GameSound.COLOR_PLACE.play(event.getPlayer());
		}

		event.getPlayer().updateInventory();
	}

	@EventHandler
	public void onInventoryInteract(InventoryClickEvent event) {
		int[] lockedSlots = {3, 4, 5, 8, 39, 38, 37, 36};

		Player player = (Player) event.getWhoClicked();
		if(PracticeManager.isInSettings(player) && event.getSlot() <= 8) {
			event.setCancelled(true);
		}

		for(int slot : lockedSlots) {
			if(slot == event.getSlot()) {
				event.setCancelled(true);
				break;
			}
		}

		player.updateInventory();
	}

	@EventHandler
	public void onDrop(PlayerDropItemEvent event) {
		event.setCancelled(true);
	}

	public void killPlayer(Player player, DeathCause cause) {
		player.getWorld().strikeLightningEffect(player.getLocation());
		player.teleport(QueueManager.SPAWN_LOCATION);
		PixelParty.gameManager.alivePlayers.remove(player.getUniqueId());
		player.getInventory().clear();
		ScoreboardHandler.setToDeadTeam(player);
		SpectatorManager.setSpectator(player);

		Misc.broadcast("&4\u2716 " + Misc.getDisplayName(player) + " &7" + cause.getMessage() + "!");
	}

	public static void updatePlayers() {
		for(Player onlinePlayer : Bukkit.getOnlinePlayers()) {
			for(Player target : Bukkit.getOnlinePlayers()) {
				EntityPlayer nmsTarget = ((CraftPlayer) target).getHandle();
				PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(target.getEntityId(), nmsTarget.getDataWatcher(), true);
				((CraftPlayer) onlinePlayer).getHandle().playerConnection.sendPacket(packet);
			}
		}
	}

	public static void giveBoots(Player player) {
		User user = PixelParty.LUCKPERMS.getUserManager().getUser(player.getUniqueId());
		assert user != null;
		Group group = PixelParty.LUCKPERMS.getGroupManager().getGroup(user.getPrimaryGroup());

		ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
		LeatherArmorMeta meta = (LeatherArmorMeta) boots.getItemMeta();
		meta.setColor(BootColor.fromGroup(group).getColor());
		boots.setItemMeta(meta);
		player.getInventory().setBoots(boots);
	}

	static int spawned = 0;
	public static Location getSpawnLocation() {
		Location spawn = new Location(Bukkit.getWorld("world"), 0, 1, 0);
		if(spawned % 2 == 0) {
			spawn.add(27.5, 0, 0);
			spawn.setYaw(90);
		} else {
			spawn.add(-27.5, 0, 0);
			spawn.setYaw(-90);
		}

		spawn.add(0, 0, 22.5 * (spawned % 2 == 0 ? 1 : -1));
		spawned++;

		return spawn;
	}


}
