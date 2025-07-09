package com.ney.anti_skull.service;

import com.ney.anti_skull.config.ConfigManager;
import com.ney.anti_skull.registry.SkullRegistry;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Сервис валидации голов при установке.
 * Проверяет, разрешено ли игроку устанавливать данную голову.
 */
public class SkullValidationService {

    private final SkullRegistry skullRegistry;

    public SkullValidationService(SkullRegistry skullRegistry) {
        this.skullRegistry = skullRegistry;
    }

    /**
     * Проверяет валидность установки головы.
     *
     * @param event событие установки блока
     * @return true если установка разрешена, false если запрещена
     */
    public boolean isSkullPlacementValid(@NotNull BlockPlaceEvent event,
                                         ConfigManager configManager) {

        if (!isValidSkullItem(event)) return true;
        if (!isSkullBlock(event)) return true;
        if (!configManager.isListEnabled()) return false;

        String displayName = getDisplayName(event);
        if (displayName == null || displayName.isEmpty())
            return true;

        return !skullRegistry.isSkullRegistered(displayName);

    }

    /**
     * Проверяет, является ли предмет головой игрока.
     */
    private boolean isValidSkullItem(@NotNull BlockPlaceEvent event) {

        ItemStack itemStack = event.getItemInHand();
        ItemMeta itemMeta = itemStack.getItemMeta();

        return itemStack.getType() == Material.PLAYER_HEAD
                || itemMeta instanceof SkullMeta;

    }

    /**
     * Проверяет, что установленный блок — это голова.
     */
    private boolean isSkullBlock(@NotNull BlockPlaceEvent event) {
        BlockState blockState = event.getBlock().getState();
        return blockState instanceof Skull;
    }

    /**
     * Извлекает отображаемое имя головы.
     */
    private @Nullable String getDisplayName(@NotNull BlockPlaceEvent event) {

        ItemMeta itemMeta = event.getItemInHand().getItemMeta();
        if (itemMeta instanceof SkullMeta) {
            return itemMeta.getDisplayName();
        }

        return null;

    }
}