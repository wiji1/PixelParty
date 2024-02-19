package dev.wiji.pixelparty.controllers;

import de.tr7zw.nbtapi.NBTItem;
import dev.wiji.pixelparty.PixelParty;
import dev.wiji.pixelparty.enums.GameSound;
import dev.wiji.pixelparty.enums.NBTTag;
import dev.wiji.pixelparty.enums.ServerType;
import dev.wiji.pixelparty.enums.Skin;
import dev.wiji.pixelparty.events.PacketReceiveEvent;
import dev.wiji.pixelparty.inventory.FloorSelectGUI;
import dev.wiji.pixelparty.inventory.PrivacyGUI;
import dev.wiji.pixelparty.objects.PracticeProfile;
import dev.wiji.pixelparty.util.Misc;
import dev.wiji.pixelparty.util.SkinUtil;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Field;
import java.util.*;

public class PracticeManager implements Listener {

	public static final int SETTINGS_SLOT = 7;

	public static List<UUID> settingsPlayers = new ArrayList<>();
	public static Map<UUID, EntityArmorStand> spawnMap = new HashMap<>();
	public static List<EntityArmorStand> colorList = new ArrayList<>();

	public static boolean isInSettings(Player player) {
		return settingsPlayers.contains(player.getUniqueId());
	}

	public static void giveSettingsItem(Player player) {
		player.getInventory().setItem(SETTINGS_SLOT, getSettingsItem());
	}

	@EventHandler
	public void onSpawnPlace(PlayerInteractEvent event) {
		if(event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) return;
		if(PixelParty.serverType != ServerType.PRACTICE) return;

		ItemStack item = event.getPlayer().getItemInHand();
		if(Misc.isAirOrNull(item)) return;

		NBTItem nbtItem = new NBTItem(item);

		Block clickedBlock = event.getClickedBlock();
		if(clickedBlock != null && !PixelParty.gameManager.floorManager.isOnFloor(clickedBlock.getLocation())) return;

		if(nbtItem.hasKey(NBTTag.SPAWN_SELECT.getRef())) {
			event.setCancelled(true);
			if(event.getAction() == Action.RIGHT_CLICK_AIR) return;

			if(spawnMap.containsKey(event.getPlayer().getUniqueId())) {
				removeEntity(event.getPlayer(), spawnMap.get(event.getPlayer().getUniqueId()));
				spawnMap.remove(event.getPlayer().getUniqueId());
			}

			ActionBarManager.sendActionBarMessage(event.getPlayer(), "&aSpawn point selected! Punch to remove.", 1);
			GameSound.ITEM_PLACE.play(event.getPlayer());

			EntityArmorStand stand = new EntityArmorStand(((CraftWorld) event.getPlayer().getWorld()).getHandle());

			stand.setInvisible(true);
			stand.setSmall(true);
			stand.setGravity(false);

			Location loc = event.getClickedBlock().getLocation().add(0.5, 0.1, 0.5);
			loc.setYaw(event.getPlayer().getLocation().getYaw());

			stand.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());

			spawnMap.put(event.getPlayer().getUniqueId(), stand);
			spawnEntity(event.getPlayer(), stand, Skin.SPAWN_SELECT);

		}
	}

	@EventHandler
	public void onColorInteract(PlayerInteractEvent event) {
		if(PixelParty.serverType != ServerType.PRACTICE) return;

		ItemStack itemStack = event.getItem();
		if(Misc.isAirOrNull(itemStack)) return;

		PracticeProfile profile = PixelParty.gameManager.practiceProfile;
		if(!profile.getManagerList().contains(event.getPlayer().getUniqueId()) && PixelParty.owner != event.getPlayer()) return;

		NBTItem nbtItem = new NBTItem(itemStack);
		if(nbtItem.hasKey(NBTTag.COLOR_SELECT.getRef())) {
			event.setCancelled(true);

			if(event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) return;

			if(event.getPlayer().isSneaking() && !colorList.isEmpty()) {
				for(EntityArmorStand armorStand : colorList) {
					removeEntityForAll(armorStand);
				}

				colorList.clear();
				ActionBarManager.sendActionBarMessage(event.getPlayer(), "&cColor points removed!", 1);
				GameSound.ITEM_BREAK.play(event.getPlayer());
			}

			if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				ActionBarManager.sendActionBarMessage(event.getPlayer(), "&cColor point selected! Punch to remove. Shift Right-Click to remove all.", 1);
				GameSound.ITEM_PLACE.play(event.getPlayer());

				Location loc = event.getClickedBlock().getLocation().add(0.5, 0.1, 0.5);

				EntityArmorStand stand = new EntityArmorStand(((CraftWorld) event.getPlayer().getWorld()).getHandle());
				stand.setInvisible(true);
				stand.setSmall(true);
				stand.setGravity(false);

				stand.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());

				colorList.add(stand);
				spawnEntityForAll(stand, Skin.COLOR_SELECT);
			}
		}
	}

	@EventHandler
	public void onPacketReceive(PacketReceiveEvent event) {
		if(!event.getPacketType().name().equals("USE_ENTITY")) return;

		if(PixelParty.serverType != ServerType.PRACTICE) return;
		PracticeProfile profile = PixelParty.gameManager.practiceProfile;

		try {
			Field action = event.getPacket().getClass().getDeclaredField("action");
			action.setAccessible(true);

			Field entityID = event.getPacket().getClass().getDeclaredField("a");
			entityID.setAccessible(true);

			PacketPlayInUseEntity.EnumEntityUseAction useAction = (PacketPlayInUseEntity.EnumEntityUseAction) action.get(event.getPacket());
			if(useAction != PacketPlayInUseEntity.EnumEntityUseAction.ATTACK) return;

			int id = (int) entityID.get(event.getPacket());

			spawnMap.values().removeIf(stand -> {
				if(stand.getId() == id) {
					removeEntity(event.getPlayer(), stand);
					ActionBarManager.sendActionBarMessage(event.getPlayer(), "&aSpawn point removed!", 1);
					GameSound.ITEM_BREAK.play(event.getPlayer());
					return true;
				}
				return false;
			});

			if(!profile.getManagerList().contains(event.getPlayer().getUniqueId()) && PixelParty.owner != event.getPlayer()) return;

			colorList.removeIf(stand -> {
				if(stand.getId() == id) {
					removeEntityForAll(stand);
					ActionBarManager.sendActionBarMessage(event.getPlayer(), "&cColor point removed!", 1);
					GameSound.ITEM_BREAK.play(event.getPlayer());
					return true;
				}
				return false;
			});

		} catch(NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@EventHandler
	public void onButtonInteract(PlayerInteractEvent event) {
		if(event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) return;
		if(PixelParty.serverType != ServerType.PRACTICE) return;
		if(Misc.isAirOrNull(event.getItem())) return;

		PracticeProfile profile = PixelParty.gameManager.practiceProfile;

		NBTItem nbtItem = new NBTItem(event.getItem());

		NBTTag[] managerTags = {NBTTag.DECREASE_SPEED, NBTTag.PAUSE_GAME, NBTTag.INCREASE_SPEED, NBTTag.FLOOR_SELECT, NBTTag.TOGGLE_POWERUPS, NBTTag.COLOR_SELECT};
		for(NBTTag tag : managerTags) {
			if(nbtItem.hasKey(tag.getRef()) && !profile.getManagerList().contains(event.getPlayer().getUniqueId()) && PixelParty.owner != event.getPlayer()) {
				ActionBarManager.sendActionBarMessage(event.getPlayer(), "&cYou must be a manager to do this!", 1);
				GameSound.ERROR.play(event.getPlayer());
				return;
			}
		}

		if(PixelParty.owner != event.getPlayer() && nbtItem.hasKey(NBTTag.PRIVACY_SETTINGS.getRef())) {
			ActionBarManager.sendActionBarMessage(event.getPlayer(), "&cYou must be the server owner to do this!", 1);
			GameSound.ERROR.play(event.getPlayer());
			return;
		}

		if(nbtItem.hasKey(NBTTag.PRACTICE_SETTINGS.getRef())) {
			giveSettingsItems(event.getPlayer());
			GameSound.CLICK.play(event.getPlayer());
			settingsPlayers.add(event.getPlayer().getUniqueId());

			colorList.forEach(armorStand -> spawnEntity(event.getPlayer(), armorStand, Skin.COLOR_SELECT));
			spawnEntity(event.getPlayer(), spawnMap.get(event.getPlayer().getUniqueId()), Skin.SPAWN_SELECT);

		} else if(nbtItem.hasKey(NBTTag.EXIT_SETTINGS.getRef())) {
			removeSettingsItems(event.getPlayer());
			GameSound.CLICK.play(event.getPlayer());
			settingsPlayers.remove(event.getPlayer().getUniqueId());

			for(EntityArmorStand entityArmorStand : colorList) {
				removeEntity(event.getPlayer(), entityArmorStand);
			}

			removeEntity(event.getPlayer(), spawnMap.get(event.getPlayer().getUniqueId()));

		} else if(nbtItem.hasKey(NBTTag.FLOOR_SELECT.getRef())) {
			FloorSelectGUI gui = new FloorSelectGUI(event.getPlayer());
			gui.open();
		} else if(nbtItem.hasKey(NBTTag.TOGGLE_POWERUPS.getRef())) {
			boolean powerups = !profile.powerupsEnabled();
			profile.setPowerupsEnabled(powerups);
			ActionBarManager.sendActionBarMessage(event.getPlayer(), "&3" + (powerups ? "Enabled" : "Disabled"), 1);
			GameSound.CLICK.play(event.getPlayer());
		} else if(nbtItem.hasKey(NBTTag.DECREASE_SPEED.getRef())) {
			profile.setRound(Math.max(1, profile.getRound() - 2));
			ActionBarManager.sendActionBarMessage(event.getPlayer(), "&eRound Speed: " +
					PixelParty.gameManager.getRoundSpeed(profile.getRound()), 1);
			GameSound.CLICK.play(event.getPlayer());
		} else if(nbtItem.hasKey(NBTTag.INCREASE_SPEED.getRef())) {
			profile.setRound(Math.min(GameManager.MAX_ROUNDS, profile.getRound() + 2));
			ActionBarManager.sendActionBarMessage(event.getPlayer(), "&eRound Speed: " +
					PixelParty.gameManager.getRoundSpeed(profile.getRound()), 1);
			GameSound.CLICK.play(event.getPlayer());
		} else if(nbtItem.hasKey(NBTTag.PAUSE_GAME.getRef())) {
			ActionBarManager.sendActionBarMessage(event.getPlayer(), "&e" + (profile.isPaused() ? "Resumed" : "Paused"), 1);
			profile.setPaused(!profile.isPaused());
			GameSound.CLICK.play(event.getPlayer());
		} if(nbtItem.hasKey(NBTTag.PRIVACY_SETTINGS.getRef())) {
			PrivacyGUI gui = new PrivacyGUI(event.getPlayer());
			gui.open();
		}
	}

	private void giveSettingsItems(Player player) {
		ItemStack exit = new ItemStack(Material.BARRIER);
		ItemMeta exitMeta = exit.getItemMeta();
		exitMeta.setDisplayName(ChatColor.RED + "Exit Settings");
		exit.setItemMeta(exitMeta);
		NBTItem exitNBT = new NBTItem(exit, true);
		exitNBT.setBoolean(NBTTag.EXIT_SETTINGS.getRef(), true);

		ItemStack floor = new ItemStack(Material.PAPER);
		ItemMeta floorMeta = floor.getItemMeta();
		floorMeta.setDisplayName(ChatColor.GREEN + "Floor Selection");
		floor.setItemMeta(floorMeta);
		NBTItem floorNBT = new NBTItem(floor, true);
		floorNBT.setBoolean(NBTTag.FLOOR_SELECT.getRef(), true);

		ItemStack color = SkinUtil.getPlayerSkull(Skin.COLOR_SELECT);
		ItemMeta colorMeta = color.getItemMeta();
		colorMeta.setDisplayName(ChatColor.RED + "Color Selection (Place)");
		color.setItemMeta(colorMeta);
		NBTItem colorNBT = new NBTItem(color, true);
		colorNBT.setBoolean(NBTTag.COLOR_SELECT.getRef(), true);

		ItemStack spawn = SkinUtil.getPlayerSkull(Skin.SPAWN_SELECT);
		ItemMeta spawnMeta = spawn.getItemMeta();
		spawnMeta.setDisplayName(ChatColor.GREEN + "Spawn Selection (Place)");
		spawn.setItemMeta(spawnMeta);
		NBTItem spawnNBT = new NBTItem(spawn, true);
		spawnNBT.setBoolean(NBTTag.SPAWN_SELECT.getRef(), true);

		ItemStack speedDecrease = SkinUtil.getPlayerSkull(Skin.MINUS);
		ItemMeta speedDecreaseMeta = speedDecrease.getItemMeta();
		speedDecreaseMeta.setDisplayName(ChatColor.YELLOW + "Decrease Speed");
		speedDecrease.setItemMeta(speedDecreaseMeta);
		NBTItem speedDecreaseNBT = new NBTItem(speedDecrease, true);
		speedDecreaseNBT.setBoolean(NBTTag.DECREASE_SPEED.getRef(), true);

		ItemStack speedIncrease = SkinUtil.getPlayerSkull(Skin.PLUS);
		ItemMeta speedIncreaseMeta = speedIncrease.getItemMeta();
		speedIncreaseMeta.setDisplayName(ChatColor.YELLOW + "Increase Speed");
		speedIncrease.setItemMeta(speedIncreaseMeta);
		NBTItem speedIncreaseNBT = new NBTItem(speedIncrease, true);
		speedIncreaseNBT.setBoolean(NBTTag.INCREASE_SPEED.getRef(), true);

		ItemStack pause = SkinUtil.getPlayerSkull(Skin.PAUSE);
		ItemMeta pauseMeta = pause.getItemMeta();
		pauseMeta.setDisplayName(ChatColor.YELLOW + "Pause Game");
		pause.setItemMeta(pauseMeta);
		NBTItem pauseNBT = new NBTItem(pause, true);
		pauseNBT.setBoolean(NBTTag.PAUSE_GAME.getRef(), true);

		ItemStack powerup = new ItemStack(Material.BEACON);
		ItemMeta powerupMeta = powerup.getItemMeta();
		powerupMeta.setDisplayName(ChatColor.DARK_AQUA + "Toggle Powerups");
		powerup.setItemMeta(powerupMeta);
		NBTItem powerupNBT = new NBTItem(powerup, true);
		powerupNBT.setBoolean(NBTTag.TOGGLE_POWERUPS.getRef(), true);

		player.getInventory().setItem(8, exit);
		givePrivacyItemStack(player);
		player.getInventory().setItem(6, powerup);
		player.getInventory().setItem(5, speedIncrease);
		player.getInventory().setItem(4, pause);
		player.getInventory().setItem(3, speedDecrease);
		player.getInventory().setItem(2, floor);
		player.getInventory().setItem(1, color);
		player.getInventory().setItem(0, spawn);
	}

	public static void givePrivacyItemStack(Player player) {
		ItemStack privacy = SkinUtil.getPlayerSkull(Skin.PRIVACY);
		ItemMeta privacyMeta = privacy.getItemMeta();
		privacyMeta.setDisplayName(ChatColor.AQUA + "Privacy Settings");
		privacy.setItemMeta(privacyMeta);
		NBTItem privacyNBT = new NBTItem(privacy, true);
		privacyNBT.setBoolean(NBTTag.PRIVACY_SETTINGS.getRef(), true);

		player.getInventory().setItem(7, privacy);
	}

	public void removeSettingsItems(Player player) {
		for(int i = 0; i < 9; i++) {
			player.getInventory().setItem(i, new ItemStack(Material.AIR));
		}

		MusicManager.giveHeads(player);
		giveSettingsItem(player);

		player.getInventory().setItem(8, new ItemStack(Material.AIR));
		player.updateInventory();
	}


	private static ItemStack getSettingsItem() {
		ItemStack item = new ItemStack(Material.REDSTONE_COMPARATOR);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.RED + "Practice Settings");
		item.setItemMeta(meta);

		NBTItem nbtItem = new NBTItem(item, true);
		nbtItem.setBoolean(NBTTag.PRACTICE_SETTINGS.getRef(), true);
		return item;
	}

	public static void removeEntityForAll(EntityArmorStand entity) {
		for(Player player : Bukkit.getOnlinePlayers()) removeEntity(player, entity);
	}

	public static void removeEntity(Player player, EntityArmorStand entity) {
		if(entity == null) return;
		PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(entity.getId());
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
	}


	public static void spawnEntityForAll(EntityArmorStand entity, Skin skin) {
		settingsPlayers.forEach(uuid -> {
			Player player = Bukkit.getPlayer(uuid);
			if(player != null) spawnEntity(player, entity, skin);
		});
	}

	public static void spawnEntity(Player player, EntityArmorStand entity, Skin skin) {
		if(entity == null) return;
		PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving(entity);
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);

		PacketPlayOutEntityEquipment equipment = new PacketPlayOutEntityEquipment(entity.getId(),
				4, CraftItemStack.asNMSCopy(SkinUtil.getPlayerSkull(skin)));
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(equipment);
	}

}
