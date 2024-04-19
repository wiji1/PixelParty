package dev.wiji.pixelparty.controllers;

import dev.wiji.pixelparty.PixelParty;
import dev.wiji.pixelparty.enums.ServerType;
import dev.wiji.pixelparty.events.PacketReceiveEvent;
import dev.wiji.pixelparty.events.PacketSendEvent;
import dev.wiji.pixelparty.misc.Pair;
import dev.wiji.pixelparty.objects.PacketPlayer;
import dev.wiji.pixelparty.playerdata.PixelPlayer;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

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
	public void onChunkPacketSend(PacketSendEvent event) {
		if(!event.getPacketType().name().equals("MAP_CHUNK")) return;
		if(PixelParty.serverType == ServerType.LOBBY) return;
		PacketPlayOutMapChunk packet = (PacketPlayOutMapChunk) event.getPacket();
		PixelPlayer pixelPlayer = PixelPlayer.getPixelPlayer(event.getPlayer());

//		Bukkit.broadcastMessage("MAP CHUNK - " + event.getPlayer().getName());

		try {
			Field chunkXField = packet.getClass().getDeclaredField("a");
			Field chunkZField = packet.getClass().getDeclaredField("b");

			chunkXField.setAccessible(true);
			chunkZField.setAccessible(true);

			int chunkX = chunkXField.getInt(packet);
			int chunkZ = chunkZField.getInt(packet);

			Field chunkDataField = packet.getClass().getDeclaredField("c");
			Field chunkBooleanField = packet.getClass().getDeclaredField("d");

			chunkDataField.setAccessible(true);

			PacketPlayOutMapChunk.ChunkMap chunkMap = (PacketPlayOutMapChunk.ChunkMap) chunkDataField.get(packet);
			PacketPlayOutMapChunk.ChunkMap newChunkMap = new PacketPlayOutMapChunk.ChunkMap();

			int id = pixelPlayer.woolFloor ? 35 : 159;

			newChunkMap.b = chunkMap.b;
			newChunkMap.a = modifyChunk(chunkMap.b, chunkMap.a, chunkX, chunkZ, id);

			PacketPlayOutMapChunk packetCopy = new PacketPlayOutMapChunk();
			chunkXField.set(packetCopy, chunkX);
			chunkZField.set(packetCopy, chunkZ);
			chunkDataField.set(packetCopy, newChunkMap);
			chunkBooleanField.set(packetCopy, chunkBooleanField.get(packet));

			event.setPacket(packetCopy);

		} catch(NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@EventHandler
	public void onBulkChunkPacketSend(PacketSendEvent event) {
		if(!event.getPacketType().name().equals("MAP_CHUNK_BULK")) return;
		if(PixelParty.serverType == ServerType.LOBBY) return;

		PacketPlayOutMapChunkBulk packet = (PacketPlayOutMapChunkBulk) event.getPacket();
		PixelPlayer pixelPlayer = PixelPlayer.getPixelPlayer(event.getPlayer());

		try {
			Field xField = packet.getClass().getDeclaredField("a");
			Field zField = packet.getClass().getDeclaredField("b");
			Field flagsField = packet.getClass().getDeclaredField("d");
			Field chunkDataField = packet.getClass().getDeclaredField("c");
			Field worldField = packet.getClass().getDeclaredField("world");
			xField.setAccessible(true);
			zField.setAccessible(true);
			flagsField.setAccessible(true);
			chunkDataField.setAccessible(true);
			worldField.setAccessible(true);

			int[] chunkX = (int[]) xField.get(packet);
			int[] chunkZ = (int[]) zField.get(packet);
			PacketPlayOutMapChunk.ChunkMap[] chunkMap = (PacketPlayOutMapChunk.ChunkMap[]) chunkDataField.get(packet);
			PacketPlayOutMapChunk.ChunkMap[] newChunkMap = new PacketPlayOutMapChunk.ChunkMap[chunkMap.length];

			int id = pixelPlayer.woolFloor ? 35 : 159;

			for(int i = 0; i < chunkMap.length; i++) {
				PacketPlayOutMapChunk.ChunkMap map = chunkMap[i];

				PacketPlayOutMapChunk.ChunkMap newMap = new PacketPlayOutMapChunk.ChunkMap();
				newMap.b = map.b;
				newMap.a = modifyChunk(map.b, map.a, chunkX[i], chunkZ[i], id);

				newChunkMap[i] = newMap;
			}

			PacketPlayOutMapChunkBulk packetCopy = new PacketPlayOutMapChunkBulk();
			xField.set(packetCopy, chunkX);
			zField.set(packetCopy, chunkZ);
			flagsField.set(packetCopy, flagsField.get(packet));
			chunkDataField.set(packetCopy, newChunkMap);
			worldField.set(packetCopy, worldField.get(packet));

			event.setPacket(packetCopy);
		} catch(NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@EventHandler
	public void onBlockUpdate(PacketSendEvent event) {
		if(!event.getPacketType().name().equals("BLOCK_CHANGE")) return;
		if(PixelParty.serverType == ServerType.LOBBY) return;
		PacketPlayOutBlockChange packet = (PacketPlayOutBlockChange) event.getPacket();
		PixelPlayer pixelPlayer = PixelPlayer.getPixelPlayer(event.getPlayer());

		FloorManager floorManager = PixelParty.gameManager.floorManager;

		try {
			Field blockDataField = packet.getClass().getDeclaredField("block");
			blockDataField.setAccessible(true);
			Field blockPositionField = packet.getClass().getDeclaredField("a");
			blockPositionField.setAccessible(true);

			BlockPosition blockPosition = (BlockPosition) blockPositionField.get(packet);

			Block block = Bukkit.getWorld("world").getBlockAt(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
			if(block.getType() == Material.AIR) return;
			if(!floorManager.isOnFloor(block.getLocation())) return;

			boolean isBlock = !floorManager.shouldBeAir(block);

			int blockID = pixelPlayer.woolFloor ? 35 : 159;

			int id = isBlock ? blockID : 0;
			byte color = isBlock ? floorManager.getBlockColor(block) : 0;

			try {
				net.minecraft.server.v1_8_R3.Block nmsBlock = net.minecraft.server.v1_8_R3.Block.getById(id);

				blockDataField.set(packet, nmsBlock.fromLegacyData(color));
			} catch(Exception e) {
				throw new RuntimeException(e);
			}



		} catch(NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@EventHandler
	public void onMultiBlockChange(PacketSendEvent event) {
		if(!event.getPacketType().name().equals("MULTI_BLOCK_CHANGE")) return;
		PacketPlayOutMultiBlockChange packet = (PacketPlayOutMultiBlockChange) event.getPacket();
		PixelPlayer pixelPlayer = PixelPlayer.getPixelPlayer(event.getPlayer());

		FloorManager floorManager = PixelParty.gameManager.floorManager;

		try {
			Field coordinatesField = packet.getClass().getDeclaredField("a");
			Field blockDataField = packet.getClass().getDeclaredField("b");
			coordinatesField.setAccessible(true);
			blockDataField.setAccessible(true);

			ChunkCoordIntPair chunkCoord = (ChunkCoordIntPair) coordinatesField.get(packet);
			Chunk chunk = Bukkit.getWorld("world").getChunkAt(chunkCoord.x, chunkCoord.z);

			PacketPlayOutMultiBlockChange.MultiBlockChangeInfo[] blockChanges = (PacketPlayOutMultiBlockChange.MultiBlockChangeInfo[]) blockDataField.get(packet);

			List<PacketPlayOutMultiBlockChange.MultiBlockChangeInfo> newBlockChange = new ArrayList<>();

			int blockID = pixelPlayer.woolFloor ? 35 : 159;

			for(PacketPlayOutMultiBlockChange.MultiBlockChangeInfo blockChange : blockChanges) {
				BlockPosition blockPosition = blockChange.a();
				Block block = chunk.getBlock(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());

				if(!floorManager.isOnFloor(block.getLocation())) continue;
				boolean isAir = block.getType() == Material.AIR;


				byte color = isAir ? 0 : floorManager.getBlockColor(block);
				int material = isAir ? 0 : blockID;
				net.minecraft.server.v1_8_R3.Block nmsBlock = net.minecraft.server.v1_8_R3.Block.getById(material);

				short blockDataValue = (short) ((block.getX() & 15) << 12 | (block.getZ() & 15) << 8 | block.getY());
				newBlockChange.add(packet.new MultiBlockChangeInfo(blockDataValue, nmsBlock.fromLegacyData(color)));
			}

			blockDataField.set(packet, newBlockChange.toArray(new PacketPlayOutMultiBlockChange.MultiBlockChangeInfo[0]));

		} catch(NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public byte[] modifyChunk(int bitmask, byte[] buffer, int chunkX, int chunkZ, int blockId) {
		FloorManager floorManager = PixelParty.gameManager.floorManager;

		int index = 0;

		for(int i = 0; i < 16; ++i) {
			if((bitmask & 1 << i) == 0) continue;
			for(int y = 0; y < 16; ++y) {

				if(y != 0) {
					++index;
					continue;
				}

				for(int z = 0; z < 16; ++z) {
					for(int x = 0; x < 16; ++x) {
						if(index < buffer.length) {
							int blockData = buffer[index << 1] & 255 | (buffer[(index << 1) + 1] & 255) << 8;
//							int blockId = blockData >> 4;
//							int metadata = blockData & 15;

							Block block = Bukkit.getWorld("world").getBlockAt(x + (chunkX * 16), y, z + (chunkZ * 16));
							if(!floorManager.isOnFloor(block.getLocation()) || block.getX() == 32) {
								++index;
								continue;
							}

							boolean shouldBeAir = floorManager.shouldBeAir(block);
							byte color = floorManager.getBlockColor(block);
							if(color == -1) {
								++index;
								continue;

							}

							int newId = shouldBeAir ? 0 : blockId;
							int newMetadata = shouldBeAir ? 0 : color;

							int newBlockData = (newId << 4) | (newMetadata & 15);
							buffer[index << 1] = (byte) (newBlockData & 255);
							buffer[(index << 1) + 1] = (byte) (newBlockData >> 8 & 255);
						}
						++index;
					}
				}
			}

		}

		return buffer;
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

			PixelPlayer pixelPlayer = PixelPlayer.getPixelPlayer(event.getPlayer());
			for(Integer xCoord : xCoords) {
				for(Integer zCoord : zCoords) {
					Pair<Integer, Integer> pair = new Pair<>(xCoord, zCoord);
					if(!pixelPlayer.loadedChunks.contains(pair)) {
						pixelPlayer.loadedChunks.add(pair);

						checkToSpawnNPCs(event.getPlayer(), xCoord, zCoord);
					}
				}
			}

			chunkField.set(event.getPacket(), chunkArray);

		} catch(IllegalAccessException e) {
			throw new RuntimeException(e);
		}

	}

	public void checkToSpawnNPCs(Player player, int x, int z) {
		for(PacketPlayer lobbyNPC : LobbyManager.lobbyNPCs) {
			Chunk chunk = lobbyNPC.getLocation().getChunk();
			if(chunk.getX() == x && chunk.getZ() == z) {

				new BukkitRunnable() {
					@Override
					public void run() {
						lobbyNPC.spawnForPlayer(player);
						System.out.println("Spawned NPC for " + player.getName());
					}
				}.runTaskLater(PixelParty.INSTANCE, 5L);
			}
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
