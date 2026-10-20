package eu.neydev.antiskull.config;

import eu.neydev.antiskull.config.type.MessageKey;
import eu.neydev.antiskull.config.type.PermissionNode;
import eu.neydev.antiskull.config.type.TakeAwayType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Контракт конфигурации NeyAntiSkull.
 * Все остальные компоненты зависят от интерфейса, а не от реализации:
 * сервисы и слушатель не знают, откуда приехали значения (YAML, дефолты
 * кода или подставленный в тесте стаб).
 */
public interface AntiSkullConfig {

    /**
     * Мастер-выключатель плагина: при false установка не проверяется вовсе.
     */
    boolean isPluginEnabled();

    /**
     * Работает ли список имен.
     *
     * false означает «блокировать все головы», а не «разрешить все»:
     * так settings.list.enabled: false используется как тотальный запрет.
     */
    boolean isListEnabled();

    /**
     * Учитывается ли регистр при сравнении имени головы со списком.
     */
    boolean isCaseSensitive();

    /**
     * Имена запрещенных голов, уже окрашенные: сравнение идет с legacy-именем
     * предмета, в котором цветовые коды представлены символами §.
     */
    @NotNull List<String> getDisallowedSkullNames();

    boolean isTakeAwayEnabled();

    @NotNull TakeAwayType getTakeAwayType();

    boolean arePermissionsEnabled();

    /**
     * Строка узла права из конфигурации (или дефолт узла).
     *
     * @param node узел права
     * @return название права для Bukkit
     */
    @NotNull String getPermission(@NotNull PermissionNode node);

    boolean areMessagesEnabled();

    /**
     * Префикс, подставляемый в плейсхолдер {prefix}. Уже окрашен.
     */
    @NotNull String getMessagePrefix();

    /**
     * Строки сообщения по ключу.
     *
     * @param key ключ сообщения
     * @return окрашенные строки; пустой список, если сообщение выключено
     */
    @NotNull List<String> getMessages(@NotNull MessageKey key);

}
