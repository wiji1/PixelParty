package dev.wiji.pixelparty.controllers;

import dev.wiji.pixelparty.PixelParty;
import dev.wiji.pixelparty.enums.ServerType;
import dev.wiji.pixelparty.objects.Floor;
import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.PacketPlayOutMapChunk;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftChunk;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;

public class FloorManager {
	public static Location pasteLocation = new Location(Bukkit.getWorld("world"), -32, 0, -32);

	public List<Floor> floors = new ArrayList<>();
	public Floor startFloor;
	public Floor endFloor;

	public boolean isVoid = false;

	public void registerFloor(Floor floor) {
		floors.add(floor);
	}

	public Floor currentFloor;
	public byte[] currentFloorBlueprint;
	public List<Byte> availableColors = new ArrayList<>();

	public Byte chosenColor;

	public byte[][] randomPaintPattern = {
			{1, 0, 0, 0, 0, 1, 0, 0, 0, 1,},
			{0, 1, 0, 0, 1, 0, 1, 0, 1, 0,},
			{0, 0, 1, 0, 1, 1, 0, 1, 0, 0,},
			{0, 1, 0, 1, 1, 1, 1, 0, 1, 1,},
			{1, 0, 1, 1, 1, 1, 1, 1, 1, 0,},
			{0, 0, 1, 1, 1, 1, 1, 1, 0, 0,},
			{0, 0, 0, 1, 1, 1, 1, 0, 0, 0,},
			{1, 0, 0, 1, 0, 1, 0, 1, 0, 1,},
			{0, 1, 0, 0, 1, 0, 0, 1, 0, 0,},
			{0, 1, 0, 0, 1, 0, 0, 1, 0, 0,},
	};

	public void chooseFloor() {
		if(PixelParty.gameManager.gameState == GameManager.GameState.ENDING) {
			this.currentFloor = endFloor;
			generateBlueprint();
			buildFloor();
			return;
		}

		if(PixelParty.gameManager.round == 1) {
			this.currentFloor = startFloor;
			generateBlueprint();
			buildFloor();
			return;
		}

		List<Floor> possibleFloors = new ArrayList<>();

		if(PixelParty.serverType == ServerType.PRACTICE) {
			for(String selectedFloor : PixelParty.gameManager.practiceProfile.getSelectedFloors()) possibleFloors.add(getFloor(selectedFloor));
		}

		if(possibleFloors.isEmpty()) possibleFloors.addAll(floors);


		Floor chosenFloor = possibleFloors.get((int) (Math.random() * possibleFloors.size()));

		while(chosenFloor == currentFloor && possibleFloors.size() > 1) {
			chosenFloor = possibleFloors.get((int) (Math.random() * possibleFloors.size()));
		}

		this.currentFloor = chosenFloor;
		generateBlueprint();
	}

	public void chooseColor() {
		List<Byte> availableColors = new ArrayList<>();
		if(PixelParty.serverType == ServerType.PRACTICE && !PracticeManager.colorList.isEmpty()) {
			for(EntityArmorStand armorStand : PracticeManager.colorList) {

				Location location = armorStand.getBukkitEntity().getLocation();
				location.setY(0);

				Block block = location.getBlock();
				availableColors.add(block.getData());
			}
		} else {
			availableColors.addAll(this.availableColors);
		}

		this.chosenColor = availableColors.get((int) (Math.random() * availableColors.size()));
	}

	public void buildFloor() {
		chosenColor = null;
		isVoid = false;

		for(int i = 0; i < 64; i++) {
			for(int j = 0; j < 64; j++) {
				int x = pasteLocation.getBlockX() + j;
				int y = pasteLocation.getBlockY();
				int z = pasteLocation.getBlockZ() + i;

				byte data = currentFloorBlueprint[i * 64 + j];
				Bukkit.getWorld("world").getBlockAt(x, y, z).setTypeIdAndData(159, data, false);
			}
		}

		updateChunks();
	}

	public void fillVoid() {
		isVoid = true;

		for(int i = 0; i < 64; i++) {
			for(int j = 0; j < 64; j++) {
				int x = pasteLocation.getBlockX() + j;
				int y = pasteLocation.getBlockY();
				int z = pasteLocation.getBlockZ() + i;

				byte data = currentFloorBlueprint[i * 64 + j];
				if(data == chosenColor) continue;

				Bukkit.getWorld("world").getBlockAt(x, y, z).setTypeId(0);
			}
		}

		updateChunks();
	}

	private void updateChunks() {
		for(Player player : Bukkit.getOnlinePlayers()) {
			int xMultiplier = (int) Math.signum(player.getLocation().getX());
			int zMultiplier = (int) Math.signum(player.getLocation().getZ());

			EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();

			for(int i = 0; i < 2; i++) {
				for(int j = 0; j < 2; j++) {
					Chunk chunk = Bukkit.getWorld("world").getChunkAt(xMultiplier * i, zMultiplier * j);
					PacketPlayOutMapChunk packet = new PacketPlayOutMapChunk(((CraftChunk) chunk).getHandle(), false, 1);

					entityPlayer.playerConnection.sendPacket(packet);
				}
			}

		}
	}

	public void randomizeBlock(Block block) {
		byte color = block.getData();
		List<Byte> possibleColors = new ArrayList<>(availableColors);

		possibleColors.removeIf(aByte -> aByte == color);
		byte newColor = possibleColors.get(new Random().nextInt(possibleColors.size()));

		changeColor(block, newColor);
	}

	public void changeColor(Block block, byte color) {
		int x = block.getX() + 32;
		int z = block.getZ() + 32;

		int index = z * 64 + x;
		if(index < 0 || index >= currentFloorBlueprint.length) return;

		currentFloorBlueprint[index] = color;
		block.setData(color);
	}

	public byte getBlockColor(Block block) {
		int x = block.getX() + 32;
		int z = block.getZ() + 32;

		return currentFloorBlueprint[z * 64 + x];
	}

	public boolean shouldBeAir(Block block) {
		if(!isVoid) return false;

		int x = block.getX() + 32;
		int z = block.getZ() + 32;

		return currentFloorBlueprint[z * 64 + x] != chosenColor;
	}

	public boolean isOnFloor(Location location) {
		if(location.getBlockY() != 0) return false;
		return location.getBlockX() >= -32 && location.getBlockX() <= 32 && location.getBlockZ() >= -32 && location.getBlockZ() <= 32;
	}

	public void generateBlueprint() {
		this.currentFloorBlueprint = currentFloor.blueprint.clone();

		List<Byte> availableColors = new ArrayList<>();

		for(int i = 0; i < currentFloor.blueprint.length; i++) {
			byte data = currentFloorBlueprint[i];

			if(availableColors.contains(data)) continue;
			availableColors.add(data);
		}

		if(currentFloor.floorType == Floor.FloorType.STATIC) {
			this.availableColors = availableColors;
			return;
		}

		Map<Byte, Byte> colorSubstitutionMap = new HashMap<>();

		if(currentFloor.floorType == Floor.FloorType.MIXED) {
			for(DyeColor mixedStaticColor : currentFloor.getMixedStaticColors()) {
				byte color = mixedStaticColor.getWoolData();
				colorSubstitutionMap.put(color, color);
				availableColors.removeAll(Collections.singletonList(color));
			}
		}

		for(Byte availableColor : availableColors) {
			byte randomColor = (byte) (Math.random() * 16);
			while(randomColor == 12 || colorSubstitutionMap.containsValue(randomColor)) {
				randomColor = (byte) (Math.random() * 16);
			}

			colorSubstitutionMap.put(availableColor, randomColor);
		}

		this.availableColors = new ArrayList<>(colorSubstitutionMap.values());

		for(int i = 0; i < currentFloor.blueprint.length; i++) {
			byte data = currentFloor.blueprint[i];
			currentFloorBlueprint[i] = colorSubstitutionMap.get(data);
		}

	}

	public void paint(Location location, Byte color) {
		location.subtract(5, 0, 5);

		for(int x = 0; x < randomPaintPattern.length; x++) {
			for(int z = 0; z < randomPaintPattern[x].length; z++) {
				if(randomPaintPattern[x][z] == 0) continue;
				Location block = location.clone().add(x, 0, z);
				if(!isOnFloor(block)) continue;

				if(color != null) changeColor(block.getBlock(), color);
				else randomizeBlock(block.getBlock());
			}
		}
	}

	public Location getHitBlockCenter(Location location, Vector vector) {
		return new Location(location.getWorld(), location.getX() + vector.getX(), 1,
				location.getZ() + vector.getZ());
	}

	public Floor getFloor(String name) {
		for(Floor floor : floors) {
			if(floor.name.equalsIgnoreCase(name)) return floor;
		}
		return null;
	}
}
