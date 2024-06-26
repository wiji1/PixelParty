package dev.wiji.pixelparty.events;

import com.comphenix.protocol.PacketType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PacketSendEvent extends Event implements Cancellable {
	private static final HandlerList HANDLERS = new HandlerList();

	private final Player player;
	private Object packet;
	private final PacketType packetType;

	private boolean cancelled = false;

	public PacketSendEvent(Player player, Object packet, PacketType packetType) {
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

	public void setPacket(Object packet) {
		this.packet = packet;
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
