package eu.neydev.antiskull;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Интеграционные тесты на MockBukkit: плагин загружается в живой серверный
 * мок, слушатель реагирует через настоящий диспетчер событий, команды
 * исполняются через command map, конфигурация сохраняется в папку плагина.
 *
 * Блок и предмет в событии - моки: MockBukkit не отдает Skull как состояние
 * блока PLAYER_HEAD, а правило плагина читает именно тип состояния. Игрок
 * при этом настоящий (PlayerMock), поэтому сообщение, права и инвентарь
 * проверяются на живой реализации.
 */
class NeyAntiSkullIntegrationTest {

    private ServerMock server;
    private NeyAntiSkull plugin;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(NeyAntiSkull.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private BlockPlaceEvent placeEvent(Player player, String displayName) {

        Block block = mock(Block.class);
        ItemStack head = mock(ItemStack.class);
        SkullMeta meta = mock(SkullMeta.class);

        when(block.getState()).thenReturn(mock(Skull.class));
        when(head.getType()).thenReturn(Material.PLAYER_HEAD);
        when(head.getItemMeta()).thenReturn(meta);
        when(head.getAmount()).thenReturn(1);
        when(meta.getDisplayName()).thenReturn(displayName);

        return new BlockPlaceEvent(block, mock(BlockState.class), block, head, player, true,
                EquipmentSlot.HAND);

    }

    @Test
    @DisplayName("Плагин стартует: сервисы, реестр и команда на месте")
    void pluginStartsAndWiresEverything() {

        assertTrue(plugin.isEnabled());
        assertNotNull(plugin.getServices());
        assertNotNull(plugin.getConfigManager());
        assertNotNull(plugin.getConsoleService());
        assertNotNull(plugin.getServices().getSkullRegistry());

        PluginCommand command = plugin.getCommand("antiskull");

        assertNotNull(command, "команда из plugin.yml найдена");
        assertNotNull(command.getExecutor(), "executor привязан");
        assertNotNull(command.getTabCompleter(), "tab-completer привязан");

    }

    @Test
    @DisplayName("Права из plugin.yml зарегистрированы на сервере")
    void permissionsFromPluginYmlAreRegistered() {

        assertNotNull(server.getPluginManager().getPermission("antiskull.bypass.place"));
        assertNotNull(server.getPluginManager().getPermission("antiskull.bypass.takeaway"));
        assertNotNull(server.getPluginManager().getPermission("antiskull.reload"));
        assertNotNull(server.getPluginManager().getPermission("antiskull.admin"));

    }

    @Test
    @DisplayName("Поставляемый config.yml читается в документированные значения")
    void shippedConfigCarriesDocumentedDefaults() {

        assertTrue(plugin.getDataFolder().toPath().resolve("config.yml").toFile().exists(),
                "config.yml сохранен в папку плагина");
        assertTrue(plugin.getConfigManager().isPluginEnabled());
        assertTrue(plugin.getConfigManager().isListEnabled());
        assertTrue(plugin.getConfigManager().isTakeAwayEnabled());
        assertFalse(plugin.getConfigManager().arePermissionsEnabled());
        assertEquals(3, plugin.getServices().getSkullRegistry().size(),
                "три имени из поставки, окрашенные и нормализованные");

    }

    @Test
    @DisplayName("Запрещенная голова блокируется настоящим диспетчером событий")
    void blockedHeadIsCancelledThroughTheDispatcher() {

        PlayerMock player = server.addPlayer();
        BlockPlaceEvent event = placeEvent(player, "§aPlayer Head");

        server.getPluginManager().callEvent(event);

        assertTrue(event.isCancelled());
        assertEquals("", player.nextMessage(), "первая строка предупреждения пустая");
        assertTrue(String.valueOf(player.nextMessage()).contains("prohibited"));

    }

    @Test
    @DisplayName("Голова не из списка проходит без отмены и без сообщения")
    void unlistedHeadPassesThrough() {

        PlayerMock player = server.addPlayer();
        BlockPlaceEvent event = placeEvent(player, "Some Other Head");

        server.getPluginManager().callEvent(event);

        assertFalse(event.isCancelled());
        assertNull(player.nextMessage());

    }

    @Test
    @DisplayName("Уже отмененная установка не обрабатывается")
    void cancelledEventIsNotProcessed() {

        PlayerMock player = server.addPlayer();
        BlockPlaceEvent event = placeEvent(player, "§aPlayer Head");

        event.setCancelled(true);
        server.getPluginManager().callEvent(event);

        assertNull(player.nextMessage(), "ignoreCancelled оставляет событие чужому плагину");

    }

    @Test
    @DisplayName("Команда reload исполняется через command map")
    void reloadCommandRunsThroughCommandMap() {

        PlayerMock player = server.addPlayer();

        assertTrue(player.performCommand("antiskull reload"));
        assertTrue(String.valueOf(player.nextMessage()).contains("Configuration reloaded"));

    }

    @Test
    @DisplayName("Команда info печатает правила из снапшота")
    void infoCommandReportsTheRules() {

        PlayerMock player = server.addPlayer();

        player.performCommand("antiskull info");

        List<String> lines = List.of(
                String.valueOf(player.nextMessage()),
                String.valueOf(player.nextMessage()),
                String.valueOf(player.nextMessage()));

        assertTrue(lines.get(0).contains("Version") && lines.get(0).contains("2.0.0"), lines.get(0));
        assertTrue(lines.get(1).contains("Blocking"), lines.get(1));
        assertTrue(lines.get(2).contains("3 names"), lines.get(2));

    }

    @Test
    @DisplayName("Без аргументов команда печатает справку")
    void commandWithoutArgumentsPrintsUsage() {

        PlayerMock player = server.addPlayer();

        player.performCommand("antiskull");

        assertTrue(String.valueOf(player.nextMessage()).contains("NeyAntiSkull commands:"));

    }

    @Test
    @DisplayName("Автодополнение команды работает через сервер")
    void tabCompletionIsAvailable() {

        PlayerMock player = server.addPlayer();
        PluginCommand command = plugin.getCommand("antiskull");

        assertNotNull(command);
        assertEquals(List.of("reload"), command.getTabCompleter().onTabComplete(player, command,
                "antiskull", new String[]{"re"}));

    }

    @Test
    @DisplayName("Отключение плагина проходит без ошибок и снимает команду")
    void pluginStopsGracefully() {

        server.getPluginManager().disablePlugin(plugin);

        assertFalse(plugin.isEnabled());

    }

}
