package eu.neydev.antiskull.listener;

import eu.neydev.antiskull.config.type.MessageKey;
import eu.neydev.antiskull.config.type.PermissionNode;
import eu.neydev.antiskull.config.type.TakeAwayType;
import eu.neydev.antiskull.event.SkullPlaceBlockedEvent;
import eu.neydev.antiskull.registry.SkullRegistry;
import eu.neydev.antiskull.service.MessageService;
import eu.neydev.antiskull.service.PermissionService;
import eu.neydev.antiskull.service.SkullRemovalService;
import eu.neydev.antiskull.service.SkullValidationService;
import eu.neydev.antiskull.support.StubConfig;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Слушатель на настоящем BlockPlaceEvent: блокировка, сообщение,
 * конфискация, обходы правом и взаимодействие с собственным событием.
 */
class SkullPlaceListenerTest {

    private final Plugin plugin = mock(Plugin.class);
    private final Server server = mock(Server.class);
    private final PluginManager pluginManager = mock(PluginManager.class);
    private final Player player = mock(Player.class);
    private final PlayerInventory inventory = mock(PlayerInventory.class);
    private final Block block = mock(Block.class);
    private final Block placedAgainst = mock(Block.class);
    private final Skull skullState = mock(Skull.class);
    private final ItemStack head = mock(ItemStack.class);
    private final SkullMeta meta = mock(SkullMeta.class);

    private StubConfig config;
    private SkullPlaceListener listener;

    @BeforeEach
    void setUp() {

        config = new StubConfig()
                .withNames("§aPlayer Head")
                .withMessage(MessageKey.BLOCKED, "blocked!");

        SkullRegistry registry = new SkullRegistry(config);

        when(plugin.getServer()).thenReturn(server);
        when(server.getPluginManager()).thenReturn(pluginManager);
        when(player.isOnline()).thenReturn(true);
        when(player.getInventory()).thenReturn(inventory);
        when(inventory.getItemInMainHand()).thenReturn(head);
        when(head.getAmount()).thenReturn(7);
        when(head.getType()).thenReturn(org.bukkit.Material.PLAYER_HEAD);
        when(head.getItemMeta()).thenReturn(meta);
        when(meta.getDisplayName()).thenReturn("§aPlayer Head");
        when(block.getState()).thenReturn(skullState);

        listener = new SkullPlaceListener(plugin, config,
                new SkullValidationService(config, registry),
                new SkullRemovalService(),
                new PermissionService(config),
                new MessageService(config));

    }

    private BlockPlaceEvent place(EquipmentSlot hand) {
        return new BlockPlaceEvent(block, skullState, placedAgainst, head, player, true, hand);
    }

    @Test
    @DisplayName("Запрещенная голова: установка отменена, сообщение и конфискация")
    void blockedHeadIsCancelledMessagedAndTaken() {

        BlockPlaceEvent event = place(EquipmentSlot.HAND);

        listener.onSkullPlace(event);

        assertTrue(event.isCancelled());
        verify(player).sendMessage("blocked!");
        verify(inventory).setItemInMainHand(null);

    }

    @Test
    @DisplayName("Разрешенная голова не трогается, событие не публикуется")
    void allowedHeadIsUntouched() {

        when(meta.getDisplayName()).thenReturn("Other");
        BlockPlaceEvent event = place(EquipmentSlot.HAND);

        listener.onSkullPlace(event);

        assertFalse(event.isCancelled());
        verify(player, never()).sendMessage(org.mockito.ArgumentMatchers.anyString());
        verify(pluginManager, never()).callEvent(any());

    }

    @Test
    @DisplayName("Право place отменяет блокировку до проверки имени")
    void placeBypassSkipsValidationEntirely() {

        config.withPermissionsEnabled(true);
        when(player.hasPermission(config.getPermission(PermissionNode.PLACE))).thenReturn(true);

        BlockPlaceEvent event = place(EquipmentSlot.HAND);

        listener.onSkullPlace(event);

        assertFalse(event.isCancelled());
        verify(block, never()).getState();
        verify(pluginManager, never()).callEvent(any());

    }

    @Test
    @DisplayName("Выключенная конфискация сохраняет предмет")
    void disabledTakeAwayKeepsTheItem() {

        config.withTakeAwayEnabled(false);

        BlockPlaceEvent event = place(EquipmentSlot.HAND);

        listener.onSkullPlace(event);

        assertTrue(event.isCancelled());
        verify(inventory, never()).setItemInMainHand(null);

    }

    @Test
    @DisplayName("Право take_away сохраняет предмет при действующем запрете")
    void takeAwayBypassKeepsTheItem() {

        config.withPermissionsEnabled(true);
        when(player.hasPermission(config.getPermission(PermissionNode.TAKE_AWAY))).thenReturn(true);

        BlockPlaceEvent event = place(EquipmentSlot.HAND);

        listener.onSkullPlace(event);

        assertTrue(event.isCancelled());
        verify(inventory, never()).setItemInMainHand(null);

    }

    @Test
    @DisplayName("Режим ALL забирает совпадающие стеки из инвентаря")
    void allModeUsesConfiguredType() {

        config.withTakeAwayType(TakeAwayType.ALL);
        when(inventory.getSize()).thenReturn(1);
        when(inventory.getItem(0)).thenReturn(head);
        when(head.isSimilar(head)).thenReturn(true);

        listener.onSkullPlace(place(EquipmentSlot.HAND));

        verify(inventory).setItem(0, null);
        verify(inventory, never()).setItemInMainHand(null);

    }

    @Test
    @DisplayName("Отмена собственного события пропускает установку")
    void cancelledBlockedEventLetsPlacementThrough() {

        doAnswer(invocation -> {
            invocation.getArgument(0, SkullPlaceBlockedEvent.class).setCancelled(true);
            return null;
        }).when(pluginManager).callEvent(any(SkullPlaceBlockedEvent.class));

        BlockPlaceEvent event = place(EquipmentSlot.HAND);

        listener.onSkullPlace(event);

        assertFalse(event.isCancelled());
        verify(player, never()).sendMessage(org.mockito.ArgumentMatchers.anyString());
        verify(inventory, never()).setItemInMainHand(null);

    }

    @Test
    @DisplayName("Собственное событие может отключить одну конфискацию")
    void blockedEventCanVetoTakeAway() {

        doAnswer(invocation -> {
            invocation.getArgument(0, SkullPlaceBlockedEvent.class).setTakeAway(false);
            return null;
        }).when(pluginManager).callEvent(any(SkullPlaceBlockedEvent.class));

        BlockPlaceEvent event = place(EquipmentSlot.HAND);

        listener.onSkullPlace(event);

        assertTrue(event.isCancelled(), "запрет остается в силе");
        verify(player).sendMessage("blocked!");
        verify(inventory, never()).setItemInMainHand(null);

    }

    @Test
    @DisplayName("Собственное событие несет игрока, блок, предмет и флаг")
    void blockedEventCarriesTheWholeContext() {

        listener.onSkullPlace(place(EquipmentSlot.OFF_HAND));

        ArgumentCaptor<SkullPlaceBlockedEvent> captor =
                ArgumentCaptor.forClass(SkullPlaceBlockedEvent.class);

        verify(pluginManager).callEvent(captor.capture());

        SkullPlaceBlockedEvent fired = captor.getValue();

        assertSame(player, fired.getPlayer());
        assertSame(block, fired.getBlock());
        assertSame(head, fired.getItem());
        assertTrue(fired.isTakeAway());

    }

    @Test
    @DisplayName("Выключенный плагин не блокирует установку")
    void disabledPluginLeavesEventAlone() {

        config.withPluginEnabled(false);
        BlockPlaceEvent event = place(EquipmentSlot.HAND);

        listener.onSkullPlace(event);

        assertFalse(event.isCancelled());
        verify(pluginManager, never()).callEvent(any());

    }

    @Test
    @DisplayName("Выключенные сообщения не отменяют блокировку")
    void disabledMessagesStillBlock() {

        config.withMessagesEnabled(false);
        BlockPlaceEvent event = place(EquipmentSlot.HAND);

        listener.onSkullPlace(event);

        assertTrue(event.isCancelled());
        verify(player, never()).sendMessage(org.mockito.ArgumentMatchers.anyString());
        verify(inventory).setItemInMainHand(null);

    }

    @Test
    @DisplayName("Установка из оффханда конфискует оффханд")
    void offHandPlacementTakesOffHand() {

        when(inventory.getItemInOffHand()).thenReturn(head);

        listener.onSkullPlace(place(EquipmentSlot.OFF_HAND));

        verify(inventory).setItemInOffHand(null);
        verify(inventory, never()).setItemInMainHand(null);

    }

    @Test
    @DisplayName("Пустая рука не мешает блокировке установки")
    void emptyHandDoesNotPreventCancellation() {

        when(inventory.getItemInMainHand()).thenReturn(null);
        BlockPlaceEvent event = place(EquipmentSlot.HAND);

        listener.onSkullPlace(event);

        assertTrue(event.isCancelled());
        verify(player).sendMessage("blocked!");
        verify(inventory).setItemInMainHand(null);

    }

}
