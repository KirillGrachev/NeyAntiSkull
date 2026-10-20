package eu.neydev.antiskull.listener;

import eu.neydev.antiskull.config.AntiSkullConfig;
import eu.neydev.antiskull.config.type.MessageKey;
import eu.neydev.antiskull.event.SkullPlaceBlockedEvent;
import eu.neydev.antiskull.service.MessageService;
import eu.neydev.antiskull.service.PermissionService;
import eu.neydev.antiskull.service.SkullRemovalService;
import eu.neydev.antiskull.service.SkullValidationService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * Слушатель установки блоков: головы из списка не ставятся.
 *
 * Приоритет HIGHEST с ignoreCancelled: решение принимается после плагинов,
 * которые уже отменили или разрешили установку (защита регионов, приваты),
 * но до MONITOR, читающего итог. Отмененный кем-то другим BlockPlaceEvent
 * не обрабатывается вовсе.
 *
 * Все зависимости приходят готовыми из контейнера: версия 1.x создавала
 * сервисы в конструкторе слушателя и новый обработчик на каждое событие,
 * из-за чего на каждой установке блока выделялся лишний объект, а подменить
 * сервис в тесте было нельзя.
 */
public class SkullPlaceListener implements Listener {

    private final Plugin plugin;
    private final AntiSkullConfig config;
    private final SkullValidationService validationService;
    private final SkullRemovalService removalService;
    private final PermissionService permissionService;
    private final MessageService messageService;

    public SkullPlaceListener(@NotNull Plugin plugin,
                              @NotNull AntiSkullConfig config,
                              @NotNull SkullValidationService validationService,
                              @NotNull SkullRemovalService removalService,
                              @NotNull PermissionService permissionService,
                              @NotNull MessageService messageService) {
        this.plugin = plugin;
        this.config = config;
        this.validationService = validationService;
        this.removalService = removalService;
        this.permissionService = permissionService;
        this.messageService = messageService;
    }

    /**
     * Проверяет установку блока и блокирует запрещенные головы.
     *
     * @param event событие установки блока
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSkullPlace(@NotNull BlockPlaceEvent event) {

        Player player = event.getPlayer();

        if (permissionService.canBypassBlock(player)) {
            return;
        }

        if (validationService.isPlacementAllowed(event)) {
            return;
        }

        boolean takeAway = config.isTakeAwayEnabled() && !permissionService.canBypassTakeAway(player);

        SkullPlaceBlockedEvent blockedEvent = new SkullPlaceBlockedEvent(player,
                event.getBlock(), event.getItemInHand(), takeAway);
        plugin.getServer().getPluginManager().callEvent(blockedEvent);

        if (blockedEvent.isCancelled()) {
            return;
        }

        event.setCancelled(true);
        messageService.send(player, MessageKey.BLOCKED);

        if (blockedEvent.isTakeAway()) {
            removalService.takeAway(player, event.getItemInHand(), event.getHand(),
                    config.getTakeAwayType());
        }

    }

}
