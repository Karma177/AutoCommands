package trollnetwork.karma177.autocommands;

/**
 * EventMethod
 * Enum che rappresenta i metodi di evento supportati dal plugin, con la chiave JSON corrispondente.
 */
public enum EventMethod {
        join("onJoin"),
        command("onCommand"),
        logout("onLogout");

        private final String jsonKey;

        EventMethod(String jsonKey) {
            this.jsonKey = jsonKey;
        }

        public String getJsonKey() {
            return jsonKey;
        }
    }
