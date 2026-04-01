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
import trollnetwork.karma177.autocommands.Exceptions.NoCommandsForGroupException;
import trollnetwork.karma177.autocommands.Exceptions.NoCommandsForUserException;
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

        switch (args.length) {
            case 0 -> {
                source.sendMessage(Messages.toComponent(Messages.get("usage_error")));
            }
            case 1 -> {
                switch (args[0]) {
                    case "version" -> {
                        source.sendMessage(Messages.toComponent(this.plugin.getVersion()));
                    }
                    case "help" -> {
                        for (Entry<String, Component> line : Messages.getHelp().entrySet())
                            source.sendMessage(line.getValue());
                    }
                    case "reload" -> {
                        source.sendMessage(Messages.toComponent(Messages.get("reload_success")));
                        this.plugin.reload();
                    }
                    default -> {
                        source.sendMessage(Messages.toComponent(Messages.get("usage_error")));
                    }
                }
            }
            case 3 -> {
                if (args[0].equalsIgnoreCase("run")) {

                    int[] executed = { -1, -1 };
                    try {
                        switch (args[1]) {
                            case "user" -> {
                                executed = this.plugin.pullExecuteUserExclusive(args[2], "run");
                            }
                            case "group" -> {
                                executed = this.plugin.pullExecuteAllUsersInGroup(args[2], "run");
                            }
                            case "all" -> {
                                executed = this.plugin.pullAndExecuteAllForUser(args[2], "run");
                            }
                            default -> {
                                source.sendMessage(Messages.toComponent(Messages.get("usage_error")));
                                return;
                            }
                        }
                    } catch (InvalidCommandMethodException e) {
                        source.sendActionBar(Messages.toComponent(Messages.get("cmd_exec_failed")
                            .replace("{uuid}", args[2])
                            .replace("{countSuccess}", String.valueOf(executed[1]))
                            .replace("{countTotal}", String.valueOf(executed[0]))));
                            
                        source.sendMessage(Messages.toComponent(Messages.get("check_console")));
                        this.plugin.getLogger().info(e.getMessage());
                        return;
                    } catch (MissingUserConfigException | EmptyCommandException e) {
                        source.sendMessage(
                            Messages.toComponent(Messages.get("no_command_for_user").replace("{uuid}", args[2])));
                        return;
                    } catch (MissingPluginConfigException e) {
                        source.sendActionBar(Messages.toComponent(Messages.get("no_plugin_config")));
                        return;
                    } catch (IllegalArgumentException e) {
                        source.sendMessage(Messages.toComponent(Messages.get("no_group_with_name").replace("{group}", args[2])));
                        return;
                    } catch (NoCommandsForUserException e) {
                        source.sendMessage(Messages.toComponent(Messages.get("no_commands_for_group_or_user").replace("{uuid}", args[2])));
                        return;
                    } catch (NoCommandsForGroupException e) {
                        source.sendMessage(Messages.toComponent(Messages.get("no_commands_for_group").replace("{group}", args[2])));
                        return;
                    }

                    if (executed[0] == executed[1])
                        source.sendActionBar(Messages.toComponent(Messages.get("cmd_exec_success")
                            .replace("{uuid}", args[1])
                            .replace("{countSuccess}", String.valueOf(executed[1]))
                            .replace("{countTotal}", String.valueOf(executed[0]))));
                    else
                        source.sendActionBar(Messages.toComponent(Messages.get("cmd_exec_failed")
                            .replace("{uuid}", args[1])
                            .replace("{countSuccess}", String.valueOf(executed[1]))
                            .replace("{countTotal}", String.valueOf(executed[0]))));
                }else {
                    source.sendMessage(Messages.toComponent(Messages.get("usage_error")));
                    return;
                }
            }
            default -> {
                source.sendMessage(Messages.toComponent(Messages.get("usage_error")));
            }
        }

    }

    @Override
    public List<String> suggest(Invocation invocation) {
        if (!PermissionChecker.hasCommandPermission(invocation)) {
            return Collections.emptyList();
        }

        String[] args = invocation.arguments();
        List<String> suggestions = new ArrayList<>();

        switch (args.length) {
            case 0:
            case 1:
                String partial0 = args.length == 0 ? "" : args[0].toLowerCase();
                String[] rootCommands = { "run", "version", "help", "reload" };
                for (String cmd : rootCommands) {
                    if (cmd.startsWith(partial0)) {
                        suggestions.add(cmd);
                    }
                }
                break;

            case 2:
                if (args[0].equalsIgnoreCase("run")) {
                    String partial1 = args[1].toLowerCase();
                    String[] runSubCommands = { "user", "group", "all" };
                    for (String cmd : runSubCommands) {
                        if (cmd.startsWith(partial1)) {
                            suggestions.add(cmd);
                        }
                    }
                }
                break;

            case 3:
                if (args[0].equalsIgnoreCase("run")) {
                    String partial2 = args[2].toLowerCase();
                    String[] availableTarget = new String[0];
                    if (args[1].equalsIgnoreCase("user") || args[1].equalsIgnoreCase("all")) {
                        availableTarget = this.plugin.getGestoreComandi().getAvailableUUIDs();
                    } else if (args[1].equalsIgnoreCase("group")) {
                        availableTarget = this.plugin.getGestoreComandi().getAvailableGroups();
                    }
                    for (String target : availableTarget) {
                        if (target.toLowerCase().startsWith(partial2)) {
                            suggestions.add(target);
                        }
                    }
                }
                break;
        }

        return suggestions;
    }

}
