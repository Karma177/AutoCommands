package trollnetwork.karma177;

/**
 * EventMethod
 * Enum che rappresenta i metodi di evento supportati dal plugin, con la chiave JSON corrispondente.
 */
public enum EventMethod {
        join("onJoin"),
        command("onCommand");

        private final String jsonKey;

        EventMethod(String jsonKey) {
            this.jsonKey = jsonKey;
        }

        public String getJsonKey() {
            return jsonKey;
        }
    }