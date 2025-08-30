package com.ney.anti_skull.service;

import com.ney.anti_skull.config.type.TakeAwayType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SkullRemovalService {

    public void removeSkull(ItemStack itemStack,
                            Player player, EquipmentSlot hand,
                            boolean shouldRemoveSkull,
                            TakeAwayType takeAwayType) {

        if (!shouldRemoveSkull || itemStack == null) return;

        switch (takeAwayType) {

            case ALL ->
                removeAllSkullsFromInventory(player, itemStack);

            case HAND -> removeOnlyUsedHand(player, hand);

        }
    }

    private void removeOnlyUsedHand(Player player, EquipmentSlot hand) {

        if (hand == EquipmentSlot.HAND) {
            player.getInventory().setItemInMainHand(null);
        } else if (hand == EquipmentSlot.OFF_HAND) {
            player.getInventory().setItemInOffHand(null);
        }

    }

    private void removeAllSkullsFromInventory(@NotNull Player player,
                                              @NotNull ItemStack itemStack) {

        for (int i = 0; i < player.getInventory().getSize(); i++) {

            ItemStack slotItem = player.getInventory().getItem(i);
            if (slotItem != null && slotItem.isSimilar(itemStack)) {
                player.getInventory().setItem(i, null);
            }

        }
    }
}