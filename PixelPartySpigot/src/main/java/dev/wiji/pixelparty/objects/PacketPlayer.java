package dev.wiji.pixelparty.objects;

import com.mojang.authlib.GameProfile;
import dev.wiji.pixelparty.PixelParty;
import dev.wiji.pixelparty.enums.Skin;
import dev.wiji.pixelparty.events.PacketReceiveEvent;
import dev.wiji.pixelparty.holograms.Hologram;
import dev.wiji.pixelparty.util.SkinUtil;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.scoreboard.CraftScoreboard;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.util.*;

public abstract class PacketPlayer implements Listener {

	private static final Map<UUID, List<String>> spawnedPlayers = new HashMap<>();

	public String name;
	public Skin skin;

	public UUID uuid;

	public Hologram hologram;

	public int id;

	public final List<UUID> spawningPlayers = new ArrayList<>();
	private final List<UUID> cooldownList = new ArrayList<>();
	//TODO: Move this to other cooldown system

	public PacketPlayer(Skin skin) {
		this(null, skin);
	}

	public PacketPlayer(String name, Skin skin) {
		PixelParty.INSTANCE.getServer().getPluginManager().registerEvents(this, PixelParty.INSTANCE);

		this.name = name;
		this.skin = skin;

		this.uuid = UUID.randomUUID();
		this.id = (int) (Math.random() * -1000000);

		this.hologram = new Hologram(getLocation().clone().add(0, 1.8, 0)) {
			@Override
			public List<String> getStrings(Player player) {
				List<String> hologramText = new ArrayList<>();
				if(name != null) hologramText.add(name);
				hologramText.addAll(getHologramText());

				return hologramText;
			}
		};
	}


	//Can be overridden
	public List<String> getHologramText() {
		return new ArrayList<>();
	}

	public abstract Location getLocation();

	public abstract void onRightClick(Player player);

	public void spawn() {
		for(Player onlinePlayer : Bukkit.getOnlinePlayers()) {
			spawnForPlayer(onlinePlayer);
		}
	}

	public void spawnForPlayer(Player player) {
		WorldServer server = ((CraftWorld) player.getLocation().getWorld()).getHandle();
		World world = ((CraftWorld) player.getWorld()).getHandle();

		GameProfile gameProfile = new GameProfile(uuid, name);
		SkinUtil.skinProfile(gameProfile, skin.getTexture(), skin.getSignature());

		CustomPlayer customPlayer = new CustomPlayer(MinecraftServer.getServer(), server, gameProfile, new PlayerInteractManager(world), id);

		Location loc = getLocation();

		customPlayer.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
		customPlayer.yaw = loc.getYaw();
		customPlayer.pitch = loc.getPitch();

		PacketPlayOutPlayerInfo pi = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, customPlayer);
		PacketPlayOutNamedEntitySpawn spawn = new PacketPlayOutNamedEntitySpawn(customPlayer);
		PlayerConnection co = ((CraftPlayer) player).getHandle().playerConnection;
		co.sendPacket(pi);
		co.sendPacket(spawn);

		DataWatcher watcher = customPlayer.getDataWatcher();
		watcher.watch(10, (byte) 126);

		PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata(customPlayer.getId(), watcher, true);
		co.sendPacket(metadata);

		PacketPlayOutEntityHeadRotation headRotation = new PacketPlayOutEntityHeadRotation(customPlayer, (byte) (loc.getYaw() * 256 / 360));
		co.sendPacket(headRotation);

		ScoreboardTeam team;
		team = ((CraftScoreboard) Bukkit.getScoreboardManager().getMainScoreboard()).getHandle().getTeam(player.getName());
		if(team == null) {
			team = new ScoreboardTeam(((CraftScoreboard) Bukkit.getScoreboardManager().getMainScoreboard()).getHandle(), player.getName());
			team.setNameTagVisibility(ScoreboardTeamBase.EnumNameTagVisibility.NEVER);
		}

		ArrayList<String> playerToAdd = new ArrayList<>(spawnedPlayers.getOrDefault(player.getUniqueId(), new ArrayList<>()));
		playerToAdd.add(customPlayer.getName());

		spawnedPlayers.put(player.getUniqueId(), playerToAdd);

		co.sendPacket(new PacketPlayOutScoreboardTeam(team, 1));
		co.sendPacket(new PacketPlayOutScoreboardTeam(team, 0));
		co.sendPacket(new PacketPlayOutScoreboardTeam(team, playerToAdd, 3));

		spawningPlayers.add(player.getUniqueId());
		new BukkitRunnable() {
			@Override
			public void run() {
				spawningPlayers.remove(player.getUniqueId());

				PacketPlayOutPlayerInfo update = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, customPlayer);
				co.sendPacket(update);
			}
		}.runTaskLater(PixelParty.INSTANCE, 40);
	}

	@EventHandler
	public void onRightClick(PacketReceiveEvent event) {
		if(!Objects.equals(event.getPacketType().name(), "USE_ENTITY")) return;

		try {
			Field action = event.getPacket().getClass().getDeclaredField("action");
			action.setAccessible(true);

			Field entityID = event.getPacket().getClass().getDeclaredField("a");
			entityID.setAccessible(true);

			PacketPlayInUseEntity.EnumEntityUseAction useAction = (PacketPlayInUseEntity.EnumEntityUseAction) action.get(event.getPacket());
			if(useAction == PacketPlayInUseEntity.EnumEntityUseAction.ATTACK) return;

			if(cooldownList.contains(event.getPlayer().getUniqueId())) return;

			cooldownList.add(event.getPlayer().getUniqueId());
			Bukkit.getScheduler().runTaskLater(PixelParty.INSTANCE, () -> cooldownList.remove(event.getPlayer().getUniqueId()), 20 * 2);

			int id = (int) entityID.get(event.getPacket());
			if(id == this.id) onRightClick(event.getPlayer());

		} catch(NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public static class CustomPlayer extends EntityPlayer {
		public int entityID;
		public CustomPlayer(MinecraftServer srv, WorldServer world, GameProfile game, PlayerInteractManager interact, int entityID) {
			super(srv, world, game, interact);

			this.entityID = entityID;
		}

		@Override
		public int getId() {
			return entityID;
		}
	}
}
