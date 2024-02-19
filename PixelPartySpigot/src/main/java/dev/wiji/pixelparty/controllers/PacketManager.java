package dev.wiji.pixelparty.controllers;

import dev.wiji.pixelparty.PixelParty;
import dev.wiji.pixelparty.enums.ServerType;
import dev.wiji.pixelparty.events.PacketSendEvent;
import dev.wiji.pixelparty.objects.PacketPlayer;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PacketManager implements Listener {

	@EventHandler
	public void onMetadataPacketSent(PacketSendEvent event) {
		if(!Objects.equals(event.getPacketType().name(), "ENTITY_METADATA")) return;

		if(PixelParty.serverType == ServerType.LOBBY) return;

		GameManager.GameState gameState = PixelParty.gameManager.gameState;
		if(gameState == GameManager.GameState.LOBBY) return;

		//If player is alive, can only see ghosts of other alice players
		//If player is dead, other dead players are ghosts, everyone else is visible
		//If the game is over, everyone is a ghost

		try {
			Field watcherField = event.getPacket().getClass().getDeclaredField("b");
			watcherField.setAccessible(true);

			List<DataWatcher.WatchableObject> originalWatchableObjects = (List<DataWatcher.WatchableObject>) watcherField.get(event.getPacket());
			List<DataWatcher.WatchableObject> watchableObjects = new ArrayList<>();

			Field entityField = event.getPacket().getClass().getDeclaredField("a");
			entityField.setAccessible(true);
			int entityId = entityField.getInt(event.getPacket());

			Entity entity = getEntity(entityId);
			if(!(entity instanceof Player)) return;
			Player targetPlayer = (Player) entity;

			boolean receiverIsAlive = PixelParty.gameManager.alivePlayers.contains(event.getPlayer().getUniqueId());
			boolean targetIsAlive = PixelParty.gameManager.alivePlayers.contains(targetPlayer.getUniqueId());

			boolean isVisible = (!receiverIsAlive && targetIsAlive || PixelParty.serverType == ServerType.LOBBY);
			if(entity.getEntityId() == event.getPlayer().getEntityId() && gameState == GameManager.GameState.INGAME && targetIsAlive) isVisible = true;

			for(DataWatcher.WatchableObject watchableObject : originalWatchableObjects) {
				Field type = watchableObject.getClass().getDeclaredField("a");
				Field index = watchableObject.getClass().getDeclaredField("b");
				Field value = watchableObject.getClass().getDeclaredField("c");

				type.setAccessible(true);
				index.setAccessible(true);
				value.setAccessible(true);

				int typeData = type.getInt(watchableObject);
				int indexData = index.getInt(watchableObject);
				Object valueData = value.get(watchableObject);

				if(indexData == 0) valueData = isVisible ? ((byte) ((byte) valueData & ~0x20)) : ((byte) 0x20);

				DataWatcher.WatchableObject watchable = new DataWatcher.WatchableObject(typeData, indexData, valueData);

				watchableObjects.add(watchable);
			}

			watcherField.set(event.getPacket(), watchableObjects);

		} catch (NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}


	@EventHandler
	public void onPlayerInfoPacketSent(PacketSendEvent event) {
		if(!Objects.equals(event.getPacketType().name(), "PLAYER_INFO")) return;

		Object packet = event.getPacket();

		try {
			Field actionField = packet.getClass().getDeclaredField("a");
			actionField.setAccessible(true);

			Field playerInfoField = packet.getClass().getDeclaredField("b");
			playerInfoField.setAccessible(true);

			Field playerListField = PacketPlayOutPlayerInfo.PlayerInfoData.class.getDeclaredField("e");
			playerListField.setAccessible(true);

			List<PacketPlayOutPlayerInfo.PlayerInfoData> playerInfoData = (List<PacketPlayOutPlayerInfo.PlayerInfoData>) playerInfoField.get(packet);

			PacketPlayOutPlayerInfo.EnumPlayerInfoAction action = (PacketPlayOutPlayerInfo.EnumPlayerInfoAction) actionField.get(packet);

			if(action == PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER) return;

			List<PacketPlayOutPlayerInfo.PlayerInfoData> toRemove = new ArrayList<>();
			for(PacketPlayOutPlayerInfo.PlayerInfoData playerInfoDatum : playerInfoData) {
				for(PacketPlayer lobbyNPC : LobbyManager.lobbyNPCs) {
					if(lobbyNPC.spawningPlayers.contains(event.getPlayer().getUniqueId())) continue;

					if(lobbyNPC.uuid.equals(playerInfoDatum.a().getId())) {
						toRemove.add(playerInfoDatum);
					}
				}
			}

			playerInfoData.removeAll(toRemove);
			playerInfoField.set(packet, playerInfoData);


		} catch(NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@EventHandler
	public void onBulkTerrainPacketSendEvent(PacketSendEvent event) {
		if(PixelParty.serverType != ServerType.LOBBY) return;
		if(!Objects.equals(event.getPacketType().name(), "MAP_CHUNK_BULK")) return;

		Field chunkField;
		Field x;
		Field z;

		try {
			chunkField = PacketPlayOutMapChunkBulk.class.getDeclaredField("c");
			chunkField.setAccessible(true);
			x = PacketPlayOutMapChunkBulk.class.getDeclaredField("a");
			x.setAccessible(true);
			z = PacketPlayOutMapChunkBulk.class.getDeclaredField("b");
			z.setAccessible(true);
		} catch(NoSuchFieldException e) {
			throw new RuntimeException(e);
		}

		List<Integer> xCoords = new ArrayList<>();
		List<Integer> zCoords = new ArrayList<>();
		List<PacketPlayOutMapChunk.ChunkMap> chunkList = new ArrayList<>();

		try {
			PacketPlayOutMapChunk.ChunkMap[] chunks = (PacketPlayOutMapChunk.ChunkMap[]) chunkField.get(event.getPacket());

			for(int chunkNum = 0; chunkNum < chunks.length; chunkNum++) {

				int xVal = ((int[]) x.get(event.getPacket()))[chunkNum];
				int zVal = ((int[]) z.get(event.getPacket()))[chunkNum];

				if(Math.abs(xVal * 16) < 112 && Math.abs(zVal * 16) < 112) {
					xCoords.add(((int[]) x.get(event.getPacket()))[chunkNum]);
					zCoords.add(((int[]) z.get(event.getPacket()))[chunkNum]);
					chunkList.add(chunks[chunkNum]);
				}
			}

			x.set(event.getPacket(), xCoords.stream().mapToInt(i -> i).toArray());
			z.set(event.getPacket(), zCoords.stream().mapToInt(i -> i).toArray());

			PacketPlayOutMapChunk.ChunkMap[] chunkArray = new PacketPlayOutMapChunk.ChunkMap[chunkList.size()];

			for(int i = 0; i < chunkList.size(); i++) {
				chunkArray[i] = chunkList.get(i);
			}

			chunkField.set(event.getPacket(), chunkArray);

		} catch(IllegalAccessException e) {
			throw new RuntimeException(e);
		}

	}

	@EventHandler
	public void onTerrainPacketSendEvent(PacketSendEvent event) {
		if(PixelParty.serverType != ServerType.LOBBY) return;
		if(!Objects.equals(event.getPacketType().name(), "MAP_CHUNK")) return;

		Field x;
		Field z;

		try {
			x = PacketPlayOutMapChunk.class.getDeclaredField("a");
			x.setAccessible(true);
			z = PacketPlayOutMapChunk.class.getDeclaredField("b");
			z.setAccessible(true);
		} catch(NoSuchFieldException e) {
			throw new RuntimeException(e);
		}

		try {
			int xVal = (int) x.get(event.getPacket());
			int zVal = (int) z.get(event.getPacket());

			if(Math.abs(xVal * 16) > 112 || Math.abs(zVal * 16) > 112) event.setCancelled(true);

		} catch(IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public Entity getEntity(int entityID) {
		for(Entity entity : Bukkit.getWorld("world").getEntities()) {
			if(entity.getEntityId() == entityID) return entity;
		}
		return  null;
	}

	public static void sendTabHeaders(Player player) {

		PacketPlayOutPlayerListHeaderFooter packet = new PacketPlayOutPlayerListHeaderFooter();
		try {
			Field headerField = packet.getClass().getDeclaredField("a");
			Field footerField = packet.getClass().getDeclaredField("b");

			headerField.setAccessible(true);
			footerField.setAccessible(true);

			String header = ChatColor.translateAlternateColorCodes('&', "&bYou are playing on &e&lMC.WIJI.DEV");
			String footer = ChatColor.translateAlternateColorCodes('&', "&ehttps://wiji.dev");

			headerField.set(packet, IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + header + "\"}"));
			footerField.set(packet, IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + footer + "\"}"));


			((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);

		} catch(NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

}
