package com.ney.anti_skull.service;

import com.ney.anti_skull.registry.SkullRegistry;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

public class SkullValidationService {

    private final SkullRegistry skullRegistry;

    public SkullValidationService(SkullRegistry skullRegistry) {
        this.skullRegistry = skullRegistry;
    }

    public boolean isSkullPlacementValid(@NotNull BlockPlaceEvent event) {

        ItemStack itemStack = event.getItemInHand();
        if (itemStack == null || !itemStack.hasItemMeta()) {
            return true;
        }

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (!(itemMeta instanceof SkullMeta)) {
            return true;
        }

        BlockState blockState = event.getBlock().getState();
        if (!(blockState instanceof Skull)) {
            return true;
        }

        SkullMeta skullMeta = (SkullMeta) itemMeta;
        String displayName = skullMeta.getDisplayName();

        if (displayName == null || displayName.isEmpty()) {
            return true;
        }

        return !skullRegistry.isSkullRegistered(displayName);

    }
}