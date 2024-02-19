package dev.wiji.pixelparty;

import dev.wiji.pixelparty.commands.LobbyCommand;
import dev.wiji.pixelparty.commands.PlayCommand;
import dev.wiji.pixelparty.controllers.*;

import dev.wiji.pixelparty.enums.ServerType;
import dev.wiji.pixelparty.inspector.IContainerInspector;
import dev.wiji.pixelparty.inspector.docker.DockerContainerInspector;
import dev.wiji.pixelparty.inspector.events.MessageEvent;
import dev.wiji.pixelparty.messaging.PluginMessage;
import dev.wiji.pixelparty.messaging.RedisManager;
import dev.wiji.pixelparty.sql.TableManager;
import dev.wiji.pixelparty.updater.ServerUpdater;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class BungeeMain extends Plugin implements PixelPartyPlugin {

	public static BungeeMain INSTANCE;
	public static LobbyManager lobbyManager;

	private Map<String, Configuration> configuration;

	@Override
	public void onEnable() {
		INSTANCE = this;
		getProxy().setReconnectHandler(new ReconnectionHandler());
		RedisManager.init(this);
		TableManager.registerTables(this);

		try {
			this.loadConfiguration();
		} catch (IOException e) {
			getLogger().warning("Not able to write Configuration File.");
			getLogger().warning("Check write Permissions to the plugin directory.");
			getLogger().warning("Stopped Plugin enabling (Plugin will not work!)");
			e.printStackTrace();
			return;
		}

		if (getConfiguration().get("server-updater").getBoolean("enabled")) {
			getLogger().info("[Server Updater] Enabled!");
			bootstrapServerUpdater(getConfiguration().get("server-updater"));
		}

		if (getConfiguration().get("container-inspector").getBoolean("enabled")) {
			getLogger().info("[Container Inspector] Enabled!");
			bootstrapContainerInspector(
					getConfiguration().get("container-inspector")
			);
		}

		lobbyManager = new LobbyManager();
		getProxy().getPluginManager().registerListener(this, lobbyManager);

		getProxy().getPluginManager().registerListener(this, new PracticeQueueManager());

		getProxy().getPluginManager().registerListener(this, new QueueManager(ServerType.NORMAL));

		getProxy().getPluginManager().registerCommand(this, new PlayCommand("play"));
		getProxy().getPluginManager().registerCommand(this, new LobbyCommand("lobby"));
		getProxy().getPluginManager().registerListener(this, new MessageListener());
		getProxy().getPluginManager().registerListener(this, new PlayerManager());
	}

	@Override
	public void onDisable() {
		DockerManager.shutdown = true;
		DockerManager.cleanUp();
		RedisManager.cleanUp();
	}

	@Override
	public String getServerName() {
		return "PROXY";
	}

	@Override
	public void callMessageEvent(PluginMessage message, String channel) {
		getProxy().getPluginManager().callEvent(new MessageEvent(message, channel));
	}

	@Override
	public String getConfigOption(String option) {
		return null;
	}

	private void bootstrapServerUpdater(Configuration configuration) {
		ServerUpdater serverUpdater = new ServerUpdater(configuration, getProxy(), getLogger());
		getProxy().getPluginManager().registerListener(this, serverUpdater);
	}

	private void bootstrapContainerInspector(Configuration configuration) {

		IContainerInspector containerInspector = new DockerContainerInspector(configuration, getProxy(), getLogger());

//		if (configuration.getString("backend").equals("docker")) {
//			containerInspector = new DockerContainerInspector(configuration, getProxy(), getLogger());
//		} else {
////			containerInspector = new KubernetesContainerInspector(configuration, getProxy(), getLogger());
//		}

		getProxy().getScheduler().runAsync(this, containerInspector::runContainerInspection);
		getProxy().getScheduler().runAsync(this, containerInspector::runContainerListener);
	}

	private void loadConfiguration() throws IOException {

		List<String> configNames = Arrays.asList(
				"container-inspector",
				"server-updater"
		);
		Map<String, Configuration> configuration = new HashMap<>(configNames.size());


		if (!getDataFolder().exists()) {
			if (!getDataFolder().mkdir()) {
				throw new IOException("Not able to generate Plugin Data Folder");
			}
		}


		for (String configName : configNames) {


			File file = new File(getDataFolder(), configName + ".yml");

			if (!file.exists()) {
				try (InputStream in = getResourceAsStream(configName + ".yml")) {
					Files.copy(in, file.toPath());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			configuration.put(configName, ConfigurationProvider.getProvider(YamlConfiguration.class)
					.load(new File(getDataFolder(), configName + ".yml")
					));
		}

		this.configuration = configuration;
	}

	public Map<String, Configuration> getConfiguration() {
		return configuration;
	}
}
