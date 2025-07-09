package com.ney.anti_skull.service;

import org.bukkit.inventory.ItemStack;

public class SkullRemovalService {

    public void removeSkull(ItemStack itemStack,
                            boolean shouldRemoveSkull) {
        if (shouldRemoveSkull && itemStack != null) {
            itemStack.setAmount(0);
        }
    }
}