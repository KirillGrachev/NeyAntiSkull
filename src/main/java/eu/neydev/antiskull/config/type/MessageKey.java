package eu.neydev.antiskull.config.type;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Ключи сообщений плагина.
 *
 * Каждый ключ хранит путь в config.yml, дефолтный текст на английском
 * и признак включенности по умолчанию. {@code legacyConfigKey} - путь
 * версии 1.x: конфигурации старше 2.0.0 продолжают читаться, а в консоль
 * уходит подсказка о переименовании (файл на диске не переписывается).
 */
public enum MessageKey {

    BLOCKED("blocked", "on_place", true,
            "\n #efff5e[⚠] #ecff45Attention! Placement of this item is prohibited. \n"),

    NO_PERMISSION("no_permission", null, true,
            "{prefix}&#ff6b6bYou do not have permission to do this."),

    USAGE("usage", null, true, "{prefix}&#8ce0ffNeyAntiSkull commands:\n"
            + "&f/antiskull reload &8- &7reload the configuration\n"
            + "&f/antiskull info &8- &7current blocking rules and the list size"),

    RELOAD("reload", null, true,
            "{prefix}&#9dff8cConfiguration reloaded &7({time} ms, {names} names in the list)."),

    INFO("info", null, true, "{prefix}&7Version: &f{version}\n"
            + "&7Blocking: &f{blocking}\n"
            + "&7Name list: &f{list} &8({names} names)\n"
            + "&7Case sensitive: &f{case_sensitive}\n"
            + "&7Take away: &f{take_away} &8({take_away_type})\n"
            + "&7Permissions: &f{permissions}");

    private final String configKey;
    private final @Nullable String legacyConfigKey;
    private final boolean defaultEnabled;
    private final String defaultMessage;

    MessageKey(String configKey, @Nullable String legacyConfigKey,
               boolean defaultEnabled, String defaultMessage) {
        this.configKey = configKey;
        this.legacyConfigKey = legacyConfigKey;
        this.defaultEnabled = defaultEnabled;
        this.defaultMessage = defaultMessage;
    }

    public @NotNull String getConfigKey() {
        return configKey;
    }

    /**
     * Путь секции в конфигурации 1.x или null, если ключ появился в 2.0.0.
     */
    public @Nullable String getLegacyConfigKey() {
        return legacyConfigKey;
    }

    /**
     * Включено ли сообщение, если его секции нет в конфигурации.
     */
    public boolean isDefaultEnabled() {
        return defaultEnabled;
    }

    public @NotNull String getDefaultMessage() {
        return defaultMessage;
    }

}
