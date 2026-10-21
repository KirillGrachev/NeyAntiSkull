package eu.neydev.antiskull.service;

import eu.neydev.antiskull.config.type.TakeAwayType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Конфискация предмета: режим руки, режим всего инвентаря и поведение на
 * неожиданном слоте.
 */
class SkullRemovalServiceTest {

    private final Player player = mock(Player.class);
    private final PlayerInventory inventory = mock(PlayerInventory.class);
    private final SkullRemovalService service = new SkullRemovalService();

    private ItemStack head;

    @BeforeEach
    void setUp() {

        when(player.getInventory()).thenReturn(inventory);

        head = mock(ItemStack.class);
        when(head.getType()).thenReturn(Material.PLAYER_HEAD);
        when(head.getAmount()).thenReturn(7);

    }

    @Test
    @DisplayName("HAND очищает основную руку и возвращает размер стака")
    void handModeClearsMainHand() {

        when(inventory.getItemInMainHand()).thenReturn(head);

        int removed = service.takeAway(player, head, EquipmentSlot.HAND, TakeAwayType.HAND);

        assertEquals(7, removed);
        verify(inventory).setItemInMainHand(null);
        verify(inventory, never()).setItemInOffHand(null);

    }

    @Test
    @DisplayName("HAND для оффханда очищает оффханд")
    void handModeClearsOffHand() {

        when(inventory.getItemInOffHand()).thenReturn(head);

        int removed = service.takeAway(player, head, EquipmentSlot.OFF_HAND, TakeAwayType.HAND);

        assertEquals(7, removed);
        verify(inventory).setItemInOffHand(null);
        verify(inventory, never()).setItemInMainHand(null);

    }

    @Test
    @DisplayName("Неожиданный слот не трогается вовсе")
    void unexpectedSlotIsUntouched() {

        int removed = service.takeAway(player, head, EquipmentSlot.FEET, TakeAwayType.HAND);

        assertEquals(0, removed, "лучше не изъять предмет, чем снять ботинки");
        verify(inventory, never()).setItemInMainHand(null);
        verify(inventory, never()).setItemInOffHand(null);

    }

    @Test
    @DisplayName("Пустая рука дает ноль изъятых предметов")
    void emptyHandRemovesNothing() {

        when(inventory.getItemInMainHand()).thenReturn(null);

        assertEquals(0, service.takeAway(player, head, EquipmentSlot.HAND, TakeAwayType.HAND));

    }

    @Test
    @DisplayName("ALL забирает все такие же стеки и считает предметы")
    void allModeRemovesEveryMatchingStack() {

        ItemStack twin = mock(ItemStack.class);
        ItemStack other = mock(ItemStack.class);

        when(twin.getAmount()).thenReturn(3);
        // isSimilar вызывается на предмете из слота с предметом события в аргументе
        when(head.isSimilar(head)).thenReturn(true);
        when(twin.isSimilar(head)).thenReturn(true);
        when(other.isSimilar(head)).thenReturn(false);
        when(inventory.getSize()).thenReturn(3);
        when(inventory.getItem(0)).thenReturn(head);
        when(inventory.getItem(1)).thenReturn(other);
        when(inventory.getItem(2)).thenReturn(twin);

        int removed = service.takeAway(player, head, EquipmentSlot.HAND, TakeAwayType.ALL);

        assertEquals(10, removed);
        verify(inventory).setItem(0, null);
        verify(inventory).setItem(2, null);
        verify(inventory, never()).setItem(1, null);

    }

    @Test
    @DisplayName("ALL на пустом инвентаре ничего не ломает")
    void allModeOnEmptyInventory() {

        when(inventory.getSize()).thenReturn(2);
        when(inventory.getItem(anyInt())).thenReturn(null);

        assertEquals(0, service.takeAway(player, head, EquipmentSlot.HAND, TakeAwayType.ALL));
        verify(inventory, never()).setItem(anyInt(), org.mockito.ArgumentMatchers.isNull());

    }

}
