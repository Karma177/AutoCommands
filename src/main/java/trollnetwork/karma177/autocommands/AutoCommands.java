package trollnetwork.karma177.autocommands;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyReloadEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;

import trollnetwork.karma177.autocommands.Exceptions.EmptyCommandException;
import trollnetwork.karma177.autocommands.Exceptions.InvalidCommandMethodException;
import trollnetwork.karma177.autocommands.Exceptions.MissingPluginConfigException;
import trollnetwork.karma177.autocommands.Exceptions.MissingUserConfigException;

import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import trollnetwork.karma177.autocommands.utils.Messages;

@Plugin(
        id = "autocommands",
        name = "AutoCommands",
        version = "1.1-STABLE",
        description = "Autoesecuzione di comandi predeterminati.",
        authors = {"Karma177"}
)
public class AutoCommands {

    private final ProxyServer proxy;
    private final Logger logger;
    private final GestoreComandi gestoreComandi;
    private final Path dataDirectory;
    private static final String CONFIG_FILE_NAME = "commands.json";

    /**
     * AutoCommands
     * Costruttore della classe principale del plugin, in cui vengono iniettate le dipendenze necessarie
     * @param proxy
     * @param logger
     * @param dataDirectory
     */
    @Inject
    public AutoCommands(ProxyServer proxy, Logger logger, @DataDirectory Path dataDirectory) {
        this.proxy = proxy;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        this.gestoreComandi = new GestoreComandi(this.dataDirectory.toString(), CONFIG_FILE_NAME);
    }

    /**
     * onProxyInitialization
     * Metodo chiamato all'avvio del proxy
     * @param event
     */
    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        this.logger.info("AutoCommands in avvio...");
        this.onEnable();
    }

    /**
     * onProxyShutdown
     * Metodo chiamato alla chiusura del proxy
     * @param event
     */
    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        this.logger.info("AutoCommands in arresto...");
        this.logger.info("AutoCommands... Comandi automatici, o comandi su ruote?");
        this.onDisable();
    }

    @Subscribe
    public void onProxyReload(ProxyReloadEvent event) {
        this.logger.info("Ricaricamento di AutoCommands in corso...");
        preloadCommandManager();
        Messages.init(this.dataDirectory.resolve("messages.yml").toString(), this.logger);
        this.logger.info("AutoCommands ricaricato con successo!");
    }

    /**
     * onPostLogin
     * Metodo chiamato quando un giocatore si connette al proxy
     * @param event
     */
    @Subscribe
    public void onPostLogin(PostLoginEvent event) {
        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();
        pullAndExecute(uuid, "join");
    }

    /**
     * onDisconnect
     * Metodo chiamato quando un giocatore si disconnette dal proxy
     * @param event
     */
    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();
        pullAndExecute(uuid, "logout");
    }

    /**
     * onEnable
     * Metodo chiamato all'avvio del plugin, in cui viene caricata la configurazione
     */
    private void onEnable() {
        this.logger.info("Caricamento configurazione...");
        Messages.init(this.dataDirectory.resolve("messages.yml").toString(), this.logger);
        // load logic
        preloadCommandManager();
        registerCommands();
        this.logger.info("Configurazione completata!");
    }

    /**
     * onDisable
     * Metodo chiamato alla chiusura del plugin, in cui viene eseguita la logica di unload
     */
    private void onDisable() {
        // unload logic
        this.logger.info("Arresto completato!");
    }

    /**
     * onCommandCall
     * Metodo chiamato quando viene eseguito il comando /AutoCommands in game, prende la lista dei comandi da eseguire in Command e li esegue
     * @param args
     * @param source
     */
    public void onCommandCall(String[] args, CommandSource source) {
        String uuid = args[0];
        // "command" è la Phase che l'enum mappa come "onCommand"
        String[] comandiDaEseguire;
        try {
            comandiDaEseguire = gestoreComandi.getCommandList(uuid, "command");
        } catch (InvalidCommandMethodException e) {
            source.sendMessage(Messages.toComponent(Messages.get("cmd_exec_failed").replace("{uuid}", uuid)));
            source.sendMessage(Messages.toComponent(Messages.get("check_console")));
            this.logger.info(e.getMessage());
            return;
        } catch (MissingUserConfigException | EmptyCommandException e) {
            source.sendMessage(Messages.toComponent(Messages.get("no_command_for_user").replace("{uuid}", uuid)));
            return;
        } catch (MissingPluginConfigException e) {
            source.sendMessage(Messages.toComponent(Messages.get("no_plugin_config")));
            return;
        }
        source.sendMessage(Messages.toComponent(Messages.get("cmd_exec_success")
                .replace("{uuid}", uuid)
                .replace("{count}", String.valueOf(comandiDaEseguire.length))));

        CommandManager commandManager = proxy.getCommandManager();
        CommandSource console = proxy.getConsoleCommandSource();
        commandExecuter(console, commandManager, comandiDaEseguire);
    }
    
    public GestoreComandi getGestoreComandi() {
        return this.gestoreComandi;
    }

    private void pullAndExecute(String uuid, String method) {
        String[] comandiDaEseguire;
        try {
            comandiDaEseguire = gestoreComandi.getCommandList(uuid, method);
        } catch (MissingPluginConfigException | EmptyCommandException | MissingUserConfigException | InvalidCommandMethodException e) {
            this.logger.info(e.getMessage());
            return;
        }

        CommandManager commandManager = proxy.getCommandManager();
        CommandSource console = proxy.getConsoleCommandSource();
        commandExecuter(console, commandManager, comandiDaEseguire);
    }

    /**
     * commandExecuter
     * Metodo che esegue una lista di comandi in modo asincrono, prendendo come CommandSource la console del proxy
     * @param console
     * @param commandManager
     * @param comandiDaEseguire
     */
    private void commandExecuter(CommandSource console, CommandManager commandManager, String[] comandiDaEseguire) {
        this.logger.info("CommandExecuter running...");
        for (String comando : comandiDaEseguire)
            commandManager.executeAsync(console, comando).thenAccept(successo -> {
                if (!successo)
                    logger.warn("Impossibile eseguire il comando: " + comando);
                else
                    logger.info("Comando eseguito: " + comando);
            });
    }

    /**
     * createEmptyConfigFile
     * Metodo che crea un file di configurazione vuoto se non esiste
     */
    private void createEmptyConfigFile() {
        this.logger.info("Creazione file di configurazione...");
        try {
            if (!Files.exists(this.dataDirectory))
                Files.createDirectories(this.dataDirectory);
            
            Path configFile = this.dataDirectory.resolve(CONFIG_FILE_NAME);
            if (!Files.exists(configFile))
                try (java.io.InputStream in = AutoCommands.class.getClassLoader().getResourceAsStream(CONFIG_FILE_NAME)) {
                    if (in == null){
                        this.logger.error("Impossibile trovare " + CONFIG_FILE_NAME + " nelle risorse interne.");
                        return;
                    }
                    Files.copy(in, configFile);
                }
        } catch (IOException e) {
            this.logger.error("Impossibile creare il file di configurazione. Maggiori informazioni a seguire:");
            this.logger.error(e.getMessage());
        }
    }

    private void preloadCommandManager() {
        Path configFile = this.dataDirectory.resolve(CONFIG_FILE_NAME);
        if (!Files.exists(configFile))
            createEmptyConfigFile();
        
        try {
            this.gestoreComandi.reload();
        } catch (MissingPluginConfigException e) {
            this.logger.error(e.getMessage());
        }
    }

    private void registerCommands(){
        // Registrazione del comando /AutoCommands in game
        CommandManager commandManager = proxy.getCommandManager();
        commandManager.register(
            commandManager.metaBuilder("autocommands")
            .aliases("autocmd")
            .build(),
            new AutoCommandListener(this)
        );
    }
}
