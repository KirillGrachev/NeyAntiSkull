package eu.neydev.antiskull.listener;

import eu.neydev.antiskull.NeyAntiSkull;
import eu.neydev.antiskull.config.ConfigManager;
import eu.neydev.antiskull.event.SkullPlaceEvent;
import eu.neydev.antiskull.registry.SkullRegistry;
import eu.neydev.antiskull.service.SkullRemovalService;
import eu.neydev.antiskull.service.SkullValidationService;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.jetbrains.annotations.NotNull;

public class SkullPlaceListener implements Listener {

    private final ConfigManager configManager;
    private final SkullValidationService skullValidationService;
    private final SkullRemovalService skullRemovalService;

    public SkullPlaceListener(@NotNull NeyAntiSkull plugin) {

        this.configManager = plugin.getConfigManager();

        SkullRegistry skullRegistry = new SkullRegistry(configManager);

        this.skullValidationService = new SkullValidationService(
                skullRegistry,
                configManager
        );

        this.skullRemovalService = new SkullRemovalService();

    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSkullPlace(BlockPlaceEvent event) {

        new SkullPlaceEvent(configManager,
                skullValidationService,
                skullRemovalService
        ).onSkullPlace(event);

    }
}