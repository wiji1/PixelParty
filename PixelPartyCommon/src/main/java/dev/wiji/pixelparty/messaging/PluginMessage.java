package dev.wiji.pixelparty.messaging;


import java.util.*;
import java.util.function.Consumer;

public class PluginMessage {

	public static Map<UUID, Consumer<PluginMessage>> callbacks = new HashMap<>();

	private final List<String> strings = new ArrayList<>();
	private final List<Integer> integers = new ArrayList<>();
	private final List<Long> longs = new ArrayList<>();
	private final List<Boolean> booleans = new ArrayList<>();

	public String originServer;
	public String intendedServer;
	public UUID messageID;

	public PluginMessage() {
		this.originServer = RedisManager.plugin.getServerName();
	}

	public PluginMessage(String data) {
		String[] categories = data.split(":", -1);
		originServer = categories[0];
		intendedServer = categories[1];
		messageID = UUID.fromString(categories[2]);

		if(!intendedServer.equals(RedisManager.plugin.getServerName())) return;

		String[] strings = categories[3].split("\\|");
		for(String string : strings) {
			if(string.isEmpty()) continue;
			this.strings.add(string);
		}

		String[] integers = categories[4].split("\\|");
		for(String integer : integers) {
			if(integer.isEmpty()) continue;
			this.integers.add(Integer.parseInt(integer));
		}

		String[] longs = categories[5].split("\\|");
		for(String longValue : longs) {
			if(longValue.isEmpty()) continue;
			this.longs.add(Long.parseLong(longValue));
		}

		String[] booleans = categories[6].split("\\|");
		for(String booleanValue : booleans) {
			if(booleanValue.isEmpty()) continue;
			this.booleans.add(Boolean.parseBoolean(booleanValue));
		}

		new HashMap<>(callbacks).forEach((uuid, consumer) -> {
			if(uuid.equals(messageID)) {
				consumer.accept(this);
				callbacks.remove(uuid);
			}
		});
	}

	public PluginMessage writeString(String string) {
		strings.add(string == null ? "" : string);
		return this;
	}

	public PluginMessage writeInt(int integer) {
		integers.add(integer);
		return this;
	}

	public PluginMessage writeLong(long longValue) {
		longs.add(longValue);
		return this;
	}

	public PluginMessage writeBoolean(boolean bool) {
		booleans.add(bool);
		return this;
	}

	public void request(Consumer<PluginMessage> callback) {
		this.messageID = UUID.randomUUID();
		callbacks.put(messageID, callback);

		send();
	}

	public void replyTo(PluginMessage message) {
		this.intendedServer = message.originServer;
		this.messageID = message.messageID;

		send();
	}

	public void send() {
		if(intendedServer == null) intendedServer = "PROXY";
		if(messageID == null) messageID = UUID.randomUUID();

		StringBuilder builder = new StringBuilder();
		builder.append(originServer).append(":").append(intendedServer).append(":").append(messageID);

		builder.append(":");
		for(String string : strings) builder.append(string).append("|");

		builder.append(":");
		for(Integer integer : integers) builder.append(integer).append("|");

		builder.append(":");
		for(Long longValue : longs) builder.append(longValue).append("|");

		builder.append(":");
		for(Boolean booleanValue : booleans) builder.append(booleanValue).append("|");

		RedisManager.sendMessage(builder.toString());
	}

	public PluginMessage setIntendedServer(String server) {
		intendedServer = server;
		return this;
	}

	public List<String> getStrings() {
		return strings;
	}

	public List<Integer> getIntegers() {
		return integers;
	}

	public List<Long> getLongs() {
		return longs;
	}

	public List<Boolean> getBooleans() {
		return booleans;
	}
}
