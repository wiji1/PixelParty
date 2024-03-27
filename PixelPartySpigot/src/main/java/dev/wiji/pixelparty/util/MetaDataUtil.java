package dev.wiji.pixelparty.util;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import dev.wiji.pixelparty.enums.Group;
import dev.wiji.pixelparty.enums.Skin;
import dev.wiji.pixelparty.playerdata.PixelPlayer;
import dev.wiji.pixelparty.sql.Constraint;
import dev.wiji.pixelparty.sql.Field;
import dev.wiji.pixelparty.sql.SQLTable;
import dev.wiji.pixelparty.sql.TableManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MetaDataUtil {

	public static Map<UUID, MetaData> metaData = new HashMap<>();

	public static String getDisplayName(Player player) {
		return getDisplayName(player.getUniqueId());
	}

	public static String getDisplayName(UUID uuid) {

		Group group = Group.DEFAULT;
		String name = "NULL";

		if(metaData.containsKey(uuid)) {
			MetaData data = metaData.get(uuid);

			group = data.getGroup();
			name = ChatColor.stripColor(data.getName());
		} else {
			ResultSet rs = getCachedPlayerData(uuid);

			try {
				if(rs.next()) {
					name = rs.getString("name");
					group = Group.fromString(rs.getString("user_group"));
				}

				rs.close();
			} catch(Exception e) { e.printStackTrace(); }
		}

		metaData.put(uuid, new MetaData(name, group, getCachedSkin(uuid)));

		return ChatColor.translateAlternateColorCodes('&',
				group.getChatColor() + name);
	}

	public static String getNameAndRank(Player player) {
		return getNameAndRank(player.getUniqueId());
	}

	public static String getNameAndRank(UUID uuid) {
		String name = getDisplayName(uuid);

		Group group;

		if(metaData.containsKey(uuid)) {
			group = metaData.get(uuid).getGroup();
		} else {
			group = Group.getGroup(uuid);
		}

		String groupPrefix = group.getChatColor().toString();
		String rankName = group == Group.DEFAULT ? "" : "[" + group.getDisplayName() + "] " ;

		metaData.put(uuid, new MetaData(name, group, getCachedSkin(uuid)));

		return ChatColor.translateAlternateColorCodes('&',
				groupPrefix + rankName + name);
	}

	public static ResultSet getCachedPlayerData(UUID uuid) {
		SQLTable table = TableManager.getTable("PlayerCache");
		if(table == null) throw new RuntimeException("SQL Table failed to register!");

		return table.selectRow(new Constraint("uuid", uuid.toString()));

	}

	public static MinecraftSkin getCachedSkin(UUID uuid) {
		if(metaData.containsKey(uuid)) return metaData.get(uuid).getSkin();

		SQLTable table = TableManager.getTable("PlayerCache");
		if(table == null) throw new RuntimeException("SQL Table failed to register!");

		ResultSet rs = table.selectRow(new Constraint("uuid", uuid.toString()),
				new Field("skin_texture"),
				new Field("skin_signature")
		);

		try {
			if(rs.next()) {
				return new MinecraftSkin(rs.getString("skin_texture"), rs.getString("skin_signature"));
			}

			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}


	public static void skinProfile(GameProfile profile, String texture, String signature) {
		profile.getProperties().removeAll("textures");
		profile.getProperties().put("textures", new Property("textures", texture, signature));
	}

	public static ItemStack getPlayerSkull(MinecraftSkin skin) {
		return getPlayerSkull(skin.texture, skin.signature);
	}

	public static ItemStack getPlayerSkull(Skin skin) {
		return getPlayerSkull(skin.getTexture(), skin.getSignature());
	}

	public static ItemStack getPlayerSkull(String texture, String signature) {
		org.bukkit.inventory.ItemStack head = new org.bukkit.inventory.ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
		SkullMeta meta = (SkullMeta) head.getItemMeta();

		GameProfile profile = new GameProfile(UUID.randomUUID(), null);

		profile.getProperties().removeAll("textures");
		profile.getProperties().put("textures", new Property("textures", texture));

		try {
			java.lang.reflect.Field profileField = meta.getClass().getDeclaredField("profile");
			profileField.setAccessible(true);
			profileField.set(meta, profile);

		} catch (IllegalArgumentException | NoSuchFieldException | SecurityException | IllegalAccessException error) {
			error.printStackTrace();
		}

		head.setItemMeta(meta);
		return head;
	}


	public static class MinecraftSkin {
		public String texture;
		public String signature;

		public MinecraftSkin(String texture, String signature) {
			this.texture = texture;
			this.signature = signature;
		}
	}

	public static class MetaData {
		private final String name;
		private final Group group;
		private final MinecraftSkin skin;

		public MetaData(String name, Group group, MinecraftSkin skin) {
			this.name = name;
			this.group = group;
			this.skin = skin;
		}

		public String getName() {
			return name;
		}

		public Group getGroup() {
			return group;
		}

		public MinecraftSkin getSkin() {
			return skin;
		}
	}

}
