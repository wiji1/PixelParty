package dev.wiji.pixelparty.events;

import dev.wiji.pixelparty.messaging.PluginMessage;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MessageEvent extends Event {

	private final PluginMessage message;
	private final String channel;

	private static final HandlerList HANDLERS = new HandlerList();

	public MessageEvent(PluginMessage message, String channel) {
		this.message = message;
		this.channel = channel;
	}

	public PluginMessage getMessage() {
		return message;
	}

	public String getChannel() {
		return channel;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}
}
