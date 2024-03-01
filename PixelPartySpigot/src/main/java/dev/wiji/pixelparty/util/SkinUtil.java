package dev.wiji.pixelparty.util;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import dev.wiji.pixelparty.enums.Skin;
import dev.wiji.pixelparty.sql.Constraint;
import dev.wiji.pixelparty.sql.SQLTable;
import dev.wiji.pixelparty.sql.TableManager;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.UUID;

public class SkinUtil {
//	public static Map<UUID, MinecraftSkin> cachedSkins = new HashMap<>();

//	public static void cacheSkin(Player player) {
//		EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
//		GameProfile profile = nmsPlayer.getProfile();
//		Collection<Property> textures = profile.getProperties().get("textures");
//
//		String texture = null;
//		String signature = null;
//
//		for(Property property : textures) {
//			if(!property.getName().equals("textures")) continue;
//			texture = property.getValue();
//			signature = property.getSignature();
//		}
//
//		if(cachedSkins.containsKey(player.getUniqueId())) {
//			MinecraftSkin skin = cachedSkins.get(player.getUniqueId());
//
//			if(!texture.equals(skin.texture)) {
//				skin.texture = texture;
//				skin.signature = texture;
//			}
//		} else {
//			cachedSkins.put(player.getUniqueId(), new MinecraftSkin(texture, signature));
//		}
//	}

}
