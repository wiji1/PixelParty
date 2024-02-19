package dev.wiji.pixelparty.events;

import com.comphenix.protocol.PacketType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PacketReceiveEvent extends Event implements Cancellable {
	private static final HandlerList HANDLERS = new HandlerList();

	private final Player player;
	private final Object packet;
	private final PacketType packetType;

	private boolean cancelled = false;

	public PacketReceiveEvent(Player player, Object packet, PacketType packetType) {
		this.player = player;
		this.packet = packet;
		this.packetType = packetType;
	}

	public Player getPlayer() {
		return player;
	}

	public Object getPacket() {
		return packet;
	}

	public PacketType getPacketType() {
		return packetType;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
}
