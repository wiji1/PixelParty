package dev.wiji.pixelparty.util;

import dev.wiji.pixelparty.enums.Group;
import dev.wiji.pixelparty.playerdata.PixelPlayer;
import dev.wiji.pixelparty.sql.Constraint;
import dev.wiji.pixelparty.sql.SQLTable;
import dev.wiji.pixelparty.sql.TableManager;
import net.minecraft.server.v1_8_R3.EntityInsentient;
import net.minecraft.server.v1_8_R3.EntityTypes;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.*;

public class Misc {
	private final static TreeMap<Integer, String> map = new TreeMap<>();

	static {
		map.put(1000, "M");
		map.put(900, "CM");
		map.put(500, "D");
		map.put(400, "CD");
		map.put(100, "C");
		map.put(90, "XC");
		map.put(50, "L");
		map.put(40, "XL");
		map.put(10, "X");
		map.put(9, "IX");
		map.put(5, "V");
		map.put(4, "IV");
		map.put(1, "I");
	}

	public static String toRoman(int number) {
		int l = map.floorKey(number);
		if(number == l) return map.get(number);
		return map.get(l) + toRoman(number - l);
	}

	public static void broadcast(String message) {
		for(Player player : Bukkit.getOnlinePlayers()) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
		}
	}

	public static Player getPlayer(UUID uuid){
		for(Player player : Bukkit.getOnlinePlayers()){
			if(player.getUniqueId().equals(uuid)){
				return player;
			}
		}
		return null;
	}

	public static boolean isAirOrNull(ItemStack itemStack) {
		return itemStack == null ||itemStack.getType() == Material.AIR;
	}

	public static String color(String message) {
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	@SuppressWarnings("unchecked")
	public static void registerEntity(String name, int id, Class<? extends EntityInsentient> customClass) {
		try {

			List<Map<?, ?>> dataMaps = new ArrayList<Map<?, ?>>();
			for (Field f : EntityTypes.class.getDeclaredFields()) {
				if (f.getType().getSimpleName().equals(Map.class.getSimpleName())) {
					f.setAccessible(true);
					dataMaps.add((Map<?, ?>) f.get(null));
				}
			}

			((Map<Class<? extends EntityInsentient>, String>) dataMaps.get(1)).put(customClass, name);
			((Map<Class<? extends EntityInsentient>, Integer>) dataMaps.get(3)).put(customClass, id);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static int fromFixedPoint(double fixedPoint) {
		return (int) (fixedPoint * 32.0);
	}
}
