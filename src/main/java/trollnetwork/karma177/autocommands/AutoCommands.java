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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import trollnetwork.karma177.autocommands.utils.Messages;

@Plugin(
        id = "autocommands",
        name = "AutoCommands",
        version = "1.2-STABLE",
        description = "Autoesecuzione di comandi predeterminati.",
        authors = {"Karma177"}
)
public class AutoCommands {

    private final ProxyServer proxy;
    private final Logger logger;
    private final GestoreComandi gestoreComandi;
    private final Path dataDirectory;
    private final String version = "1.2-STABLE";
    private static final String USER_LIST = "users.json";
    private static final String GROUP_LIST = "groups.json";

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
        this.gestoreComandi = new GestoreComandi(this.dataDirectory.toString(), USER_LIST, GROUP_LIST);
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
        reload();
    }

    public void reload() {
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
        try{

            pullAndExecuteAllForUser(uuid, "login");
        } catch (MissingPluginConfigException | EmptyCommandException | MissingUserConfigException
                | InvalidCommandMethodException e) {
            this.logger.error(e.getMessage());
        }
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
        try {
            pullAndExecuteAllForUser(uuid, "logout");
        } catch (MissingPluginConfigException | EmptyCommandException | MissingUserConfigException
                | InvalidCommandMethodException e) {
            this.logger.error(e.getMessage());
        }
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
    
    public GestoreComandi getGestoreComandi() {
        return this.gestoreComandi;
    }

    public Logger getLogger(){
        return this.logger;
    }

    public int[] pullAndExecuteAllForUser(String uuid, String method) throws MissingPluginConfigException, EmptyCommandException, MissingUserConfigException, InvalidCommandMethodException {
        return pullAndExecuteMaster(uuid, method, "all");
    }

    public int[] pullExecuteUserExclusive(String uuid, String method) throws MissingPluginConfigException, EmptyCommandException, MissingUserConfigException, InvalidCommandMethodException {
        return pullAndExecuteMaster(uuid, method, "user");
    }

    public int[] pullExecuteAllUsersInGroup(String group, String method) throws MissingPluginConfigException, EmptyCommandException, MissingUserConfigException, InvalidCommandMethodException {
        return pullAndExecuteMaster(group, method, "group");
    }

    private int[] pullAndExecuteMaster(String target, String method, String targetType) throws MissingPluginConfigException, EmptyCommandException, MissingUserConfigException, InvalidCommandMethodException {
        List<String> comandiDaEseguire = new ArrayList<>();
        switch(targetType){
            case "user" -> comandiDaEseguire.addAll(Arrays.asList(applyUUID(gestoreComandi.getCommandList(target, method), target)));
            case "group" -> {
                String[] userUUIDs = gestoreComandi.getUsersFromGroup(target);
                String[] groupCommands = gestoreComandi.getGroupCommands(target, method);
                for(String uuid : userUUIDs)
                    comandiDaEseguire.addAll(Arrays.asList(applyUUID(groupCommands, uuid)));
            }
            case "all" -> {
                // Prendiamo tutti gli utenti e per ognuno prendiamo i comandi di gruppo e utente
                comandiDaEseguire.addAll(Arrays.asList(applyUUID(gestoreComandi.getCommandList(target, method), target)));
                comandiDaEseguire.addAll(Arrays.asList(applyUUID(gestoreComandi.getGroupCommandsFromUUID(target, method), target)));
            }
            default -> throw new InvalidCommandMethodException("Il target "+targetType+" non è un metodo per accedere alla lista comandi valido.");
        }

        CommandManager commandManager = proxy.getCommandManager();
        CommandSource console = proxy.getConsoleCommandSource();
        int successCount = commandExecuter(console, commandManager, comandiDaEseguire.toArray(new String[0]));

        return new int[]{comandiDaEseguire.size(), successCount};
    }

    private String[] applyUUID(String[] commands, String uuid){
        return Arrays.stream(commands)
                .map(cmd -> cmd.replace("{uuid}", uuid))
                .toArray(String[]::new);
    }
    


    /**
     * commandExecuter
     * Metodo che esegue una lista di comandi in modo asincrono, prendendo come CommandSource la console del proxy
     * @param console
     * @param commandManager
     * @param comandiDaEseguire
     */
    private int commandExecuter(CommandSource console, CommandManager commandManager, String[] comandiDaEseguire) {
        this.logger.info("CommandExecuter running...");
        AtomicInteger totalCommands = new AtomicInteger(comandiDaEseguire.length);
        AtomicInteger successCount = new AtomicInteger(0);
        for (String command : comandiDaEseguire){
            this.logger.info("Eseguendo:"+command);
            totalCommands.decrementAndGet();
            if(command == null || command.isBlank()){
                this.logger.warn("Impossibile eseguire il comando: '" + command + "'");
                this.logger.warn("Comando vuoto o null ignorato.");
            }else{
                try{
                    commandManager.executeAsync(console, command).thenAccept(successo -> {
                        if (!successo)
                            logger.warn("Impossibile eseguire il comando: " + command);
                        else{
                            logger.info("Comando eseguito: " + command);
                            successCount.incrementAndGet();
                        }
                    });
                }catch(Exception e){
                    logger.warn("Impossibile eseguire il comando: '" + command + "'. Errore: " + e.getMessage());
                }
            }
        }
        this.logger.info("Esecuzione comandi avviata: " + comandiDaEseguire.length + " comandi da eseguire.");
        this.logger.info("Eseguiti prima di uscire: " + totalCommands.get());
        return successCount.get();
    }

    /**
     * createEmptyConfigFile
     * Metodo che crea un file di configurazione vuoto se non esiste
     */
    private void createEmptyUserList() {
        this.logger.info("Creazione file di configurazione per gli utenti...");
        try {
            if (!Files.exists(this.dataDirectory))
                Files.createDirectories(this.dataDirectory);
            
            Path configFile = this.dataDirectory.resolve(USER_LIST);
            if (!Files.exists(configFile))
                try (java.io.InputStream in = AutoCommands.class.getClassLoader().getResourceAsStream(USER_LIST)) {
                    if (in == null){
                        this.logger.error("Impossibile trovare " + USER_LIST + " nelle risorse interne.");
                        return;
                    }
                    Files.copy(in, configFile);
                }
        } catch (IOException e) {
            this.logger.error("Impossibile creare il file di configurazione. Maggiori informazioni a seguire:");
            this.logger.error(e.getMessage());
        }
    }

    /**
     * createEmptyConfigFile
     * Metodo che crea un file di configurazione vuoto se non esiste
     */
    private void createEmptyGroupList() {
        this.logger.info("Creazione file di configurazione per gli utenti...");
        try {
            if (!Files.exists(this.dataDirectory))
                Files.createDirectories(this.dataDirectory);
            
            Path configFile = this.dataDirectory.resolve(GROUP_LIST);
            if (!Files.exists(configFile))
                try (java.io.InputStream in = AutoCommands.class.getClassLoader().getResourceAsStream(GROUP_LIST)) {
                    if (in == null){
                        this.logger.error("Impossibile trovare " + GROUP_LIST + " nelle risorse interne.");
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
        Path userList = this.dataDirectory.resolve(USER_LIST);
        Path groupList = this.dataDirectory.resolve(GROUP_LIST);
        if (!Files.exists(userList))
            createEmptyUserList();

        if(!Files.exists(groupList))
            createEmptyGroupList();

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

    public String getVersion() {
        return this.version;
    }
}
