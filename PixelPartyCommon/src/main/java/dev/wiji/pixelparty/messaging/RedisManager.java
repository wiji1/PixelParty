package dev.wiji.pixelparty.messaging;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.Locale;

public class RedisManager {
	public static PixelPartyPlugin plugin;

	public static Jedis jedisSubscribe = new Jedis("redis://152.70.124.44:6379");
	public static Jedis jedisPublish = new Jedis("redis://152.70.124.44:6379");

	public static void init(PixelPartyPlugin instance) {
		plugin = instance;

		Thread thread = new Thread(() -> {
			try {
				JedisPubSub jedisPubSub = new JedisPubSub() {
					@Override
					public void onMessage(String channel, String message) {
						if(!channel.equals("PLUGIN")) return;

						PluginMessage pluginMessage = new PluginMessage(message);
						if(!pluginMessage.intendedServer.toUpperCase(Locale.ROOT).equals(plugin.getServerName())) return;
						plugin.callMessageEvent(pluginMessage, channel);
					}
				};

				jedisSubscribe.subscribe(jedisPubSub, "PLUGIN");

			} catch(Exception e) {
				e.printStackTrace();
			}
		});

		thread.start();
	}

	public static void sendMessage(String message) {
		jedisPublish.publish("PLUGIN", message);
	}

	public static void cleanUp() {
		jedisSubscribe.close();
		jedisPublish.close();
	}
}
