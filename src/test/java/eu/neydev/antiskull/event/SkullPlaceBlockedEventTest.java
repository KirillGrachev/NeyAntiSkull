package eu.neydev.antiskull.event;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Контракты собственного события: синхронность, свой HandlerList, отмена
 * и отдельный переключатель конфискации.
 */
class SkullPlaceBlockedEventTest {

    private final Player player = mock(Player.class);
    private final Block block = mock(Block.class);
    private final ItemStack item = mock(ItemStack.class);

    private SkullPlaceBlockedEvent event(boolean takeAway) {
        return new SkullPlaceBlockedEvent(player, block, item, takeAway);
    }

    @Test
    @DisplayName("Событие синхронное и несет участника, блок и предмет")
    void eventIsSynchronousAndCarriesValues() {

        SkullPlaceBlockedEvent event = event(true);

        assertFalse(event.isAsynchronous(), "установка блока обрабатывается в главном потоке");
        assertSame(player, event.getPlayer());
        assertSame(block, event.getBlock());
        assertSame(item, event.getItem());
        assertTrue(event.isTakeAway());

    }

    @Test
    @DisplayName("Флаг конфискации приходит из конфигурации и прав")
    void takeAwayFlagIsCarriedFromTheCaller() {
        assertFalse(event(false).isTakeAway());
    }

    @Test
    @DisplayName("HandlerList собственный и совпадает со статическим")
    void handlerListIsOwnAndStatic() {

        HandlerList handlers = event(true).getHandlers();

        assertSame(SkullPlaceBlockedEvent.getHandlerList(), handlers);
        assertSame(handlers, event(false).getHandlers());

    }

    @Test
    @DisplayName("Отмена по умолчанию выключена и переключается")
    void cancellationDefaultsToFalse() {

        SkullPlaceBlockedEvent event = event(true);

        assertFalse(event.isCancelled());

        event.setCancelled(true);

        assertTrue(event.isCancelled());

    }

    @Test
    @DisplayName("Конфискацию можно отключить, не снимая запрет")
    void takeAwayCanBeVetoedAlone() {

        SkullPlaceBlockedEvent event = event(true);

        event.setTakeAway(false);

        assertFalse(event.isTakeAway());
        assertFalse(event.isCancelled(), "запрет остается в силе");

    }

}
