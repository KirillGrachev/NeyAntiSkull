package com.ney.anti_skull.service;

import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Сервис для удаления голов из инвентаря игрока.
 */
public class SkullRemovalService {

    /**
     * Удаляет голову в соответствии с настройками.
     *
     * @param itemStack      предмет головы
     * @param player         игрок
     * @param hand           рука, в которой была голова
     * @param shouldRemoveSkull флаг необходимости удаления
     * @param removalType    тип удаления ("HAND" или "ALL")
     */
    public void removeSkull(ItemStack itemStack,
                            Player player, EquipmentSlot hand,
                            boolean shouldRemoveSkull,
                            String removalType) {

        if (!shouldRemoveSkull || itemStack == null) return;

        switch (removalType.toUpperCase()) {

            case "ALL" ->
                removeAllSkullsFromInventory(player, itemStack);

            case "HAND" -> removeOnlyUsedHand(player, hand);

        }
    }

    /**
     * Удаляет голову только из используемой руки.
     */
    private void removeOnlyUsedHand(Player player, EquipmentSlot hand) {

        if (hand == EquipmentSlot.HAND) {
            player.getInventory().setItemInMainHand(null);
        } else if (hand == EquipmentSlot.OFF_HAND) {
            player.getInventory().setItemInOffHand(null);
        }

    }

    /**
     * Удаляет все запрещенные головы из инвентаря.
     */
    private void removeAllSkullsFromInventory(@NotNull Player player,
                                              ItemStack itemStack) {

        for (int i = 0; i < player.getInventory().getSize(); i++) {

            ItemStack slotItem = player.getInventory().getItem(i);
            if (slotItem != null && slotItem.isSimilar(itemStack)) {
                player.getInventory().setItem(i, null);
            }

        }

    }
}