package eu.neydev.antiskull.service;

import eu.neydev.antiskull.config.type.TakeAwayType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Конфискация головы после заблокированной установки.
 *
 * Забирается весь стек, а не один предмет: игрок пытался поставить
 * запрещенную голову, и оставить ему остальные 63 штуки - значит пригласить
 * повторить. Поведение 1.x сохранено намеренно, режимы различаются только
 * охватом (рука или весь инвентарь).
 *
 * Метод возвращает количество изъятых предметов: вызывающей стороне
 * (событию, диагностике, тестам) нужен факт, а не догадка по пустому слоту.
 */
public class SkullRemovalService {

    /**
     * Забирает предмет у игрока по настроенному режиму.
     *
     * @param player игрок, ставивший блок
     * @param item   предмет, которым ставился блок
     * @param hand   рука, использованная для установки
     * @param type   режим конфискации из конфигурации
     * @return количество изъятых предметов
     */
    public int takeAway(@NotNull Player player,
                        @NotNull ItemStack item,
                        @NotNull EquipmentSlot hand,
                        @NotNull TakeAwayType type) {
        return type == TakeAwayType.ALL
                ? takeAwayMatching(player, item)
                : takeAwayHand(player, hand);
    }

    /**
     * Забирает стек из руки, которой ставился блок.
     *
     * Неожиданный слот (не HAND и не OFF_HAND) не трогается вовсе: лучше
     * не изъять предмет, чем снять с игрока ботинки из-за чужого события.
     */
    private int takeAwayHand(@NotNull Player player, @NotNull EquipmentSlot hand) {

        PlayerInventory inventory = player.getInventory();

        if (hand == EquipmentSlot.OFF_HAND) {

            int amount = amountOf(inventory.getItemInOffHand());
            inventory.setItemInOffHand(null);
            return amount;

        }

        if (hand != EquipmentSlot.HAND) {
            return 0;
        }

        int amount = amountOf(inventory.getItemInMainHand());
        inventory.setItemInMainHand(null);
        return amount;

    }

    private int takeAwayMatching(@NotNull Player player, @NotNull ItemStack item) {

        PlayerInventory inventory = player.getInventory();
        int removed = 0;

        for (int slot = 0; slot < inventory.getSize(); slot++) {

            ItemStack slotItem = inventory.getItem(slot);

            if (slotItem != null && slotItem.isSimilar(item)) {

                removed += slotItem.getAmount();
                inventory.setItem(slot, null);

            }

        }

        return removed;

    }

    private int amountOf(@Nullable ItemStack item) {
        return item == null ? 0 : item.getAmount();
    }

}
