package dev.wiji.pixelparty.controllers;

import io.netty.channel.Channel;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.NetworkManager;
import net.minecraft.server.v1_8_R3.PlayerConnection;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;

public class PacketInjector {
	private static Field channel;
	private static Field networkManager;
	private static Field playerConnection;

	public static void addPlayer(final Player player) {
		try {
			EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
			final Channel channel = PacketInjector.getChannel(PacketInjector.getNetworkManager(nmsPlayer));
			if (channel.pipeline().get("HACPacket") == null) {
				final PacketHandler packetHandler = new PacketHandler(player);
				channel.pipeline().addBefore("packet_handler", "HACPacket", packetHandler);
			}
		} catch (final Throwable t) {
			t.printStackTrace();
		}
	}

	private static Channel getChannel(final Object networkManager) {
		Channel channel = null;
		try {
			channel = (Channel) PacketInjector.channel.get(networkManager);
		} catch (final Exception e) {
			e.printStackTrace();
		}

		return channel;
	}

	private static Object getNetworkManager(final Object entityPlayer) {
		Object networkManager = null;
		try {
			networkManager = PacketInjector.networkManager.get(PacketInjector.playerConnection.get(entityPlayer));
		} catch (final Exception e) {
			e.printStackTrace();
		}

		return networkManager;
	}

	public static void initialise() {
		try {
			PacketInjector.playerConnection = EntityPlayer.class.getField("playerConnection");
			PacketInjector.networkManager = PlayerConnection.class.getField("networkManager");
			PacketInjector.channel = NetworkManager.class.getField("channel");

			PacketInjector.refreshSessions();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public static void refreshSessions() {
		for(final Player player : Bukkit.getOnlinePlayers()) {
			PacketInjector.removePlayer(player);
			PacketInjector.addPlayer(player);
		}
	}

	public static void removePlayer(final Player player) {
		try {
			EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
			final Channel channel = PacketInjector.getChannel(PacketInjector.getNetworkManager(nmsPlayer));
			if (channel.pipeline().get("HACPacket") != null) {
				channel.pipeline().remove("HACPacket");
			}
		} catch (final Throwable t) {
			t.printStackTrace();
		}
	}
}
