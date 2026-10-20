package eu.neydev.antiskull.service;

import eu.neydev.antiskull.config.AntiSkullConfig;
import eu.neydev.antiskull.config.type.PermissionNode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Сервис прав доступа.
 *
 * Узлы плагина делятся на два класса с противоположными правилами, и путать
 * их нельзя. Боевые узлы (place, take_away) - разрешающие обходы: при
 * выключенной системе прав они НЕ действуют, потому что «права выключены»
 * означает «запрет работает для всех», а не «ограничения сняты со всех».
 * Иначе одна строка settings.permissions.enabled: false молча разрешала бы
 * установку запрещенных голов всему серверу. Функциональные узлы командного
 * слоя (reload) устроены наоборот: выключенная система прав разрешает все.
 */
public class PermissionService {

    private final AntiSkullConfig config;

    public PermissionService(@NotNull AntiSkullConfig config) {
        this.config = config;
    }

    /**
     * Проверяет функциональное право (команда, справка, диагностика).
     *
     * @param sender проверяемый отправитель (игрок или консоль)
     * @param node   узел права
     * @return true если действие разрешено
     */
    public boolean has(@Nullable CommandSender sender, @NotNull PermissionNode node) {

        if (!config.arePermissionsEnabled()) {
            return true;
        }

        return sender != null && sender.hasPermission(config.getPermission(node));

    }

    /**
     * Проверяет право ставить запрещенные головы.
     *
     * @param player игрок, ставящий блок
     * @return true если проверка списка для него пропускается
     */
    public boolean canBypassBlock(@Nullable Player player) {
        return hasBypass(player, PermissionNode.PLACE);
    }

    /**
     * Проверяет право сохранить предмет при заблокированной установке.
     *
     * @param player игрок, ставящий блок
     * @return true если конфискация для него не применяется
     */
    public boolean canBypassTakeAway(@Nullable Player player) {
        return hasBypass(player, PermissionNode.TAKE_AWAY);
    }

    private boolean hasBypass(@Nullable Player player, @NotNull PermissionNode node) {
        return config.arePermissionsEnabled()
                && player != null
                && player.hasPermission(config.getPermission(node));
    }

}
