package dev.wiji.pixelparty.inspector.events;

import dev.wiji.pixelparty.messaging.PluginMessage;
import net.md_5.bungee.api.plugin.Event;

public class MessageEvent extends Event {

	private final PluginMessage message;
	private final String channel;

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

}
