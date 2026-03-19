package trollnetwork.karma177.autocommands;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import trollnetwork.karma177.autocommands.Exceptions.EmptyCommandException;
import trollnetwork.karma177.autocommands.Exceptions.InvalidCommandMethodException;
import trollnetwork.karma177.autocommands.Exceptions.MissingPluginConfigException;
import trollnetwork.karma177.autocommands.Exceptions.MissingUserConfigException;

public class GestoreComandi {
    
    private final String userList;
    private final String groupList;
    // gson = lib per convertire da JSON a oggetti Java e viceversa
    private final Gson gson;
    private String fileDirectory;
    private Map<String, Map<String, List<String>>> commandsCache;
    private Map<String, Map<String, List<String>>> groupsCache;
    private List<String> methods = Arrays.asList("join","run","leave");

    public GestoreComandi(String fileDirectory, String userList, String groupList) {
        this.fileDirectory = fileDirectory;
        this.userList = userList;
        this.groupList = groupList;
        this.gson = new Gson();
        this.commandsCache = null;
    }

    public Map<String, Map<String, List<String>>> getCommands() throws MissingPluginConfigException {
        if (commandsCache == null) {
            commandsCache = loadCommandsFromFile();
        }
        return commandsCache;
    }

    public Map<String, Map<String, List<String>>> getGroups() throws MissingPluginConfigException {
        if (commandsCache == null) {
            commandsCache = loadGroupsFromFile();
        }
        return commandsCache;
    }

    /**
     * getAvailableUUIDs()
     * Legge la configurazione e restituisce una lista di tutti gli UUID presenti nel file JSON
     * @return Array di UUID disponibili
     */
    public String[] getAvailableUUIDs() {
        try {
            return getCommands().keySet().toArray(new String[0]);
        } catch (MissingPluginConfigException e) {
            return new String[0];
        }
    }


    /**
     * getCommandList()
     * Restituisce la lista dei comandi da eseguire per un UUID specifico in base al metodo scelto
     * @param UUID - L'UUID dell'utente per cui vogliamo ottenere i comandi
     * @param method - Il metodo di esecuzione del comando (es. "join", "command")
     * @return
     * @throws MissingPluginConfigException (FileNotFoundException)
     * @throws EmptyCommandException 
     * @throws MissingUserConfigException 
     * @throws InvalidCommandMethodException 
     */
    public String[] getCommandList(String UUID, String method) throws MissingPluginConfigException, EmptyCommandException, MissingUserConfigException, InvalidCommandMethodException {
        if(!this.methods.contains("method"))
            throw new InvalidCommandMethodException("Il metodo "+method+" non è un metodo per accedere alla lista comandi valido.");

        Map<String, Map<String, List<String>>> commands = this.getCommands();
        if (commands == null) {
            throw new MissingPluginConfigException("File di configurazione del plugin vuoto!");
        }

        Map<String, List<String>> userCommands = commands.get(UUID);
        if (userCommands == null) {
            throw new MissingUserConfigException("Nessuna configurazione trovata per l'utente " + UUID);
        }

        List<String> eventCommands = userCommands.get(method);
        if (eventCommands == null || eventCommands.isEmpty()) {
            throw new EmptyCommandException("Nessun comando " + method + " per l'utente " + UUID);
        }
        
        return eventCommands.toArray(new String[0]);
    }

    public String[] getGroupCommandsFromUUID(String UUID, String method){
        String[] userGroups = this.checkMemberships(UUID);
        List<String> commands = new ArrayList<>();
        for(String group : userGroups){
            commands.addAll(Arrays.asList(getGroupCommands(group, method)));
        }
        return commands.toArray(new String[0]);
    }

    public String[] getGroupCommands(String group, String method){
        return this.groupsCache.get(group).get(method).toArray(new String[0]);
    }

    public String[] checkMemberships(String UUID){
        List<String> groups = new ArrayList<>();;

        for(Map.Entry<String, Map<String, List<String>>> group : this.groupsCache.entrySet()){
            if(group.getValue().containsKey(UUID))
                groups.add(group.getKey());
        };

        return groups.toArray(new String[0]);
    }


    /**
     * reload()
     * Ricarica la configurazione dei comandi da file, aggiornando la cache
     * @return true se ricaricato, false altrimenti
     * @throws MissingPluginConfigException
     */
    public boolean reload() throws MissingPluginConfigException{
        try{
            this.commandsCache = parseJSON(new File(this.fileDirectory, this.userList));
            this.groupsCache = parseJSON(new File(this.fileDirectory, this.groupList));
        } catch (FileNotFoundException e) {
            throw new MissingPluginConfigException(this.fileDirectory + "\\" + this.userList + " non trovato");
        }

        return this.commandsCache != null && this.groupsCache != null;
    }

    /**
    * loadCommandsFromFile() 
    * Carica i comandi da un file JSON e li restituisce come una mappa
    * La struttura del file JSON dovrebbe essere:
    * {
    *   "uuid": {
    *     "onJoin": ["comando1", "comando2"],
    *     "onLeave": ["comando3"]
    *   },
    *   "altro_uuid": {
    *     "onJoin": ["comando4"]
    *   }
    * }
    *
    * @return Map<String, Map<String, List<String>>> - Mappa che associa ogni uuid a una mappa di comandi
    * @throws MissingPluginConfigException (FileNotFoundException)
    */
    private Map<String, Map<String, List<String>>> loadCommandsFromFile() throws MissingPluginConfigException {
        File file = new File(this.fileDirectory, this.userList);
        
        try {
            if (file.exists() && file.isFile()) {
                Map<String, Map<String, List<String>>> fileData = parseJSON(file);
                if (fileData != null)
                    return fileData;
            }
        } catch (FileNotFoundException e) {
            throw new MissingPluginConfigException(this.fileDirectory + "\\" + this.userList + " non trovato");
        }
        
        return null;
    }


    /**
    * loadGroupsFromFile() 
    * @return Map<String, Map<String, List<String>>> - Mappa che associa ogni guppo  a una mappa di comandi e uuids
    * @throws MissingPluginConfigException (FileNotFoundException)
    */
    private Map<String, Map<String, List<String>>> loadGroupsFromFile() throws MissingPluginConfigException {
        File file = new File(this.fileDirectory, this.groupList);
        
        try {
            if (file.exists() && file.isFile()) {
                Map<String, Map<String, List<String>>> fileData = parseJSON(file);
                if (fileData != null)
                    return fileData;
            }
        } catch (FileNotFoundException e) {
            throw new MissingPluginConfigException(this.fileDirectory + "\\" + this.groupList + " non trovato");
        }
        
        return null;
    }

    /**
     * parseJSON(File file)
     * Legge un file JSON e lo converte in una mappa
     * @param file
     * @return
     * @throws FileNotFoundException
     */
    private Map<String, Map<String, List<String>>> parseJSON(File file) throws FileNotFoundException {
        Type type = new TypeToken<Map<String, Map<String, List<String>>>>() {}.getType();
        try (FileReader reader = new FileReader(file)) {
            return gson.fromJson(reader, type);
        } catch (FileNotFoundException e) {
            throw e; // Rilanciamola se deve esser gestita all'esterno
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
