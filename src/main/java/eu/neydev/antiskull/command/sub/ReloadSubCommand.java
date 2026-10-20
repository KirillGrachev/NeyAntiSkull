package eu.neydev.antiskull.command.sub;

import eu.neydev.antiskull.command.CommandContext;
import eu.neydev.antiskull.command.AntiSkullSubCommand;
import eu.neydev.antiskull.config.ConfigManager;
import eu.neydev.antiskull.config.type.MessageKey;
import eu.neydev.antiskull.config.type.PermissionNode;
import eu.neydev.antiskull.registry.SkullRegistry;
import eu.neydev.antiskull.service.MessageService;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Подкоманда /antiskull reload.
 *
 * Перечитывает config.yml; реестр имен обновляется подпиской на reload
 * конфигурации, а не отдельным вызовом здесь - иначе порядок шагов зависел
 * бы от того, кто и в какой последовательности дергает reload.
 * В подтверждение уходит и время, и размер списка: по количеству имен
 * сразу видно, что список не потерялся при правке файла.
 */
public class ReloadSubCommand implements AntiSkullSubCommand {

    private final ConfigManager configManager;
    private final SkullRegistry skullRegistry;
    private final MessageService messageService;

    public ReloadSubCommand(@NotNull ConfigManager configManager,
                            @NotNull SkullRegistry skullRegistry,
                            @NotNull MessageService messageService) {
        this.configManager = configManager;
        this.skullRegistry = skullRegistry;
        this.messageService = messageService;
    }

    @Override
    public @NotNull String getName() {
        return "reload";
    }

    @Override
    public @NotNull PermissionNode getPermission() {
        return PermissionNode.RELOAD;
    }

    @Override
    public boolean execute(@NotNull CommandContext context, String @NotNull [] args) {

        long startedAt = System.nanoTime();
        configManager.reload();
        long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);

        messageService.send(context.sender(), MessageKey.RELOAD, Map.of(
                "time", String.valueOf(elapsedMillis),
                "names", String.valueOf(skullRegistry.size())
        ));

        return true;

    }

}
