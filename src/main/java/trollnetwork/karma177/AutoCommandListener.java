package trollnetwork.karma177;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AutoCommandListener implements SimpleCommand {

    private final AutoCommands plugin;

    public AutoCommandListener(AutoCommands plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();
        // Controlliamo che l'UUID sia stato passato come argomento
        if (args.length == 0) {
            source.sendMessage(Component.text("Uso corretto: /autocommands <uuid>", NamedTextColor.RED));
            return;
        }

        if(args.length > 1) {
            source.sendMessage(Component.text("Troppi argomenti! Uso corretto: /autocommands <uuid>", NamedTextColor.RED));
            return;
        }

        this.plugin.onCommandCall(args, source);
        
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        
        if (args.length <= 1) {
            String partial = args.length == 0 ? "" : args[0].toLowerCase();
            String[] availableUUIDs = this.plugin.getGestoreComandi().getAvailableUUIDs();
            List<String> suggestions = new ArrayList<>();
            for (String uuid : availableUUIDs) {
                if (uuid.toLowerCase().startsWith(partial)) {
                    suggestions.add(uuid);
                }
            }
            return suggestions;
        }
        
        return Collections.emptyList();
    }
}
