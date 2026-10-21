package eu.neydev.antiskull.registry;

import eu.neydev.antiskull.support.StubConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Реестр имен: нормализация регистра, пересборка на reload и независимость
 * от локали сервера.
 */
class SkullRegistryTest {

    @Test
    @DisplayName("Имена читаются из конфигурации при создании")
    void loadsNamesFromConfig() {

        SkullRegistry registry = new SkullRegistry(new StubConfig().withNames("Player Head", "&aAdmin"));

        assertEquals(2, registry.size());
        assertTrue(registry.isListed("Player Head"));
        assertTrue(registry.isListed("&aAdmin"));

    }

    @Test
    @DisplayName("По умолчанию регистр не учитывается")
    void caseIsIgnoredByDefault() {

        SkullRegistry registry = new SkullRegistry(new StubConfig().withNames("Player Head"));

        assertTrue(registry.isListed("PLAYER HEAD"));
        assertTrue(registry.isListed("player head"));

    }

    @Test
    @DisplayName("С настройкой case_sensitive сравнение становится точным")
    void caseSensitiveModeIsExact() {

        SkullRegistry registry = new SkullRegistry(new StubConfig()
                .withCaseSensitive(true)
                .withNames("Player Head"));

        assertTrue(registry.isListed("Player Head"));
        assertFalse(registry.isListed("PLAYER HEAD"));

    }

    @Test
    @DisplayName("Пустые имена не блокируют безымянные головы")
    void emptyNamesAreSkipped() {

        SkullRegistry registry = new SkullRegistry(new StubConfig().withNames("", "Player Head"));

        assertEquals(1, registry.size());
        assertFalse(registry.isListed(""));
        assertFalse(registry.isListed(null));

    }

    @Test
    @DisplayName("reload пересобирает список, а не дополняет его")
    void reloadReplacesTheWholeList() {

        StubConfig config = new StubConfig().withNames("Old Name", "Keep");
        SkullRegistry registry = new SkullRegistry(config);

        assertTrue(registry.isListed("Old Name"));

        config.withNames("Keep", "New Name");
        registry.reload();

        assertFalse(registry.isListed("Old Name"), "удаленное из конфига имя больше не блокируется");
        assertTrue(registry.isListed("Keep"));
        assertTrue(registry.isListed("New Name"));
        assertEquals(2, registry.size());

    }

    @Test
    @DisplayName("Приведение регистра не зависит от локали сервера")
    void normalizationIsLocaleIndependent() {

        Locale previous = Locale.getDefault();

        try {

            Locale.setDefault(Locale.forLanguageTag("tr-TR"));

            SkullRegistry registry = new SkullRegistry(new StubConfig().withNames("TITLE"));

            assertTrue(registry.isListed("title"),
                    "турецкая локаль не должна превращать I в ı");

        } finally {
            Locale.setDefault(previous);
        }

    }

    @Test
    @DisplayName("Повторяющиеся имена схлопываются в одно")
    void duplicateNamesCollapse() {

        SkullRegistry registry = new SkullRegistry(new StubConfig()
                .withNames("Player Head", "player head", "PLAYER HEAD"));

        assertEquals(1, registry.size());
        assertEquals(java.util.Set.of("player head"), registry.getNames());

    }

}
