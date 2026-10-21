package eu.neydev.antiskull.command.sub;

import eu.neydev.antiskull.command.CommandContext;
import eu.neydev.antiskull.config.type.MessageKey;
import eu.neydev.antiskull.config.type.PermissionNode;
import eu.neydev.antiskull.config.type.TakeAwayType;
import eu.neydev.antiskull.registry.SkullRegistry;
import eu.neydev.antiskull.service.MessageService;
import eu.neydev.antiskull.support.StubConfig;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Подкоманда info: диагностика собирается из снапшота конфигурации, булевы
 * значения подставляются словами из той же конфигурации.
 */
class InfoSubCommandTest {

    private static final String TEMPLATE =
            "v={version} b={blocking} l={list} n={names} c={case_sensitive} t={take_away} "
                    + "tt={take_away_type} p={permissions}";

    private final Plugin plugin = mock(Plugin.class);
    private final CommandSender sender = mock(CommandSender.class);
    private final StubConfig config = new StubConfig()
            .withMessage(MessageKey.INFO, TEMPLATE)
            .withMessage(MessageKey.STATE_ON, "on")
            .withMessage(MessageKey.STATE_OFF, "off");

    @BeforeEach
    void setUp() {
        when(plugin.getDescription()).thenReturn(new PluginDescriptionFile(
                "NeyAntiSkull", "2.0.0", "eu.neydev.antiskull.NeyAntiSkull"));
    }

    private InfoSubCommand subCommand(String... names) {
        return new InfoSubCommand(plugin, config,
                new SkullRegistry(new StubConfig().withNames(names)),
                new MessageService(config));
    }

    private String sent() {

        ArgumentCaptor<String> message = ArgumentCaptor.forClass(String.class);

        verify(sender).sendMessage(message.capture());
        return message.getValue();

    }

    @Test
    @DisplayName("Название и узел права совпадают с plugin.yml")
    void nameAndPermissionMatchPluginYml() {

        assertEquals("info", subCommand().getName());
        assertEquals(PermissionNode.RELOAD, subCommand().getPermission());

    }

    @Test
    @DisplayName("Дефолты стаба совпадают с поставляемым конфигом")
    void reportsEverySettingFromTheSnapshot() {

        assertTrue(subCommand().execute(CommandContext.of(sender), new String[0]));

        assertEquals("v=2.0.0 b=on l=on n=0 c=off t=on tt=HAND p=off", sent());

    }

    @Test
    @DisplayName("Размер списка берется из реестра, а не из файла")
    void reportsRegistrySize() {

        subCommand("First", "Second", "Third").execute(CommandContext.of(sender), new String[0]);

        assertTrue(sent().contains("n=3"), sent());

    }

    @Test
    @DisplayName("Каждый выключенный флаг печатается словом state_off")
    void disabledFlagsUseStateOff() {

        config.withPluginEnabled(false)
                .withListEnabled(false)
                .withCaseSensitive(false)
                .withTakeAwayEnabled(false)
                .withPermissionsEnabled(false);

        subCommand().execute(CommandContext.of(sender), new String[0]);

        assertEquals("v=2.0.0 b=off l=off n=0 c=off t=off tt=HAND p=off", sent());

    }

    @Test
    @DisplayName("Режим конфискации печатается именем перечисления")
    void takeAwayTypeIsPrintedByName() {

        config.withTakeAwayType(TakeAwayType.ALL);

        subCommand().execute(CommandContext.of(sender), new String[0]);

        assertTrue(sent().contains("tt=ALL"), sent());

    }

    @Test
    @DisplayName("Учет регистра виден в диагностике")
    void caseSensitivityIsReported() {

        config.withCaseSensitive(true);

        subCommand().execute(CommandContext.of(sender), new String[0]);

        assertTrue(sent().contains("c=on"), sent());

    }

    @Test
    @DisplayName("Выключенное слово-фрагмент дает пустую подстановку")
    void disabledStateFragmentBecomesEmpty() {

        config.withMessage(MessageKey.STATE_ON);

        subCommand().execute(CommandContext.of(sender), new String[0]);

        assertTrue(sent().contains("b= "), sent());

    }

}
