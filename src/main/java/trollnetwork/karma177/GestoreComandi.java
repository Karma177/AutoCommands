package trollnetwork.karma177;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import trollnetwork.karma177.Exceptions.EmptyCommandException;
import trollnetwork.karma177.Exceptions.InvalidCommandMethodException;
import trollnetwork.karma177.Exceptions.MissingPluginConfigException;
import trollnetwork.karma177.Exceptions.MissingUserConfigException;

public class GestoreComandi {
    
    private String fileDirectory;
    private final String configFileName;
    // gson = lib per convertire da JSON a oggetti Java e viceversa
    private final Gson gson;

    public GestoreComandi(String fileDirectory, String configFileName) {
        this.fileDirectory = fileDirectory;
        this.configFileName = configFileName;
        this.gson = new Gson();
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
    public Map<String, Map<String, List<String>>> loadCommandsFromFile() throws MissingPluginConfigException {
        File file = new File(this.fileDirectory, this.configFileName);
        try {
            if (file.exists() && file.isFile()) {
                Map<String, Map<String, List<String>>> fileData = parseJSON(file);
                if (fileData != null) {
                    return fileData;
                }
            }
        } catch (FileNotFoundException e) {
            throw new MissingPluginConfigException(this.fileDirectory + "/" + this.configFileName + " non trovato");
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
        FileReader reader = new FileReader(file);
        Type type = new TypeToken<Map<String, Map<String, List<String>>>>() {}.getType();
        return gson.fromJson(reader, type);
    }

    /**
     * getAvailableUUIDs()
     * Legge la configurazione e restituisce una lista di tutti gli UUID presenti nel file JSON
     * @return Array di UUID disponibili
     */
    public String[] getAvailableUUIDs() {
        try {
            Map<String, Map<String, List<String>>> commands = loadCommandsFromFile();
            if (commands != null) {
                return commands.keySet().toArray(new String[0]);
            }
        } catch (MissingPluginConfigException e) {
            // Ignora se non trovato durante l'autocomplete
        }
        return new String[0];
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
        EventMethod eventMethod;
        try {
            eventMethod = EventMethod.valueOf(method);
        } catch (IllegalArgumentException e) {
            throw new InvalidCommandMethodException("Metodo di esecuzione del comando non valido: " + method);
        }

        Map<String, Map<String, List<String>>> commands = loadCommandsFromFile();
        if (commands != null) {
            Map<String, List<String>> userCommands = commands.get(UUID);
            if (userCommands != null) {
                List<String> eventCommands = userCommands.get(eventMethod.getJsonKey());
                if (eventCommands != null) {
                    return eventCommands.toArray(new String[0]);
                }
                throw new EmptyCommandException("Nessun comando " + eventMethod.getJsonKey() + " per l'utente " + UUID);
            }
            throw new MissingUserConfigException("Nessuna configurazione trovata per l'utente " + UUID);
        }
        throw new MissingPluginConfigException("File di configurazione del plugin vuoto!");
    }

}
