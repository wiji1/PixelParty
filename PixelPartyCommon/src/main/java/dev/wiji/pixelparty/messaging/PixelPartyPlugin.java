package dev.wiji.pixelparty.messaging;

public interface PixelPartyPlugin {

	String getServerName();

	void callMessageEvent(PluginMessage message, String channel);

}
