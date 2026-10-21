package eu.neydev.antiskull.service;

import eu.neydev.antiskull.config.type.PermissionNode;
import eu.neydev.antiskull.support.StubConfig;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Два класса узлов: функциональные при выключенной системе прав разрешены
 * всем, оба bypass-узла - никому.
 */
class PermissionServiceTest {

    private final Player player = mock(Player.class);
    private final ConsoleCommandSender console = mock(ConsoleCommandSender.class);

    @Test
    @DisplayName("Выключенная система прав разрешает команды")
    void disabledSystemAllowsCommands() {

        PermissionService service = new PermissionService(new StubConfig());

        assertTrue(service.has(player, PermissionNode.RELOAD));
        assertTrue(service.has(null, PermissionNode.RELOAD), "даже без отправителя");

    }

    @Test
    @DisplayName("Выключенная система прав не разрешает обходить запрет")
    void disabledSystemNeverBypassesBlocking() {

        when(player.hasPermission("antiskull.bypass.place")).thenReturn(true);
        when(player.hasPermission("antiskull.bypass.takeaway")).thenReturn(true);

        PermissionService service = new PermissionService(new StubConfig());

        assertFalse(service.canBypassBlock(player),
                "иначе одна строка в конфиге отключала бы защиту всему серверу");
        assertFalse(service.canBypassTakeAway(player));

    }

    @Test
    @DisplayName("Включенная система прав проверяет узел из конфига")
    void enabledSystemChecksConfiguredNode() {

        StubConfig config = new StubConfig()
                .withPermissionsEnabled(true)
                .withPermission(PermissionNode.PLACE, "donor.heads");
        when(player.hasPermission("donor.heads")).thenReturn(true);

        assertTrue(new PermissionService(config).canBypassBlock(player));
        verify(player).hasPermission("donor.heads");

    }

    @Test
    @DisplayName("Узел конфискации работает независимо от узла установки")
    void takeAwayNodeIsIndependent() {

        StubConfig config = new StubConfig().withPermissionsEnabled(true);
        when(player.hasPermission("antiskull.bypass.takeaway")).thenReturn(true);
        when(player.hasPermission("antiskull.bypass.place")).thenReturn(false);

        PermissionService service = new PermissionService(config);

        assertTrue(service.canBypassTakeAway(player));
        assertFalse(service.canBypassBlock(player));

    }

    @Test
    @DisplayName("Без нужного узла доступ закрыт")
    void missingNodeDeniesAccess() {

        StubConfig config = new StubConfig().withPermissionsEnabled(true);
        when(player.hasPermission("antiskull.reload")).thenReturn(false);

        assertFalse(new PermissionService(config).has(player, PermissionNode.RELOAD));

    }

    @Test
    @DisplayName("Null-игрок не обходит запрет даже при включенной системе")
    void nullPlayerNeverBypasses() {

        StubConfig config = new StubConfig().withPermissionsEnabled(true);
        PermissionService service = new PermissionService(config);

        assertFalse(service.canBypassBlock(null));
        assertFalse(service.canBypassTakeAway(null));

    }

    @Test
    @DisplayName("Консоль проверяется так же, как игрок")
    void consoleIsCheckedLikePlayer() {

        StubConfig config = new StubConfig().withPermissionsEnabled(true);
        when(console.hasPermission("antiskull.reload")).thenReturn(true);

        assertTrue(new PermissionService(config).has(console, PermissionNode.RELOAD));

    }

}
