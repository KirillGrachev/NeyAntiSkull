package eu.neydev.antiskull.command;

import eu.neydev.antiskull.config.type.MessageKey;
import eu.neydev.antiskull.service.MessageService;
import eu.neydev.antiskull.service.PermissionService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Обработчик команды /antiskull.
 *
 * Без аргументов и при неизвестной подкоманде печатается справка: у плагина
 * нет состояния игрока и аргументов-значений, поэтому «подкоманда по
 * умолчанию» здесь не нужна - в отличие от /gps, где аргументом могла быть
 * метка. Проверка имени команды защищает от чужого executor'а: Bukkit
 * допускает привязку одного обработчика к нескольким командам.
 */
public class AntiSkullCommand implements TabExecutor {

    private static final String COMMAND_NAME = "antiskull";

    private final MessageService messageService;
    private final PermissionService permissionService;
    private final List<AntiSkullSubCommand> subCommands;

    public AntiSkullCommand(@NotNull MessageService messageService,
                                @NotNull PermissionService permissionService,
                                @NotNull List<AntiSkullSubCommand> subCommands) {
        this.messageService = messageService;
        this.permissionService = permissionService;
        this.subCommands = List.copyOf(subCommands);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             String @NotNull [] args) {

        if (!command.getName().equalsIgnoreCase(COMMAND_NAME)) {
            return false;
        }

        AntiSkullSubCommand subCommand = args.length == 0 ? null : findSubCommand(args[0]);

        if (subCommand == null) {
            messageService.send(sender, MessageKey.USAGE);
            return true;
        }

        return execute(subCommand, CommandContext.of(sender), Arrays.copyOfRange(args, 1, args.length));

    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender,
                                               @NotNull Command command,
                                               @NotNull String alias,
                                               String @NotNull [] args) {

        if (!command.getName().equalsIgnoreCase(COMMAND_NAME)) {
            return List.of();
        }

        CommandContext context = CommandContext.of(sender);

        if (args.length == 1) {
            return filter(availableSubCommands(context), args[0]);
        }

        AntiSkullSubCommand subCommand = findSubCommand(args[0]);

        if (subCommand == null || !hasAccess(context, subCommand)) {
            return List.of();
        }

        return filter(subCommand.complete(context, Arrays.copyOfRange(args, 1, args.length)),
                args[args.length - 1]);

    }

    private boolean execute(@NotNull AntiSkullSubCommand subCommand,
                            @NotNull CommandContext context,
                            String @NotNull [] args) {

        if (!hasAccess(context, subCommand)) {
            messageService.send(context.sender(), MessageKey.NO_PERMISSION);
            return true;
        }

        return subCommand.execute(context, args);

    }

    private boolean hasAccess(@NotNull CommandContext context,
                              @NotNull AntiSkullSubCommand subCommand) {
        return subCommand.getPermission() == null
                || permissionService.has(context.sender(), subCommand.getPermission());
    }

    private @Nullable AntiSkullSubCommand findSubCommand(@NotNull String argument) {
        return subCommands.stream()
                .filter(subCommand -> subCommand.getName().equalsIgnoreCase(argument))
                .findFirst()
                .orElse(null);
    }

    private @NotNull List<String> availableSubCommands(@NotNull CommandContext context) {
        return subCommands.stream()
                .filter(subCommand -> hasAccess(context, subCommand))
                .map(AntiSkullSubCommand::getName)
                .toList();
    }

    private @NotNull List<String> filter(@NotNull List<String> values, @NotNull String argument) {
        String prefix = argument.toLowerCase(Locale.ROOT);
        return values.stream()
                .filter(value -> value.toLowerCase(Locale.ROOT).startsWith(prefix))
                .toList();
    }

}
