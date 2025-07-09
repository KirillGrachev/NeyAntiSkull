package com.ney.anti_skull.service;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SkullRemovalService {

    public void removeSkull(ItemStack itemStack, Player player,
                            boolean shouldRemoveSkull) {

        if (!shouldRemoveSkull || itemStack == null) return;

        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack slotItem = player.getInventory().getItem(i);
            if (slotItem != null && slotItem.isSimilar(itemStack)) {
                player.getInventory().setItem(i, null);
            }
        }

        if (player.getInventory().getItemInOffHand().isSimilar(itemStack)) {
            player.getInventory().setItemInOffHand(null);
        }
    }
}