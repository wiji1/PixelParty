package dev.wiji.pixelparty.controllers;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import dev.wiji.pixelparty.playerdata.PixelPlayer;
import dev.wiji.pixelparty.sql.Constraint;
import dev.wiji.pixelparty.sql.SQLTable;
import dev.wiji.pixelparty.sql.TableManager;
import dev.wiji.pixelparty.sql.Value;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Collection;

public class PlayerDataManager implements Listener {

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerJoin(PlayerJoinEvent event) {

		cachePlayer(event.getPlayer());
		PixelPlayer.getPixelPlayer(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onLeave(PlayerQuitEvent event) {
		PixelPlayer pixelPlayer = PixelPlayer.getPixelPlayer(event.getPlayer());
		pixelPlayer.save();
		PixelPlayer.pixelPlayers.remove(pixelPlayer);
	}

	public void cachePlayer(Player player) {
		SQLTable table = TableManager.getTable("PlayerCache");
		if(table == null) throw new RuntimeException("SQL Table failed to register!");

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

		table.updateRow(new Constraint("uuid", player.getUniqueId().toString()),
				new Value("name", player.getName()),
				new Value("skin_texture", texture),
				new Value("skin_signature", signature)
		);
	}
}
