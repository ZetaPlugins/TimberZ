package com.zetaplugins.timberz.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.zetaplugins.timberz.TimberZ;
import com.zetaplugins.timberz.service.MessageService;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class TimberZCommand {
    private final TimberZ plugin;

    public TimberZCommand(TimberZ plugin) {
        this.plugin = plugin;
    }

    /**
     * Builds the root command for TimberZ.
     * @param commandName the name of the command, e.g., "timberz"
     * @return a LiteralCommandNode representing the root command
     */
    public LiteralCommandNode<CommandSourceStack> buildRootCommand(String commandName) {
        boolean isToggleEnabled = plugin.getConfig().getBoolean("toggleTimber");

        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(commandName);

        root
                .executes(this::executeVersion);

        root.then(Commands.literal("reload")
                .requires(source -> source.getSender().hasPermission("timberz.admin"))
                .executes(this::executeReload));

        root.then(Commands.literal("help")
                .executes(this::executeHelp));

        root.then(Commands.literal("toggle")
                .requires(source -> source.getSender().hasPermission("timberz.usetimber") && isToggleEnabled)
                .executes(this::executeToggle));

        return root.build();
    }

    private int executeVersion(CommandContext<CommandSourceStack> context) {
        context.getSource().getSender().sendMessage(plugin.getMessageService().getAndFormatMsg(
                true,
                "versionMsg",
                "&7You are using version %ac%%version%",
                new MessageService.Replaceable<>(
                        "%version%",
                        plugin.getDescription().getVersion()
                )
        ));
        return Command.SINGLE_SUCCESS;
    }

    private int executeHelp(CommandContext<CommandSourceStack> context) {
        context.getSource().getSender().sendMessage(plugin.getMessageService().getAndFormatMsg(
                false,
                "help",
                "\\n &8<b>></b> <#00D26A><b><gradient:#00D26A:#00B24F>ServerLinksZ</gradient></b></#00D26A> <grey>v%version%</grey>\\n\\n <click:OPEN_URL:https://docs.zetaplugins.com/timberz>%ac%<u><b>Documentation</b></u></click>   <click:OPEN_URL:https://strassburger.org/discord>%ac%<u><b>Support Discord</b></u></click>\\n",
                new MessageService.Replaceable<>(
                        "%version%",
                        plugin.getDescription().getVersion()
                )
        ));
        return Command.SINGLE_SUCCESS;
    }

    private int executeReload(CommandContext<CommandSourceStack> context) {
        plugin.getConfigService().clearCache();
        plugin.reloadConfig();
        plugin.getLocalizationService().reload();
        plugin.getTreeDetectionService().fetchLogToLeaveMap();
        plugin.getPlayerStateService().cleanupAll();

        context.getSource().getSender().sendMessage(plugin.getMessageService().getAndFormatMsg(
                true,
                "reloadMsg",
                "&7Successfully reloaded the plugin!"
        ));
        return Command.SINGLE_SUCCESS;
    }

    private int executeToggle(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessageService().getAndFormatMsg(
                    false,
                    "playerOnlyCommand",
                    "This command can only be used by players!"
            ));
            return Command.SINGLE_SUCCESS;
        }

        plugin.getPlayerStateService().toggleTimberState(player);

        return Command.SINGLE_SUCCESS;
    }
}