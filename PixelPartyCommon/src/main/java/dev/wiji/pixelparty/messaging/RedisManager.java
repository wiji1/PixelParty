package dev.wiji.pixelparty.messaging;

import dev.wiji.pixelparty.PixelPartyPlugin;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.Locale;

public class RedisManager {
	public static PixelPartyPlugin plugin;

	public static Jedis jedisSubscribe;
	public static Jedis jedisPublish;

	public static void init(PixelPartyPlugin instance) {
		plugin = instance;

		jedisSubscribe = new Jedis(plugin.getConfigOption("redis-url"));
		jedisPublish = new Jedis(plugin.getConfigOption("redis-url"));

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
