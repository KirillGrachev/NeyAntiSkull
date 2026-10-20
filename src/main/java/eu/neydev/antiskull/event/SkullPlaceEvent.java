package eu.neydev.antiskull.event;

import eu.neydev.antiskull.config.AntiSkullConfig;
import eu.neydev.antiskull.config.type.MessageKey;
import eu.neydev.antiskull.config.type.PermissionNode;
import eu.neydev.antiskull.service.SkullRemovalService;
import eu.neydev.antiskull.service.SkullValidationService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Обработчик блокировки установки головы.
 */
public class SkullPlaceEvent implements Listener {

    private final AntiSkullConfig config;
    private final SkullValidationService validationService;
    private final SkullRemovalService removalService;

    public SkullPlaceEvent(@NotNull AntiSkullConfig config,
                           @NotNull SkullValidationService validationService,
                           @NotNull SkullRemovalService removalService) {
        this.config = config;
        this.validationService = validationService;
        this.removalService = removalService;
    }

    /**
     * Блокирует установку головы из списка.
     *
     * @param event событие установки блока
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSkullPlace(@NotNull BlockPlaceEvent event) {

        Player player = event.getPlayer();

        if (config.arePermissionsEnabled()
                && player.hasPermission(config.getPermission(PermissionNode.PLACE))) {
            return;
        }

        if (validationService.isPlacementAllowed(event)) {
            return;
        }

        event.setCancelled(true);

        if (config.areMessagesEnabled()) {
            for (String line : config.getMessages(MessageKey.BLOCKED)) {
                player.sendMessage(line);
            }
        }

        boolean takeAway = config.isTakeAwayEnabled()
                && !(config.arePermissionsEnabled()
                        && player.hasPermission(config.getPermission(PermissionNode.TAKE_AWAY)));

        removalService.removeSkull(event.getItemInHand(), player, event.getHand(), takeAway,
                config.getTakeAwayType());

    }

}
