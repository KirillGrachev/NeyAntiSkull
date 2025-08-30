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

public class SkullValidationService {

    private final SkullRegistry skullRegistry;
    private final ConfigManager configManager;

    public SkullValidationService(SkullRegistry skullRegistry,
                                  ConfigManager configManager) {
        this.skullRegistry = skullRegistry;
        this.configManager = configManager;
    }

    public boolean isSkullPlacementValid(BlockPlaceEvent event) {

        if (!configManager.isSkullBlockingEnabled()) return true;
        if (!isValidSkullItem(event)) return true;
        if (!isSkullBlock(event)) return true;
        if (!configManager.isListEnabled()) return false;

        String displayName = getDisplayName(event);
        return displayName == null || displayName.isEmpty()
                || !skullRegistry.isSkullRegistered(displayName);

    }

    private boolean isValidSkullItem(@NotNull BlockPlaceEvent event) {
        ItemStack itemStack = event.getItemInHand();
        return itemStack.getType() == Material.PLAYER_HEAD;
    }

    private boolean isSkullBlock(@NotNull BlockPlaceEvent event) {
        BlockState blockState = event.getBlock().getState();
        return blockState instanceof Skull;
    }

    private @Nullable String getDisplayName(@NotNull BlockPlaceEvent event) {

        ItemMeta itemMeta = event.getItemInHand().getItemMeta();

        if (itemMeta instanceof SkullMeta) {
            return itemMeta.getDisplayName();
        }

        return null;

    }
}