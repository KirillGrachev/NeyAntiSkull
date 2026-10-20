package eu.neydev.antiskull.config.type;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Узлы прав плагина.
 *
 * Оба боевых узла - разрешающие (bypass): place снимает запрет на установку,
 * take_away спасает предмет от конфискации. reload - функциональный узел
 * командного слоя.
 *
 * {@code legacyPath} в отличие от остальных плагинов семьи хранит ПОЛНЫЙ путь,
 * а не ключ: в 1.x секция прав жила внутри settings (settings.permissions.place)
 * и переехала на верхний уровень, поэтому префиксом отделаться нельзя.
 * Дефолты совпадают с поставляемым config.yml 1.x, а не с дефолтами кода 1.x -
 * те расходились с файлом (antiskull.place вместо antiskull.bypass.place) и
 * проявлялись только на сервере с потерянным конфигом.
 */
public enum PermissionNode {

    PLACE("place", "settings.permissions.place", "antiskull.bypass.place"),
    TAKE_AWAY("take_away", "settings.permissions.take_away", "antiskull.bypass.takeaway"),
    RELOAD("reload", null, "antiskull.reload");

    private final String configKey;
    private final @Nullable String legacyPath;
    private final String defaultPermission;

    PermissionNode(String configKey, @Nullable String legacyPath, String defaultPermission) {
        this.configKey = configKey;
        this.legacyPath = legacyPath;
        this.defaultPermission = defaultPermission;
    }

    public @NotNull String getConfigKey() {
        return configKey;
    }

    /**
     * Полный путь узла в конфигурации 1.x или null, если узел появился в 2.0.0.
     */
    public @Nullable String getLegacyPath() {
        return legacyPath;
    }

    public @NotNull String getDefaultPermission() {
        return defaultPermission;
    }

}
