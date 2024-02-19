package dev.wiji.pixelparty.util;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import dev.wiji.pixelparty.enums.Skin;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SkinUtil {
	public static Map<UUID, MinecraftSkin> cachedSkins = new HashMap<>();

	public static void cacheSkin(Player player) {
		EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
		GameProfile profile = nmsPlayer.getProfile();
		Collection<Property> textures = profile.getProperties().get("textures");

		String texture = null;
		String signature = null;

		for(Property property : textures) {
			if(!property.getName().equals("textures")) continue;
			texture = property.getValue();
			signature = property.getSignature();
		}
		
		if(cachedSkins.containsKey(player.getUniqueId())) {
			MinecraftSkin skin = cachedSkins.get(player.getUniqueId());

			if(!texture.equals(skin.texture)) {
				skin.texture = texture;
				skin.signature = texture;
			}
		} else {
			cachedSkins.put(player.getUniqueId(), new MinecraftSkin(texture, signature));
		}
	}

	public static MinecraftSkin getCachedSkin(UUID uuid) {
		return cachedSkins.get(uuid);
	}
	
	
	public static class MinecraftSkin {
		public String texture;
		public String signature;
		
		public MinecraftSkin(String texture, String signature) {
			this.texture = texture;
			this.signature = signature;
		}
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
			Field profileField = meta.getClass().getDeclaredField("profile");
			profileField.setAccessible(true);
			profileField.set(meta, profile);

		} catch (IllegalArgumentException | NoSuchFieldException | SecurityException | IllegalAccessException error) {
			error.printStackTrace();
		}

		head.setItemMeta(meta);
		return head;
	}
}
