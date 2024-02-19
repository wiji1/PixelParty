package dev.wiji.pixelparty.inspector.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Event;
import com.github.dockerjava.api.model.EventType;
import com.github.dockerjava.core.command.EventsResultCallback;
import dev.wiji.pixelparty.inspector.IContainerInspector;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.config.Configuration;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class DockerContainerInspector implements IContainerInspector {
    private DockerClient dockerClient;

    private ProxyServer proxyServer;

    private String network;

    private Logger logger;

    public DockerContainerInspector(Configuration configuration, ProxyServer proxyServer, Logger logger) {
        this.proxyServer = proxyServer;
        this.network = configuration.getString("docker.network");
        this.logger = logger;
        this.dockerClient = DockerClientFactory.getByConfiguration(configuration);
    }

    public void runContainerInspection() {
        this.logger.info("[Docker Container Inspector] Running initial inspection.");

        EventsResultCallback callback = this.getEventResultCallback();
        List<Container> containers = this.dockerClient.listContainersCmd().exec();

        // Trigger fake Event to use same Result Callback
        for (Container container: containers) {
            Event event = new Event("start", container.getId(), container.getImage(), System.currentTimeMillis())
                    .withAction("bootstrap")
                    .withType(EventType.forValue("container"));

            callback.onNext(event);
        }
    }

    public void runContainerListener() {
        this.logger.info("[Docker Container Inspector] Running listener.");
        try {
            this.dockerClient.eventsCmd().exec(this.getEventResultCallback()).awaitCompletion().close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private EventsResultCallback getEventResultCallback() {
        return new ResultCallback(
                this.dockerClient,
                this.proxyServer,
                this.network
        );
    }
}