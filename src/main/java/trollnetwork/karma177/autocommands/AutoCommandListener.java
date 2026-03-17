package trollnetwork.karma177.autocommands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import trollnetwork.karma177.autocommands.utils.Messages;
import trollnetwork.karma177.autocommands.utils.PermissionChecker;

public class AutoCommandListener implements SimpleCommand {

    private final AutoCommands plugin;

    public AutoCommandListener(AutoCommands plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        
        CommandSource source = invocation.source();

        if (!PermissionChecker.hasCommandPermission(invocation)) {
            source.sendMessage(Messages.toComponent(Messages.get("no_permission")));
            return;
        }

        String[] args = invocation.arguments();
        // Controlliamo che l'UUID sia stato passato come argomento
        if (args.length == 0) {
            source.sendMessage(Messages.toComponent(Messages.get("usage_error")));
            return;
        }

        if(args.length > 1) {
            source.sendMessage(Messages.toComponent(Messages.get("too_many_args")));
            return;
        }

        this.plugin.onCommandCall(args, source);
        
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        if (!PermissionChecker.hasCommandPermission(invocation)) {
            return Collections.emptyList();
        }

        String[] args = invocation.arguments();
        
        if (args.length <= 1) {
            String partial = args.length == 0 ? "" : args[0].toLowerCase();
            String[] availableUUIDs = this.plugin.getGestoreComandi().getAvailableUUIDs();
            List<String> suggestions = new ArrayList<>();
            for (String uuid : availableUUIDs)
                if (uuid.toLowerCase().startsWith(partial))
                    suggestions.add(uuid);
            
            return suggestions;
        }
        
        return Collections.emptyList();
    }
    
}
