package com.ney.anti_skull.event;

import com.ney.anti_skull.config.ConfigManager;
import com.ney.anti_skull.service.SkullRemovalService;
import com.ney.anti_skull.service.SkullValidationService;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
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
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (!configManager.isSkullBlockingEnabled()) {
            event.setCancelled(true);
            return;
        }

        BlockState blockState = event.getBlock().getState();
        if (!(blockState instanceof Skull)) {
            return;
        }

        if (!validationService.isSkullPlacementValid(event)) {
            event.setCancelled(true);

            if (configManager.areMessagesEnabled()) {
                configManager.getBlockedMessage().forEach(player::sendMessage);
            }

            removalService.removeSkull(itemInHand, player, configManager.shouldRemoveSkull());
        }
    }
}