package trollnetwork.karma177;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;

import java.io.File;

import org.checkerframework.checker.units.qual.s;
import org.slf4j.Logger;

@Plugin(
        id = "trollcommands",
        name = "TrollCommands",
        version = "1.0-SNAPSHOT",
        description = "A Velocity plugin",
        authors = {"Karma177"}
)
public class TrollCommands {

    private final ProxyServer server;
    private final Logger logger;
    
    

    @Inject
    public TrollCommands(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        this.logger.info("TrollCommands is starting!");
        this.onEnable();
    }

    @Subscribe
    public void onProxyShutdown(ProxyInitializeEvent event) {
        this.logger.info("TrollCommands is stopping!");
        this.onDisable();
    }

    private void onEnable() {
        this.logger.info("Loading configuration...");
        // load logic
    }

    private void onDisable() {
        // unload logic
        this.logger.info("Stopped!");
    }
}