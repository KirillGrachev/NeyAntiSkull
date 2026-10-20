package eu.neydev.antiskull.command.sub;

import eu.neydev.antiskull.command.CommandContext;
import eu.neydev.antiskull.command.AntiSkullSubCommand;
import eu.neydev.antiskull.config.AntiSkullConfig;
import eu.neydev.antiskull.config.type.MessageKey;
import eu.neydev.antiskull.config.type.PermissionNode;
import eu.neydev.antiskull.registry.SkullRegistry;
import eu.neydev.antiskull.service.MessageService;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Подкоманда /antiskull info.
 *
 * Диагностика читается из снапшота конфигурации, а не из файла: команда
 * показывает ровно те правила, которые сейчас применяет слушатель. Все
 * булевы значения подставляются словами state_on/state_off из той же
 * конфигурации, поэтому перевод диагностики не расходится с переводом сообщений.
 */
public class InfoSubCommand implements AntiSkullSubCommand {

    private final Plugin plugin;
    private final AntiSkullConfig config;
    private final SkullRegistry skullRegistry;
    private final MessageService messageService;

    public InfoSubCommand(@NotNull Plugin plugin,
                          @NotNull AntiSkullConfig config,
                          @NotNull SkullRegistry skullRegistry,
                          @NotNull MessageService messageService) {
        this.plugin = plugin;
        this.config = config;
        this.skullRegistry = skullRegistry;
        this.messageService = messageService;
    }

    @Override
    public @NotNull String getName() {
        return "info";
    }

    @Override
    public @NotNull PermissionNode getPermission() {
        return PermissionNode.RELOAD;
    }

    @Override
    public boolean execute(@NotNull CommandContext context, String @NotNull [] args) {

        messageService.send(context.sender(), MessageKey.INFO, Map.of(
                "version", plugin.getDescription().getVersion(),
                "blocking", state(config.isPluginEnabled()),
                "list", state(config.isListEnabled()),
                "names", String.valueOf(skullRegistry.size()),
                "case_sensitive", state(config.isCaseSensitive()),
                "take_away", state(config.isTakeAwayEnabled()),
                "take_away_type", config.getTakeAwayType().name(),
                "permissions", state(config.arePermissionsEnabled())
        ));

        return true;

    }

    /**
     * Возвращает слово-фрагмент для булева значения.
     *
     * Выключенный фрагмент дает пустую строку, а не "null": диагностика
     * остается читаемой даже тогда, когда серверовладелец убрал слова.
     */
    private @NotNull String state(boolean enabled) {
        return String.join("", config.getMessages(enabled ? MessageKey.STATE_ON : MessageKey.STATE_OFF));
    }

}
