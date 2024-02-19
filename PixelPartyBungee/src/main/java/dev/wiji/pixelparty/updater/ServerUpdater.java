package dev.wiji.pixelparty.updater;

import dev.wiji.pixelparty.controllers.DockerManager;
import dev.wiji.pixelparty.controllers.QueueManager;
import dev.wiji.pixelparty.inspector.events.ContainerEvent;
import dev.wiji.pixelparty.objects.GameServer;
import dev.wiji.pixelparty.updater.events.PostAddServerEvent;
import dev.wiji.pixelparty.updater.events.PostRemoveServerEvent;
import dev.wiji.pixelparty.updater.events.PreAddServerEvent;
import dev.wiji.pixelparty.updater.events.PreRemoveServerEvent;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.event.EventHandler;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ServerUpdater implements Listener {

    private String identifierKey;

    private String nameKey;

    private String portKey;

    private String motdKey;

    private String restrictedKey;

    private List<String> addActionList;

    private List<String> removeActionList;

    private Boolean debug;

    private ProxyServer proxyServer;

    private Logger logger;

    public ServerUpdater(Configuration configuration, ProxyServer proxyServer, Logger logger) {
        this.identifierKey = configuration.getString("environment-variables.identifier");
        this.nameKey = configuration.getString("environment-variables.name");
        this.portKey = configuration.getString("environment-variables.port");
        this.motdKey = configuration.getString("environment-variables.motd");
        this.restrictedKey = configuration.getString("environment-variables.restricted");
        this.addActionList = configuration.getStringList("add-actions");
        this.removeActionList = configuration.getStringList("remove-actions");
        this.debug = configuration.getBoolean("debug");
        this.proxyServer = proxyServer;
        this.logger = logger;
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onDockerEvent(ContainerEvent event) {
        if (!event.getEnvironmentVariables().containsKey(this.identifierKey)) {
            logger.info("missing identifier" + this.identifierKey);
            return;
        }

        if (this.addActionList.contains(event.getAction())) {
            this.addServer(event);
        }

        else if(this.removeActionList.contains(event.getAction())) {
            this.removeServer(event);
        }
        else
        logger.info("unknown action on event: "+event.getAction());
    }


    private void addServer(ContainerEvent eventData) {

        ServerInfo serverInfo = this.getServerInfoForEvent(eventData);

        if (serverInfo.getAddress().getHostName() == null) {
            this.logger.warning("[Server Updater] Could not add server:" + serverInfo.getName());
            this.logger.warning("[Server Updater]  > Reason: No IP, is you network fine?");
            this.logger.warning("[Server Updater]  > Trigger-Event-Action: " + eventData.getAction());

            return;
        }

        if (this.proxyServer.getServers().containsKey(serverInfo.getName())) {
            if (this.debug) {
                this.logger.warning("[Server Updater] Server with id " + serverInfo.getName() + " already exists in Bungeecord Proxy.");
            }

            InetSocketAddress currentAddress = this.proxyServer.getServers().get(serverInfo.getName()).getAddress();

            if (!currentAddress.equals(serverInfo.getAddress())) {
                if (this.debug) {
                    this.logger.warning("[Server Updater]  > Server address of " + serverInfo.getName() + "changed!");
                    this.logger.warning("[Server Updater]  >> Current: " + currentAddress.toString());
                    this.logger.warning("[Server Updater]  >> New: " + serverInfo.getAddress().toString());
                    this.logger.warning("[Server Updater]  >> Server removed from proxy to re-add it");
                }
                this.proxyServer.getServers().remove(serverInfo.getName());
            } else {
                if (this.debug) {
                    this.logger.warning("[Server Updater]  > Skipped!");
                    this.logger.warning("[Server Updater]  > Trigger-Event-Action: " + eventData.getAction());
                }
                return;
            }
        }


        this.proxyServer.getPluginManager().callEvent(new PreAddServerEvent(
                serverInfo,
                eventData.getEnvironmentVariables()
        ));

        this.proxyServer.getServers().put(serverInfo.getName(), serverInfo);
        this.logger.info("[Server Updater] Added server: " + serverInfo.getName());
        this.logger.info("[Server Updater]  > Address: " + serverInfo.getAddress().toString());
        this.logger.info("[Server Updater]  > MOTD: " + serverInfo.getMotd());
        this.logger.info("[Server Updater]  > Trigger-Event-Action: " + eventData.getAction());

        this.proxyServer.getPluginManager().callEvent(new PostAddServerEvent(
                serverInfo,
                eventData.getEnvironmentVariables()
        ));
    }

    private void removeServer(ContainerEvent eventData) {
        // server id
        String id = this.getServerId(eventData);

        if(!this.proxyServer.getServers().containsKey(id)) {
            if(this.debug) {
                this.logger.warning("[Server Updater] Could not remove server: " + id);
                this.logger.warning("[Server Updater]  > Reason: Not exists");
                this.logger.warning("[Server Updater]  > Trigger-Event-Action: " + eventData.getAction());
            }

            return;
        }

        this.proxyServer.getPluginManager().callEvent(new PreRemoveServerEvent(
                this.getServerInfoForEvent(eventData),
                eventData.getEnvironmentVariables()
        ));

        this.proxyServer.getServers().remove(id);
        this.logger.info("[Server Updater] Removing Server: " + id);
        this.logger.info("[Server Updater]  > Trigger-Event-Action: " + eventData.getAction());

        this.proxyServer.getPluginManager().callEvent(new PostRemoveServerEvent(id));

        if(DockerManager.shutdown) return;

        for(QueueManager queueManager : QueueManager.queueManagers) {
            for(GameServer waitingServer : new ArrayList<>(queueManager.waitingServers)) {
                if(waitingServer.getName().equals(id)) {
                    queueManager.waitingServers.remove(waitingServer);
                    queueManager.callForServer();

                    return;
                }
            }

            for(GameServer queueServer : new ArrayList<>(queueManager.queueServers)) {
                if(queueServer.getName().equals(id)) {
                    queueManager.queueServers.remove(queueServer);
                    queueManager.callForServer();

                    return;
                }
            }

            for(GameServer activeServer : new ArrayList<>(queueManager.activeServers)) {
                if(activeServer.getName().equals(id)) {
                    queueManager.activeServers.remove(activeServer);
                    return;
                }
            }
        }
    }

    private ServerInfo getServerInfoForEvent(ContainerEvent eventData) {
        // server id
        String id = this.getServerId(eventData);

        // Getting the address to create
        int port = eventData.getEnvironmentVariables().get(this.portKey) != null
                ? Integer.parseInt(eventData.getEnvironmentVariables().get(this.portKey))
                : (eventData.getPort() != null ? eventData.getPort() : 25565);

        InetSocketAddress inetSocketAddress = new InetSocketAddress(eventData.getIp(), port);


        // Getting the motd
        String motd = eventData.getEnvironmentVariables().get(this.motdKey) != null
                ? eventData.getEnvironmentVariables().get(this.motdKey)
                : "A Minecraft Server Instance";

        // Getting restricted bool
        boolean restricted =
                eventData.getEnvironmentVariables().get(this.restrictedKey) != null &&
                        eventData.getEnvironmentVariables().get(this.restrictedKey).equals("restricted");

        return ProxyServer.getInstance().constructServerInfo(
                id,
                inetSocketAddress,
                motd,
                restricted
        );
    }

    private String getServerId(ContainerEvent eventData) {
        if (eventData.getEnvironmentVariables().get(this.nameKey) != null) {
            return eventData.getEnvironmentVariables().get(this.nameKey);
        }

        if (eventData.getName() != null) {
            return eventData.getName().replace("/", "");
        }

        return eventData.getId();
    }

}
