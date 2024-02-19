package dev.wiji.pixelparty.controllers;

import com.comphenix.protocol.PacketType;
import dev.wiji.pixelparty.events.PacketReceiveEvent;
import dev.wiji.pixelparty.events.PacketSendEvent;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PacketHandler extends ChannelDuplexHandler {
	private final Player player;

	public PacketHandler(final Player player) {
		this.player = player;
	}

	@Override
	public void channelRead(final ChannelHandlerContext c, final Object packet) throws Exception {
		final PacketType packetType = PacketType.fromClass(packet.getClass());

		PacketReceiveEvent event = new PacketReceiveEvent(this.player.getPlayer(), packet, packetType);
		Bukkit.getPluginManager().callEvent(event);

		if(!event.isCancelled()) super.channelRead(c, packet);
	}

	@Override
	public void write(final ChannelHandlerContext ctx, final Object packet, final ChannelPromise promise) throws Exception {
		final PacketType packetType = PacketType.fromClass(packet.getClass());

		PacketSendEvent event = new PacketSendEvent(this.player, packet, packetType);
		Bukkit.getPluginManager().callEvent(event);

		if(!event.isCancelled()) super.write(ctx, packet, promise);
	}
}
