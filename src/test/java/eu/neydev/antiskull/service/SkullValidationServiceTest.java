package eu.neydev.antiskull.service;

import eu.neydev.antiskull.registry.SkullRegistry;
import eu.neydev.antiskull.support.StubConfig;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Правила блокировки: порядок проверок, выключенный список как тотальный
 * запрет и поведение на головах без имени.
 */
class SkullValidationServiceTest {

    private final Block block = mock(Block.class);
    private final Block placedAgainst = mock(Block.class);
    private final Skull skullState = mock(Skull.class);
    private final Player player = mock(Player.class);
    private final ItemStack head = mock(ItemStack.class);
    private final SkullMeta meta = mock(SkullMeta.class);

    private StubConfig config;
    private SkullValidationService service;

    @BeforeEach
    void setUp() {

        config = new StubConfig().withNames("§aPlayer Head");
        SkullRegistry registry = new SkullRegistry(config);
        service = new SkullValidationService(config, registry);

        when(block.getState()).thenReturn(skullState);
        when(head.getType()).thenReturn(Material.PLAYER_HEAD);
        when(head.getItemMeta()).thenReturn(meta);
        when(meta.getDisplayName()).thenReturn("§aPlayer Head");

    }

    private BlockPlaceEvent place() {
        return new BlockPlaceEvent(block, skullState, placedAgainst, head, player, true, EquipmentSlot.HAND);
    }

    @Test
    @DisplayName("Голова из списка блокируется")
    void listedHeadIsBlocked() {
        assertFalse(service.isPlacementAllowed(place()));
    }

    @Test
    @DisplayName("Голова не из списка проходит")
    void unlistedHeadIsAllowed() {

        when(meta.getDisplayName()).thenReturn("§cOther Head");

        assertTrue(service.isPlacementAllowed(place()));

    }

    @Test
    @DisplayName("Безымянная голова проходит: пустое имя не в списке")
    void unnamedHeadIsAllowed() {

        when(meta.getDisplayName()).thenReturn("");

        assertTrue(service.isPlacementAllowed(place()));

    }

    @Test
    @DisplayName("Предмет без мета-данных проходит")
    void itemWithoutMetaIsAllowed() {

        when(head.getItemMeta()).thenReturn(null);

        assertTrue(service.isPlacementAllowed(place()));

    }

    @Test
    @DisplayName("Мета-данные не головы не читаются как имя")
    void plainItemMetaIsAllowed() {

        ItemMeta plainMeta = mock(ItemMeta.class);

        when(head.getItemMeta()).thenReturn(plainMeta);

        assertTrue(service.isPlacementAllowed(place()));
        verify(plainMeta, never()).getDisplayName();

    }

    @Test
    @DisplayName("Не голова в руке пропускается до проверки блока")
    void nonSkullItemIsAllowedBeforeBlockCheck() {

        when(head.getType()).thenReturn(Material.STONE);

        assertTrue(service.isPlacementAllowed(place()));
        verify(block, never()).getState();

    }

    @Test
    @DisplayName("Голова, не ставшая блоком-головой, пропускается")
    void nonSkullBlockIsAllowed() {

        when(block.getState()).thenReturn(mock(BlockState.class));

        assertTrue(service.isPlacementAllowed(place()));

    }

    @Test
    @DisplayName("Выключенный плагин не блокирует ничего")
    void disabledPluginAllowsEverything() {

        config.withPluginEnabled(false);

        assertTrue(service.isPlacementAllowed(place()));
        verify(head, never()).getItemMeta();

    }

    @Test
    @DisplayName("Выключенный список блокирует любую голову")
    void disabledListBlocksEverySkull() {

        config.withListEnabled(false);
        when(meta.getDisplayName()).thenReturn("Whatever");

        assertFalse(service.isPlacementAllowed(place()),
                "settings.list.enabled: false - это тотальный запрет, а не разрешение");

    }

    @Test
    @DisplayName("Выключенный список не распространяется на другие блоки")
    void disabledListStillAllowsOtherBlocks() {

        config.withListEnabled(false);
        when(head.getType()).thenReturn(Material.STONE);

        assertTrue(service.isPlacementAllowed(place()));

    }

    @Test
    @DisplayName("Регистр имени учитывается настройкой списка")
    void caseSensitivityComesFromConfig() {

        config.withCaseSensitive(true);
        when(meta.getDisplayName()).thenReturn("§aPLAYER HEAD");

        assertTrue(service.isPlacementAllowed(place()), "точное сравнение не находит имя");

    }

}
