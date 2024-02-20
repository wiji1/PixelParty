package dev.wiji.pixelparty;

import dev.wiji.pixelparty.commands.MenuCommand;
import dev.wiji.pixelparty.commands.TestCommand;
import dev.wiji.pixelparty.controllers.*;
import dev.wiji.pixelparty.enums.ServerType;
import dev.wiji.pixelparty.events.MessageEvent;
import dev.wiji.pixelparty.holograms.HologramManager;
import dev.wiji.pixelparty.inventory.GUIManager;
import dev.wiji.pixelparty.leaderboard.LeaderboardManager;
import dev.wiji.pixelparty.messaging.PluginMessage;
import dev.wiji.pixelparty.messaging.RedisManager;
import dev.wiji.pixelparty.objects.Floor;
import dev.wiji.pixelparty.objects.PowerUp;
import dev.wiji.pixelparty.powerups.*;
import dev.wiji.pixelparty.sql.TableManager;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class PixelParty extends JavaPlugin implements PixelPartyPlugin {
	public static PixelParty INSTANCE;
	public static ServerType serverType;

	public static BukkitAudiences adventure;

	public static GameManager gameManager;

	public static Player owner = null;

	public FileConfiguration config;

	@Override
	public void onEnable() {
		INSTANCE = this;
		serverType = ServerType.getServerType(getServerName());
		loadConfig();
		config = YamlConfiguration.loadConfiguration(new File(getDataFolder().getPath() + "/config.yml"));


		RedisManager.init(this);
		TableManager.registerTables(this);
		PacketInjector.initialise();

		PluginMessage response = new PluginMessage();
		response.writeString("SERVER START");
		response.send();

		getServer().getPluginManager().registerEvents(new GUIManager(), this);
		getServer().getPluginManager().registerEvents(new ChatManager(), this);
		getServer().getPluginManager().registerEvents(new PacketManager(), this);
		getServer().getPluginManager().registerEvents(new HologramManager(), this);
		getServer().getPluginManager().registerEvents(new PlayerDataManager(), this);
		getServer().getPluginManager().registerEvents(new LeaderboardManager(), this);

		getCommand("menu").setExecutor(new MenuCommand());

		if(serverType == ServerType.LOBBY) {
			getServer().getPluginManager().registerEvents(new LobbyManager(), this);
			return;
		}

		if(serverType == ServerType.PRACTICE) {
			getServer().getPluginManager().registerEvents(new PracticeManager(), this);;
		}

		AmbienceManager.init();
		adventure = BukkitAudiences.create(this);
		gameManager = new GameManager();

		getCommand("test").setExecutor(new TestCommand());
		getServer().getPluginManager().registerEvents(new ScoreboardHandler(), this);
		getServer().getPluginManager().registerEvents(new PowerUpManager(), this);
		getServer().getPluginManager().registerEvents(new SpectatorManager(), this);

		registerFloors();
		registerPowerUps();
	}

	@Override
	public void onDisable() {
		RedisManager.cleanUp();

		if(adventure != null) {
			adventure.close();
			adventure = null;
		}
	}

	@Override
	public String getServerName() {
		return getServer().getServerName();
	}

	@Override
	public void callMessageEvent(PluginMessage message, String channel) {
		MessageEvent event = new MessageEvent(message, channel);
		Bukkit.getPluginManager().callEvent(event);
	}

	@Override
	public String getConfigOption(String option) {
		return config.getString(option);
	}

	public void registerFloors() {
		FloorManager floorManager = gameManager.floorManager;

		floorManager.startFloor = new Floor("Hypixel", "start", Floor.FloorType.STATIC);
		floorManager.endFloor = new Floor("End", "end", Floor.FloorType.STATIC);

		floorManager.registerFloor(new Floor("Blue Heart", "d2", Floor.FloorType.STATIC));
		floorManager.registerFloor(new Floor("Bubbles", "d3", Floor.FloorType.DYNAMIC));
		floorManager.registerFloor(new Floor("Butterfly", "d4", Floor.FloorType.MIXED,
				DyeColor.BLACK, DyeColor.GRAY, DyeColor.WHITE, DyeColor.RED, DyeColor.ORANGE, DyeColor.YELLOW));
		floorManager.registerFloor(new Floor("Bricks", "b4", Floor.FloorType.STATIC));
		floorManager.registerFloor(new Floor("Chart", "a3", Floor.FloorType.STATIC));
		floorManager.registerFloor(new Floor("Circles", "a2", Floor.FloorType.DYNAMIC));
		floorManager.registerFloor(new Floor("Creeper", "b2", Floor.FloorType.DYNAMIC));
		floorManager.registerFloor(new Floor("Cross", "a7", Floor.FloorType.DYNAMIC));
		floorManager.registerFloor(new Floor("Diamond", "c4", Floor.FloorType.DYNAMIC));
		floorManager.registerFloor(new Floor("Diamonds", "b5", Floor.FloorType.DYNAMIC));
		floorManager.registerFloor(new Floor("Direction", "e4", Floor.FloorType.MIXED, DyeColor.BLACK));
		floorManager.registerFloor(new Floor("Factory", "d5", Floor.FloorType.MIXED, DyeColor.BLACK));
		floorManager.registerFloor(new Floor("Geometric", "a8", Floor.FloorType.DYNAMIC));
		floorManager.registerFloor(new Floor("Grid", "a1", Floor.FloorType.DYNAMIC));
		floorManager.registerFloor(new Floor("Intersect", "b8", Floor.FloorType.DYNAMIC));
		floorManager.registerFloor(new Floor("Knit", "c1", Floor.FloorType.DYNAMIC));
		floorManager.registerFloor(new Floor("Lines", "a5", Floor.FloorType.DYNAMIC));
		floorManager.registerFloor(new Floor("Monopoly", "c3", Floor.FloorType.DYNAMIC));
		floorManager.registerFloor(new Floor("Mosaic", "e3", Floor.FloorType.DYNAMIC));
		floorManager.registerFloor(new Floor("Love Birds", "e2", Floor.FloorType.STATIC));
		floorManager.registerFloor(new Floor("Pixels", "b6", Floor.FloorType.DYNAMIC));
		floorManager.registerFloor(new Floor("Platforms", "c7", Floor.FloorType.DYNAMIC));
		floorManager.registerFloor(new Floor("Pond", "a4", Floor.FloorType.STATIC));
		floorManager.registerFloor(new Floor("Ring", "b7", Floor.FloorType.DYNAMIC));
		floorManager.registerFloor(new Floor("Sky", "c5", Floor.FloorType.STATIC));
		floorManager.registerFloor(new Floor("Spikes", "d1", Floor.FloorType.STATIC));
		floorManager.registerFloor(new Floor("Squares", "c8", Floor.FloorType.DYNAMIC));
		floorManager.registerFloor(new Floor("Star", "a6", Floor.FloorType.DYNAMIC));
		floorManager.registerFloor(new Floor("Stripes", "b1", Floor.FloorType.DYNAMIC));
		floorManager.registerFloor(new Floor("Static", "e5", Floor.FloorType.DYNAMIC));
		floorManager.registerFloor(new Floor("Sun", "b3", Floor.FloorType.DYNAMIC));
		floorManager.registerFloor(new Floor("Vibrant", "c2", Floor.FloorType.DYNAMIC));
		floorManager.registerFloor(new Floor("ZigZag", "c6", Floor.FloorType.DYNAMIC));



	}

	public void registerPowerUps() {
		PowerUp.registerPowerUp(new AcidRain());
		PowerUp.registerPowerUp(new Barriers());
		PowerUp.registerPowerUp(new ColorCow());
		PowerUp.registerPowerUp(new EnderPearl());
		PowerUp.registerPowerUp(new LeapFeather());
		PowerUp.registerPowerUp(new Hunger());
		PowerUp.registerPowerUp(new MagicCarpet());
		PowerUp.registerPowerUp(new PaintEgg());
		PowerUp.registerPowerUp(new Pumpkin());
		PowerUp.registerPowerUp(new Teleport());
		PowerUp.registerPowerUp(new Villagers());
		PowerUp.registerPowerUp(new SpeedPotion());
		PowerUp.registerPowerUp(new JumpPotion());
		PowerUp.registerPowerUp(new ColorTrail());
	}

	private void loadConfig() {
		getConfig().options().copyDefaults(true);
		saveConfig();
	}
}
