package com.ney.anti_skull.listener;

import com.ney.anti_skull.NeyAntiSkull;
import com.ney.anti_skull.config.ConfigManager;
import com.ney.anti_skull.event.SkullPlaceEvent;
import com.ney.anti_skull.registry.SkullRegistry;
import com.ney.anti_skull.service.SkullRemovalService;
import com.ney.anti_skull.service.SkullValidationService;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Слушатель событий установки блоков.
 * Перехватывает попытки установки черепов и передаёт их на обработку.
 */
public class SkullPlaceListener implements Listener {

    private final ConfigManager configManager;
    private final SkullValidationService skullValidationService;
    private final SkullRemovalService skullRemovalService;

    public SkullPlaceListener(@NotNull NeyAntiSkull plugin) {
        this.configManager = plugin.getConfigManager();
        this.skullValidationService = new SkullValidationService(
                new SkullRegistry(plugin.getConfigManager())
        );
        this.skullRemovalService =  new SkullRemovalService();
    }

    /**
     * Обрабатывает событие установки блока.
     * Создаёт и вызывает обработчик для голов.
     *
     * @param event событие установки блока
     *
     * Особенности:
     * - Высший приоритет обработки (HIGHEST)
     * - Игнорирует отменённые события
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSkullPlace(BlockPlaceEvent event) {
        new SkullPlaceEvent(configManager,
                skullValidationService,
                skullRemovalService
        ).onSkullPlace(event);
    }
}