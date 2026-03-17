package trollnetwork.karma177.autocommands.utils;

import com.velocitypowered.api.command.SimpleCommand.Invocation;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.permission.PermissionFunction;
import com.velocitypowered.api.permission.Tristate;

public class PermissionChecker {
    
    /**
     * Controlla se la source che effettua il comando ha il permesso per usarlo.
     * @param invocation L'invocazione del comando da verificare.
     * @return true se ha il permesso, false altrimenti.
     */
    public static boolean hasCommandPermission(Invocation invocation) {
        return invocation.source().hasPermission("autocommands.admin");
    }

    /**
     * Controlla se un giocatore ha il permesso.
     * @param player Il giocatore da verificare.
     * @return true se il giocatore ha il permesso, false altrimenti.
     */
    public static boolean hasCommandPermission(Player player) {
        return player.hasPermission("autocommands.admin");
    }

    /**
     * Controlla se una funzione di permessi in fase di setup concede il permesso al momento.
     * Utile durante il PermissionsSetupEvent prima che vengano assegnati i permessi reali al proxy.
     * @param function La funzione di permessi nativa.
     * @return true se c'è un riscontro esplicito (TRUE), altrimenti false.
     */
    public static Tristate hasPermissionForFunction(PermissionFunction function) {
        return function.getPermissionValue("autocommands.admin");
    }
}
