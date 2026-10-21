package eu.neydev.antiskull.command.sub;

import eu.neydev.antiskull.command.CommandContext;
import eu.neydev.antiskull.config.ConfigManager;
import eu.neydev.antiskull.config.type.MessageKey;
import eu.neydev.antiskull.config.type.PermissionNode;
import eu.neydev.antiskull.registry.SkullRegistry;
import eu.neydev.antiskull.service.MessageService;
import eu.neydev.antiskull.support.StubConfig;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Подкоманда reload: конфигурация действительно перечитывается, а отправитель
 * получает подтверждение со временем и размером списка.
 */
class ReloadSubCommandTest {

    private final ConfigManager configManager = mock(ConfigManager.class);
    private final CommandSender sender = mock(CommandSender.class);
    private final StubConfig config = new StubConfig()
            .withMessage(MessageKey.RELOAD, "reloaded in {time} ms, {names} names");

    private ReloadSubCommand subCommand(String... names) {
        return new ReloadSubCommand(configManager,
                new SkullRegistry(new StubConfig().withNames(names)),
                new MessageService(config));
    }

    @Test
    @DisplayName("Название и узел права совпадают с plugin.yml")
    void nameAndPermissionMatchPluginYml() {

        assertEquals("reload", subCommand().getName());
        assertEquals(PermissionNode.RELOAD, subCommand().getPermission());

    }

    @Test
    @DisplayName("Конфигурация перечитывается, отправитель получает время и размер списка")
    void reloadsConfigAndReportsElapsed() {

        assertTrue(subCommand("First Head", "Second Head").execute(CommandContext.of(sender), new String[0]));

        verify(configManager).reload();

        ArgumentCaptor<String> message = ArgumentCaptor.forClass(String.class);

        verify(sender).sendMessage(message.capture());
        assertTrue(message.getValue().matches("reloaded in \\d+ ms, 2 names"), message.getValue());

    }

    @Test
    @DisplayName("Аргументы после названия подкоманды игнорируются")
    void extraArgumentsAreIgnored() {

        assertTrue(subCommand().execute(CommandContext.of(sender), new String[]{"now", "please"}));

        verify(configManager).reload();

    }

    @Test
    @DisplayName("Автодополнения у reload нет")
    void completionIsEmpty() {
        assertEquals(List.of(), subCommand().complete(CommandContext.of(sender), new String[0]));
    }

}
