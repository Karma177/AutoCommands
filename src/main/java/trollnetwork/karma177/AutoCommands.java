package trollnetwork.karma177;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import trollnetwork.karma177.Exceptions.EmptyCommandException;
import trollnetwork.karma177.Exceptions.InvalidCommandMethodException;
import trollnetwork.karma177.Exceptions.MissingPluginConfigException;
import trollnetwork.karma177.Exceptions.MissingUserConfigException;

import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Plugin(
        id = "autocommands",
        name = "AutoCommands",
        version = "1.0-STABLE",
        description = "Un plugin che esegue automaticamente dei comandi.",
        authors = {"Karma177"}
)
public class AutoCommands {

    private final ProxyServer proxy;
    private final Logger logger;
    private final GestoreComandi gestoreComandi;
    private final Path dataDirectory;
    private final String configFileName = "commands.json";
    

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
        this.gestoreComandi = new GestoreComandi(this.dataDirectory.toString(), configFileName);
    }

    /**
     * onProxyInitialization
     * Metodo chiamato all'avvio del proxy
     * @param event
     */
    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        this.logger.info("AutoCommands in avvio!");
        this.onEnable();
    }

    /**
     * onProxyShutdown
     * Metodo chiamato alla chiusura del proxy
     * @param event
     */
    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        this.logger.info("AutoCommands in arresto!");
        this.logger.info("AutoCommands... Comandi automatici, o comandi su ruote?");
        this.onDisable();
    }

    /**
     * onPostLogin
     * Metodo chiamato quando un giocatore si connette al proxy
     * @param event
     */
    @Subscribe
    public void onPostLogin(PostLoginEvent event) {
        Player player = event.getPlayer();
        String UUID = player.getUniqueId().toString();

        // Prendiamo la lista dei comandi da eseguire in Login
        String[] comandiDaEseguire;
        try {
            comandiDaEseguire = gestoreComandi.getCommandList(UUID, "join");
        } catch (MissingPluginConfigException | EmptyCommandException | MissingUserConfigException
                | InvalidCommandMethodException e) {
            this.logger.info(e.getMessage());
            return;
        }

        // Instanziamo il CommandManager e il CommandSource per eseguire i comandi come console
        CommandManager commandManager = proxy.getCommandManager();
        CommandSource console = proxy.getConsoleCommandSource();
        commandExecuter(console, commandManager, comandiDaEseguire);
    }

    /**
     * onCommandCall
     * Metodo chiamato quando viene eseguito il comando /AutoCommands in game, prende la lista dei comandi da eseguire in Command e li esegue
     * @param args
     * @param source
     */
    public void onCommandCall(String[] args, CommandSource source) {
        String UUID = args[0];
        // "command" è la Phase che l'enum mappa come "onCommand"
        String[] comandiDaEseguire;
        try {
            comandiDaEseguire = gestoreComandi.getCommandList(UUID, "command");
        } catch (InvalidCommandMethodException e) {
            source.sendMessage(Component.text("AutoCommands non è riuscito ad eseguire tutti i comandi per: ", NamedTextColor.RED));
            source.sendMessage(Component.text("[" + UUID + "]", NamedTextColor.RED));
            source.sendMessage(Component.text("Controlla la console per maggiori info.", NamedTextColor.RED));
            this.logger.info(e.getMessage());
            return;
        } catch(MissingUserConfigException e) {
            source.sendMessage(Component.text("Nessun comando da eseguire per: ", NamedTextColor.YELLOW));
            source.sendMessage(Component.text("[" + UUID + "]", NamedTextColor.YELLOW));
            return;
        } catch(EmptyCommandException e){
            source.sendMessage(Component.text("Nessun comando da eseguire per: ", NamedTextColor.YELLOW));
            source.sendMessage(Component.text("[" + UUID + "]", NamedTextColor.YELLOW));
            return;
        } catch(MissingPluginConfigException e){
            source.sendMessage(Component.text("Nessun file di config per il plugin trovato!", NamedTextColor.DARK_RED));
            return;
        }
        source.sendMessage(Component.text("AutoCommands (" + comandiDaEseguire.length + ") eseguiti per l'utente: ", NamedTextColor.GREEN));
        source.sendMessage(Component.text("[" + UUID + "]", NamedTextColor.GREEN));

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
    private void commandExecuter(CommandSource console, CommandManager commandManager,String[] comandiDaEseguire) {
        this.logger.info("CommandExecuter running...");
        for (String comando : comandiDaEseguire) {         
            // Eseguiamo il comando in modo asincrono
            commandManager.executeAsync(console, comando).thenAccept(successo -> {
                if (!successo) {
                    logger.warn("Impossibile eseguire il comando: " + comando);
                }else {
                    logger.info("Comando eseguito: " + comando);
                }
            });
        }
    }

    /**
     * createEmptyConfigFile
     * Metodo che crea un file di configurazione vuoto se non esiste
     */
    private void createEmptyConfigFile() {
        this.logger.info("Creazione file di configurazione...");
        try {
            if (!Files.exists(this.dataDirectory)) {
                Files.createDirectories(this.dataDirectory);
            }
            
            Path configFile = this.dataDirectory.resolve(this.configFileName);
            if (!Files.exists(configFile)) {
                String defaultJson = "{\n" +
                        "  \"UUID\": {\n" +
                        "    \"onJoin\": [\n" +
                        "      \"send user server\"\n" +
                        "    ],\n" +
                        "    \"onCommand\": [\n" +
                        "      \"send user server\"\n" +
                        "    ]\n" +
                        "  }\n" +
                        "}";
                Files.writeString(configFile, defaultJson);
            }
        } catch (IOException e) {
            this.logger.error("Impossibile creare il file di configurazione o le directory.", e);
        }
    }

    /**
     * onEnable
     * Metodo chiamato all'avvio del plugin, in cui viene caricata la configurazione
     */
    private void onEnable() {
        this.logger.info("Caricamento configurazione...");
        // load logic
        Path configFile = this.dataDirectory.resolve(this.configFileName);
        if (!Files.exists(configFile)) {
            this.logger.info("Nessun file trovato!");
            createEmptyConfigFile();
        }
        
        // Registrazione del comando /AutoCommands in game
        CommandManager commandManager = proxy.getCommandManager();
        commandManager.register(
            commandManager.metaBuilder("autocommands").build(),
            new AutoCommandListener(this)
        );
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

    public GestoreComandi getGestoreComandi() {
        return this.gestoreComandi;
    }
}