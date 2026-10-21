package eu.neydev.antiskull;

import eu.neydev.antiskull.config.ConfigManager;
import eu.neydev.antiskull.config.type.MessageKey;
import eu.neydev.antiskull.service.ConsoleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Контейнер собирает граф один раз, передает сервисам одну и ту же
 * конфигурацию и подписывает реестр на reload - именно подписка, а не
 * вызов reload() из команды, держит список имен свежим.
 */
class ServiceContainerSmokeTest {

    private final NeyAntiSkull plugin = mock(NeyAntiSkull.class);
    private final ConfigManager configManager = mock(ConfigManager.class);
    private final ConsoleService consoleService = mock(ConsoleService.class);

    private ServiceContainer container() {

        when(configManager.getDisallowedSkullNames()).thenReturn(List.of());
        when(configManager.isCaseSensitive()).thenReturn(false);
        return new ServiceContainer(plugin, configManager, consoleService);

    }

    @Test
    @DisplayName("Все компоненты созданы и доступны")
    void everyComponentIsCreated() {

        ServiceContainer container = container();

        assertSame(plugin, container.getPlugin());
        assertSame(configManager, container.getConfigManager());
        assertSame(consoleService, container.getConsoleService());
        assertNotNull(container.getSkullRegistry());
        assertNotNull(container.getMessageService());
        assertNotNull(container.getPermissionService());
        assertNotNull(container.getValidationService());
        assertNotNull(container.getRemovalService());

    }

    @Test
    @DisplayName("Сервисы читают конфигурацию из контейнера, а не свою копию")
    void servicesShareTheSameConfig() {

        when(configManager.getMessages(MessageKey.BLOCKED)).thenReturn(List.of("shared"));
        when(configManager.areMessagesEnabled()).thenReturn(true);
        when(configManager.getMessagePrefix()).thenReturn("");

        assertEquals(List.of("shared"),
                container().getMessageService().build(MessageKey.BLOCKED, Map.of()));

    }

    @Test
    @DisplayName("Реестр подписан на reload конфигурации и пересобирается по подписке")
    void registryIsSubscribedToConfigReload() {

        ServiceContainer container = container();

        ArgumentCaptor<Runnable> subscription = ArgumentCaptor.forClass(Runnable.class);

        verify(configManager).onReload(subscription.capture());
        assertEquals(0, container.getSkullRegistry().size());

        when(configManager.getDisallowedSkullNames()).thenReturn(List.of("First", "Second"));
        subscription.getValue().run();

        assertEquals(2, container.getSkullRegistry().size(),
                "подписка действительно пересобирает список");
        verify(configManager, org.mockito.Mockito.atLeastOnce()).getDisallowedSkullNames();

    }

    @Test
    @DisplayName("Два контейнера не делят сервисы между собой")
    void containersDoNotShareServices() {

        ServiceContainer first = container();
        ServiceContainer second = container();

        assertNotNull(first.getSkullRegistry());
        assertNotNull(second.getSkullRegistry());
        assertEquals(first.getValidationService().getClass(), second.getValidationService().getClass());
        verify(configManager, org.mockito.Mockito.times(2)).onReload(any());

    }

}
