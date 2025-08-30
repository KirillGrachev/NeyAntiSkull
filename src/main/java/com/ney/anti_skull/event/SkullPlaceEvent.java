package com.ney.anti_skull.event;

import com.ney.anti_skull.config.ConfigManager;
import com.ney.anti_skull.service.SkullRemovalService;
import com.ney.anti_skull.service.SkullValidationService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SkullPlaceEvent implements Listener {

    private final ConfigManager configManager;
    private final SkullValidationService validationService;
    private final SkullRemovalService removalService;

    public SkullPlaceEvent(ConfigManager configManager,
                           SkullValidationService validationService,
                           SkullRemovalService removalService) {
        this.configManager = configManager;
        this.validationService = validationService;
        this.removalService = removalService;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSkullPlace(@NotNull BlockPlaceEvent event) {

        Player player = event.getPlayer();

        if (configManager.arePermissionsEnabled()) {
            if (player.hasPermission("neyantiskull.bypass.place")) {
                return;
            }
        }

        if (!validationService.isSkullPlacementValid(event)) {

            event.setCancelled(true);

            if (configManager.areMessagesEnabled()) {
                configManager.getBlockedMessage().forEach(player::sendMessage);
            }

            boolean shouldRemove = configManager.shouldRemoveSkull();
            if (configManager.arePermissionsEnabled()) {
                shouldRemove = shouldRemove && !player.hasPermission("neyantiskull.bypass.takeaway");
            }

            removalService.removeSkull(
                    event.getItemInHand(), player,
                    event.getHand(), shouldRemove,
                    configManager.getTakeAwayType()
            );
        }
    }
}