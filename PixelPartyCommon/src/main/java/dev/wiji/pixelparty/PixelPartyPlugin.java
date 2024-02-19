package dev.wiji.pixelparty;

import dev.wiji.pixelparty.messaging.PluginMessage;

public interface PixelPartyPlugin {



	String getServerName();

	void callMessageEvent(PluginMessage message, String channel);

	String getConfigOption(String option);

}
