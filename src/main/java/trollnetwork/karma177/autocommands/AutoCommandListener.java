package trollnetwork.karma177.autocommands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;

import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import trollnetwork.karma177.autocommands.Exceptions.EmptyCommandException;
import trollnetwork.karma177.autocommands.Exceptions.InvalidCommandMethodException;
import trollnetwork.karma177.autocommands.Exceptions.MissingPluginConfigException;
import trollnetwork.karma177.autocommands.Exceptions.MissingUserConfigException;
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

        switch(args.length){
            case 0->{
                source.sendMessage(Messages.toComponent(Messages.get("usage_error")));
            }
            case 1->{
                switch(args[0]){
                    case "run"->{
                        int executed = -1;
                        try {
                            executed = this.plugin.pullAndExecute(args[1], "command");   
                        } catch (InvalidCommandMethodException e) {
                            source.sendMessage(Messages.toComponent(Messages.get("cmd_exec_failed").replace("{uuid}", args[1])));
                            source.sendMessage(Messages.toComponent(Messages.get("check_console")));
                            this.plugin.getLogger().info(e.getMessage());
                            return;
                        } catch (MissingUserConfigException | EmptyCommandException e) {
                            source.sendMessage(Messages.toComponent(Messages.get("no_command_for_user").replace("{uuid}", args[1])));
                            return;
                        } catch (MissingPluginConfigException e) {
                            source.sendMessage(Messages.toComponent(Messages.get("no_plugin_config")));
                            return;
                        }

                        if(executed!=-1)
                            source.sendActionBar(Messages.toComponent(Messages.get("cmd_exec_success")
                                    .replace("{uuid}", args[1])
                                    .replace("{count}", String.valueOf(executed))));
                        else
                            source.sendActionBar(Messages.toComponent(Messages.get("cmd_exec_failed")
                                    .replace("{uuid}", args[1])
                                    .replace("{count}", String.valueOf(executed))));
                    }
                    case "version"->{
                        source.sendMessage(Messages.toComponent(this.plugin.getVersion()));
                    }
                    case "help"->{
                        for(Entry<String, Component> line : Messages.getHelp().entrySet())
                            source.sendMessage(line.getValue());
                    }
                    case "reload"->{
                        source.sendMessage(Messages.toComponent(Messages.get("reload_success")));
                        this.plugin.reload();
                    }
                }   
            }
            default->{
                source.sendMessage(Messages.toComponent(Messages.get("too_many_args")));
            }
        }
        
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
