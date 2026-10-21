package eu.neydev.antiskull.config;

import eu.neydev.antiskull.NeyAntiSkull;
import eu.neydev.antiskull.config.type.MessageKey;
import eu.neydev.antiskull.config.type.PermissionNode;
import eu.neydev.antiskull.config.type.TakeAwayType;
import eu.neydev.antiskull.service.ConsoleService;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Проверка менеджера конфигурации на настоящем YAML-файле: дефолты, чтение
 * секций, окраска имен, устаревшие пути 1.x, предупреждения и перезагрузка.
 *
 * Сервер не поднимается: плагин мокается, getDataFolder указывает на
 * временный каталог, а getConfig перечитывает файл на каждый вызов - так
 * reload() менеджера ведет себя как на живом ядре.
 */
class ConfigManagerTest {

    @TempDir
    Path tempDir;

    private final List<LogRecord> consoleRecords = new ArrayList<>();

    private NeyAntiSkull plugin() {

        NeyAntiSkull plugin = mock(NeyAntiSkull.class);

        when(plugin.getDataFolder()).thenReturn(tempDir.toFile());
        when(plugin.getLogger()).thenReturn(countingLogger());
        // getConfig перечитывает файл на каждый вызов: reload() дергает
        // штатный reloadConfig (мок-noop) и забирает свежий конфиг
        when(plugin.getConfig()).thenAnswer(answer -> loadFromDisk());
        return plugin;

    }

    private ConfigManager manager(String yaml) {

        writeConfig(yaml);

        NeyAntiSkull plugin = plugin();
        return new ConfigManager(plugin, new ConsoleService(plugin));

    }

    /**
     * Читает config.yml из tempDir без Bukkit-логгера: в тестах сервера нет,
     * а штатный loadConfiguration при битом YAML полез бы в Bukkit.getLogger().
     */
    private YamlConfiguration loadFromDisk() {

        YamlConfiguration loaded = new YamlConfiguration();

        try {
            loaded.load(tempDir.resolve("config.yml").toFile());
        } catch (Exception ignored) {
            // Битый или отсутствующий файл: пустая конфигурация, как в продакшене
        }

        return loaded;

    }

    private Logger countingLogger() {

        Logger logger = Logger.getLogger("antiskull-config-test-" + System.nanoTime());

        logger.setUseParentHandlers(false);
        logger.addHandler(new Handler() {

            @Override
            public void publish(LogRecord record) {
                consoleRecords.add(record);
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }

        });

        return logger;

    }

    private void writeConfig(String yaml) {
        try {
            Files.writeString(tempDir.resolve("config.yml"), yaml);
        } catch (IOException exception) {
            throw new IllegalStateException("Не удалось записать тестовый config.yml", exception);
        }
    }

    private boolean hasRecord(Level level, String token) {
        return consoleRecords.stream()
                .filter(record -> record.getLevel().equals(level))
                .anyMatch(record -> String.valueOf(record.getMessage()).contains(token));
    }

    @Test
    @DisplayName("Пустой файл дает дефолты, совпадающие с поставляемым конфигом")
    void emptyFileFallsBackToCodeDefaults() {

        ConfigManager manager = manager("");

        assertTrue(manager.isPluginEnabled());
        assertTrue(manager.isListEnabled());
        assertFalse(manager.isCaseSensitive());
        assertEquals(List.of(), manager.getDisallowedSkullNames());
        assertTrue(manager.isTakeAwayEnabled(), "дефолт кода совпадает с файлом 1.x");
        assertEquals(TakeAwayType.HAND, manager.getTakeAwayType());
        assertFalse(manager.arePermissionsEnabled());
        assertTrue(manager.areMessagesEnabled());
        assertEquals("", manager.getMessagePrefix());
        assertEquals("antiskull.bypass.place", manager.getPermission(PermissionNode.PLACE));
        assertEquals("antiskull.bypass.takeaway", manager.getPermission(PermissionNode.TAKE_AWAY));
        assertEquals("antiskull.reload", manager.getPermission(PermissionNode.RELOAD));
        assertFalse(hasRecord(Level.SEVERE, "broken"), "пустой файл - не поломка");

    }

    @Test
    @DisplayName("Отсутствующий файл не считается битым")
    void missingFileIsNotBroken() {

        NeyAntiSkull plugin = plugin();
        ConfigManager manager = new ConfigManager(plugin, new ConsoleService(plugin));

        assertTrue(manager.isPluginEnabled());
        assertFalse(hasRecord(Level.SEVERE, "broken"));

    }

    @Test
    @DisplayName("Все секции читаются из файла")
    void readsEverySection() {

        ConfigManager manager = manager("""
                settings:
                  enabled: false
                  list:
                    enabled: false
                    case_sensitive: true
                    names:
                      - "Admin Head"
                  take_away:
                    enabled: false
                    type: ALL
                permissions:
                  enabled: true
                  place: "donor.place"
                  take_away: "donor.keep"
                messages:
                  enabled: false
                  prefix: "&#ff0000AS &8» &7"
                """);

        assertFalse(manager.isPluginEnabled());
        assertFalse(manager.isListEnabled());
        assertTrue(manager.isCaseSensitive());
        assertEquals(List.of("Admin Head"), manager.getDisallowedSkullNames());
        assertFalse(manager.isTakeAwayEnabled());
        assertEquals(TakeAwayType.ALL, manager.getTakeAwayType());
        assertTrue(manager.arePermissionsEnabled());
        assertEquals("donor.place", manager.getPermission(PermissionNode.PLACE));
        assertEquals("donor.keep", manager.getPermission(PermissionNode.TAKE_AWAY));
        assertFalse(manager.areMessagesEnabled());
        assertTrue(manager.getMessagePrefix().contains("§x"), "префикс окрашен");

    }

    @Test
    @DisplayName("Имена из списка окрашиваются: & и HEX превращаются в коды §")
    void namesAreColoredOnRead() {

        ConfigManager manager = manager("""
                settings:
                  list:
                    names:
                      - "&aPlayer Head"
                      - "#42fffcPlayer Head"
                """);

        List<String> names = manager.getDisallowedSkullNames();

        assertEquals(2, names.size());
        assertEquals("§aPlayer Head", names.get(0), "legacy-код переведен");
        assertTrue(names.get(1).startsWith("§x§4§2§f§f§f§c"), "HEX развернут: " + names.get(1));

    }

    @Test
    @DisplayName("Одно имя строкой и пустые элементы списка обрабатываются")
    void scalarNameAndNullElements() {

        ConfigManager scalar = manager("settings:\n  list:\n    names: \"Solo Head\"\n");

        assertEquals(List.of("Solo Head"), scalar.getDisallowedSkullNames());

        ConfigManager withNulls = manager("""
                settings:
                  list:
                    names:
                      - "first"
                      -
                      - ""
                      - "last"
                """);

        assertEquals(List.of("first", "last"), withNulls.getDisallowedSkullNames(),
                "пустое имя не должно блокировать безымянные головы");

    }

    @Test
    @DisplayName("Неизвестный режим конфискации дает HAND с предупреждением")
    void unknownTakeAwayTypeFallsBackWithWarning() {

        ConfigManager manager = manager("settings:\n  take_away:\n    type: EVERYTHING\n");

        assertEquals(TakeAwayType.HAND, manager.getTakeAwayType());
        assertTrue(hasRecord(Level.WARNING, "settings.take_away.type"));
        assertTrue(hasRecord(Level.WARNING, "EVERYTHING"));

    }

    @Test
    @DisplayName("Режим конфискации читается без учета регистра")
    void takeAwayTypeIsCaseInsensitive() {

        ConfigManager manager = manager("settings:\n  take_away:\n    type: all\n");

        assertEquals(TakeAwayType.ALL, manager.getTakeAwayType());
        assertFalse(hasRecord(Level.WARNING, "take_away"));

    }

    @Test
    @DisplayName("Секция прав 1.x читается с подсказкой в консоль")
    void legacyPermissionSectionIsRead() {

        ConfigManager manager = manager("""
                settings:
                  permissions:
                    enabled: true
                    place: "legacy.place"
                    take_away: "legacy.keep"
                """);

        assertTrue(manager.arePermissionsEnabled());
        assertEquals("legacy.place", manager.getPermission(PermissionNode.PLACE));
        assertEquals("legacy.keep", manager.getPermission(PermissionNode.TAKE_AWAY));
        assertTrue(hasRecord(Level.WARNING, "settings.permissions.place"));
        assertTrue(hasRecord(Level.WARNING, "permissions.place"));

    }

    @Test
    @DisplayName("Новая секция прав побеждает устаревшую без предупреждения")
    void newPermissionSectionWins() {

        ConfigManager manager = manager("""
                permissions:
                  enabled: true
                  place: "new.place"
                settings:
                  permissions:
                    enabled: false
                    place: "legacy.place"
                """);

        assertTrue(manager.arePermissionsEnabled());
        assertEquals("new.place", manager.getPermission(PermissionNode.PLACE));
        assertFalse(hasRecord(Level.WARNING, "settings.permissions.place"));

    }

    @Test
    @DisplayName("Устаревшее сообщение 1.x читается")
    void legacyMessageSectionIsRead() {

        ConfigManager manager = manager("""
                messages:
                  on_place:
                    enabled: true
                    text:
                      - "legacy warning"
                """);

        assertEquals(List.of("legacy warning"), manager.getMessages(MessageKey.BLOCKED));
        assertTrue(hasRecord(Level.WARNING, "messages.on_place"));

    }

    @Test
    @DisplayName("Выключенное сообщение дает пустой список строк")
    void disabledMessageBecomesEmptyList() {

        ConfigManager manager = manager("""
                messages:
                  blocked:
                    enabled: false
                    text:
                      - "не должно читаться"
                """);

        assertEquals(List.of(), manager.getMessages(MessageKey.BLOCKED));

    }

    @Test
    @DisplayName("Текст одной строкой становится списком из одной строки")
    void scalarTextBecomesSingleLine() {

        ConfigManager manager = manager("messages:\n  reload:\n    text: \"single line\"\n");

        assertEquals(List.of("single line"), manager.getMessages(MessageKey.RELOAD));

    }

    @Test
    @DisplayName("Дефолт сообщения о блокировке трехстрочный, как в файле")
    void defaultMessageKeepsBlankLines() {

        List<String> lines = manager("").getMessages(MessageKey.BLOCKED);

        assertEquals(3, lines.size());
        assertEquals("", lines.get(0));
        assertEquals("", lines.get(2));
        assertTrue(lines.get(1).contains("prohibited"), lines.get(1));

    }

    @Test
    @DisplayName("Битый YAML кричит в SEVERE и работает на дефолтах")
    void brokenYamlLogsSevere() {

        ConfigManager manager = manager("settings: [\n  enabled: true\n");

        assertTrue(manager.isPluginEnabled(), "подставлен дефолт");
        assertTrue(hasRecord(Level.SEVERE, "config.yml is broken"));

    }

    @Test
    @DisplayName("reload перечитывает файл и уведомляет подписчиков")
    void reloadRereadsFileAndNotifies() {

        writeConfig("settings:\n  list:\n    names:\n      - \"First\"\n");

        NeyAntiSkull plugin = plugin();
        ConfigManager manager = new ConfigManager(plugin, new ConsoleService(plugin));
        AtomicInteger notifications = new AtomicInteger();
        manager.onReload(notifications::incrementAndGet);

        assertEquals(List.of("First"), manager.getDisallowedSkullNames());

        writeConfig("settings:\n  list:\n    names:\n      - \"Second\"\n");
        manager.reload();

        assertEquals(List.of("Second"), manager.getDisallowedSkullNames());
        assertEquals(1, notifications.get());

    }

    @Test
    @DisplayName("Снапшот подменяется целиком: reload не рвет согласованность")
    void reloadSwapsWholeSnapshot() {

        writeConfig("""
                settings:
                  list:
                    enabled: true
                    names:
                      - "Old"
                  permissions:
                    enabled: false
                """);

        NeyAntiSkull plugin = plugin();
        ConfigManager manager = new ConfigManager(plugin, new ConsoleService(plugin));

        writeConfig("""
                settings:
                  list:
                    enabled: false
                    names:
                      - "New"
                permissions:
                  enabled: true
                """);
        manager.reload();

        assertFalse(manager.isListEnabled());
        assertEquals(List.of("New"), manager.getDisallowedSkullNames());
        assertTrue(manager.arePermissionsEnabled());

    }

}
