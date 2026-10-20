package eu.neydev.antiskull.listener;

import eu.neydev.antiskull.NeyAntiSkull;
import eu.neydev.antiskull.config.AntiSkullConfig;
import eu.neydev.antiskull.event.SkullPlaceEvent;
import eu.neydev.antiskull.registry.SkullRegistry;
import eu.neydev.antiskull.service.SkullRemovalService;
import eu.neydev.antiskull.service.SkullValidationService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Слушатель установки блоков.
 */
public class SkullPlaceListener implements Listener {

    private final AntiSkullConfig config;
    private final SkullValidationService skullValidationService;
    private final SkullRemovalService skullRemovalService;

    public SkullPlaceListener(@NotNull NeyAntiSkull plugin) {

        this.config = plugin.getConfigManager();

        SkullRegistry skullRegistry = new SkullRegistry(config);

        this.skullValidationService = new SkullValidationService(config, skullRegistry);
        this.skullRemovalService = new SkullRemovalService();

    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSkullPlace(@NotNull BlockPlaceEvent event) {

        new SkullPlaceEvent(config, skullValidationService, skullRemovalService)
                .onSkullPlace(event);

    }

}
