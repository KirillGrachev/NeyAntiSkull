package eu.neydev.antiskull.config.type;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Контракты перечислений конфигурации: уникальные ключи, непустые дефолты,
 * устаревшие пути ровно там, где ключ переехал в 2.0.0, и разбор TakeAwayType.
 */
class ConfigTypesTest {

    @Test
    @DisplayName("Ключи сообщений уникальны и имеют непустые дефолты")
    void messageKeysAreUnique() {

        Set<String> keys = new HashSet<>();

        for (MessageKey key : MessageKey.values()) {

            assertTrue(keys.add(key.getConfigKey()), "дубль ключа " + key.getConfigKey());
            assertFalse(key.getConfigKey().isBlank(), "пустой ключ у " + key);
            assertNotNull(key.getDefaultMessage(), "нет дефолта у " + key);
            assertFalse(key.getDefaultMessage().isBlank(), "пустой дефолт у " + key);
            assertTrue(key.isDefaultEnabled(), "сообщение выключено по умолчанию: " + key);

        }

        assertEquals(MessageKey.values().length, keys.size());

    }

    @Test
    @DisplayName("Устаревший путь помечен только у blocked")
    void onlyRenamedMessageCarriesLegacyKey() {

        assertEquals("on_place", MessageKey.BLOCKED.getLegacyConfigKey());

        for (MessageKey key : MessageKey.values()) {
            if (key != MessageKey.BLOCKED) {
                assertNull(key.getLegacyConfigKey(), "неожиданный legacy-ключ у " + key);
            }
        }

    }

    @Test
    @DisplayName("Узлы прав уникальны, дефолты совпадают с plugin.yml")
    void permissionNodesMatchPluginYml() {

        Set<String> keys = new HashSet<>();

        for (PermissionNode node : PermissionNode.values()) {

            assertTrue(keys.add(node.getConfigKey()), "дубль ключа " + node.getConfigKey());
            assertFalse(node.getDefaultPermission().isBlank(), "пустое право у " + node);

        }

        assertEquals("antiskull.bypass.place", PermissionNode.PLACE.getDefaultPermission());
        assertEquals("antiskull.bypass.takeaway", PermissionNode.TAKE_AWAY.getDefaultPermission());
        assertEquals("antiskull.reload", PermissionNode.RELOAD.getDefaultPermission());

    }

    @Test
    @DisplayName("Переехавшая секция прав помечена полным legacy-путем")
    void movedPermissionSectionCarriesFullPath() {

        assertEquals("settings.permissions.place", PermissionNode.PLACE.getLegacyPath());
        assertEquals("settings.permissions.take_away", PermissionNode.TAKE_AWAY.getLegacyPath());
        assertNull(PermissionNode.RELOAD.getLegacyPath(), "узел появился в 2.0.0");

    }

    @Test
    @DisplayName("Шаблоны консоли содержат свои плейсхолдеры")
    void consoleTemplatesCarryPlaceholders() {

        for (ConsoleMessage message : ConsoleMessage.values()) {

            String template = message.getDefaultTemplate();

            assertFalse(template.isBlank(), "пустой шаблон у " + message);
            assertTrue(template.contains("{"), "нет плейсхолдеров у " + message);

        }

        assertTrue(ConsoleMessage.INVALID_VALUE.getDefaultTemplate().contains("{path}"));
        assertTrue(ConsoleMessage.INVALID_VALUE.getDefaultTemplate().contains("{value}"));
        assertTrue(ConsoleMessage.INVALID_VALUE.getDefaultTemplate().contains("{defaultValue}"));
        assertTrue(ConsoleMessage.LEGACY_PATH.getDefaultTemplate().contains("{replacement}"));
        assertTrue(ConsoleMessage.COMMAND_MISSING.getDefaultTemplate().contains("{command}"));
        assertTrue(ConsoleMessage.COMMAND_UNREGISTER_FAILED.getDefaultTemplate().contains("{reason}"));

    }

    @Test
    @DisplayName("Тип конфискации разбирается без учета регистра и пробелов")
    void takeAwayTypeIsParsedLeniently() {

        assertSame(TakeAwayType.HAND, TakeAwayType.fromString("HAND"));
        assertSame(TakeAwayType.HAND, TakeAwayType.fromString("hand"));
        assertSame(TakeAwayType.ALL, TakeAwayType.fromString("  all "));

    }

    @Test
    @DisplayName("Неизвестное и пустое значение типа дают null")
    void unknownTakeAwayTypeIsNull() {

        assertNull(TakeAwayType.fromString("EVERYTHING"));
        assertNull(TakeAwayType.fromString(""));
        assertNull(TakeAwayType.fromString("   "));
        assertNull(TakeAwayType.fromString(null));

    }

    @Test
    @DisplayName("Оба режима конфискации перечислены")
    void takeAwayTypeHasExactlyTwoModes() {
        assertEquals(2, TakeAwayType.values().length);
    }

}
