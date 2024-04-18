package dev.wiji.pixelparty.controllers;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DockerClientBuilder;
import dev.wiji.pixelparty.BungeeMain;
import dev.wiji.pixelparty.objects.GameServer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DockerManager {

//	static {
//		Timer timer = new Timer();
//		timer.schedule(new TimerTask() {
//			public void run() {
//				checkForDeadServers();
//			}
//		}, 0, 5 * 1000);
//	}

	public static final DockerClient dockerClient = DockerClientBuilder.getInstance().build();
	public static boolean shutdown = false;

	public static void terminateContainer(String containerID) {

		try {
			dockerClient.killContainerCmd(containerID).exec();

			System.out.println("Killed container " + containerID);
		} catch(Exception e) {
			System.out.println("Failed to kill container " + containerID);
		}

		try {
			dockerClient.removeContainerCmd(containerID).exec();
			System.out.println("Removed container " + containerID);

			dockerClient.pruneCmd(PruneType.VOLUMES).exec();
			System.out.println("Pruned Volumes");
		} catch(Exception e) {
			System.out.println("Failed to remove container " + containerID);
		}
	}

	public static String createContainer(GameServer gameServer) {

		while(!ServerManager.checkCodeAvailability(gameServer.code)) gameServer.code = generateRandomCode();

		String hostDirectory = BungeeMain.INSTANCE.getConfiguration()
				.getString("volumes-path") + gameServer.serverType.name().toLowerCase();

		String globalDirectory = BungeeMain.INSTANCE.getConfiguration()
				.getString("volumes-path") + "global";

		Bind hostBind = new Bind(hostDirectory, new Volume("/config"));
		Bind globalBind = new Bind(globalDirectory, new Volume("/plugins"));

		ExposedPort tcp = ExposedPort.tcp(gameServer.port);
		Ports portBindings = new Ports();
		portBindings.bind(tcp, Ports.Binding.bindPort(gameServer.port));


		HostConfig hostConfig = HostConfig.newHostConfig()
				.withPortBindings(portBindings)
				.withBinds(globalBind, hostBind)
				.withAutoRemove(true)
				.withNetworkMode("host");

		CreateContainerCmd createContainerCmd = dockerClient
				.createContainerCmd("itzg/minecraft-server:java8-multiarch")
				.withEnv("EULA=TRUE", "ONLINE_MODE=FALSE", "MEMORY=1G", "TYPE=PAPER", "VERSION=1.8.8", "PAPERBUILD=445",
						"SERVER_PORT=" + gameServer.port, "COPY_CONFIG_SRC=/config", "COPY_CONFIG_DEST=/data",
						"COPY_PLUGINS_SRC=/plugins", "COPY_PLUGINS_DEST=/data", "LEVEL=" + gameServer.serverType.worldName,
						"SERVER_NAME=" + gameServer.getName())
				.withName(gameServer.getName())
				.withExposedPorts(tcp)
				.withHostConfig(hostConfig)
				.withAttachStdout(true)
				.withAttachStderr(true);

		CreateContainerResponse containerResponse = createContainerCmd.exec();
		String containerId = containerResponse.getId();
		dockerClient.startContainerCmd(containerId).exec();

		return containerId;
	}

	public static void cleanUp() {
		System.out.println("Cleaning up Docker containers...");
		for(GameServer gameServer : new ArrayList<>(ServerManager.gameServers)) gameServer.terminate();

		dockerClient.pruneCmd(PruneType.VOLUMES).exec();
		try { dockerClient.close(); } catch(IOException e) { throw new RuntimeException(e); }
	}

	private static final String LETTERS = "ABCDEF";
	private static final String DIGITS = "0123456789";
	private static final int CODE_LENGTH = 3;

	private static String generateRandomCode() {
		StringBuilder code = new StringBuilder();
		Random random = new Random();

		for (int i = 0; i < CODE_LENGTH; i++) {
			boolean useLetters = random.nextBoolean();
			String source = useLetters ? LETTERS : DIGITS;

			int randomIndex = random.nextInt(source.length());
			char randomChar = source.charAt(randomIndex);
			code.append(randomChar);
		}

		return code.toString();
	}

//	public static void checkForDeadServers() {
//		for(ServerType type : ServerType.values()) {
//			QueueManager manager = QueueManager.getQueue(type);
//			if(manager == null) continue;
//
//			//TODO: Implement this for lobby servers, Try to change lobby servers into the previous QueueManager object
//
//			for(GameServer waitingServer : manager.waitingServers) {
//				Container container = getContainer(waitingServer.containerID);
//				if(container == null) continue;
//
//				String state = container.getState();
//
//				if(state.equals("exited") || state.equals("dead") || state.equals("paused")) {
//					terminateContainer(waitingServer.containerID);
//					manager.waitingServers.remove(waitingServer);
//					manager.callForServer();
//
//					System.out.println("[PixelPartyBungee] Server " + waitingServer.getName() + " died, replacing it.");
//				}
//			}
//		}
//	}

	public static Container getContainer(String containerID) {
		List<Container> containers = dockerClient.listContainersCmd().exec();
		for(Container container : containers) {
			if(container.getId().equals(containerID)) return container;
		}
		return null;
	}

}
