package eu.neydev.antiskull;

import eu.neydev.antiskull.config.ConfigManager;
import eu.neydev.antiskull.registry.SkullRegistry;
import eu.neydev.antiskull.service.ConsoleService;
import eu.neydev.antiskull.service.MessageService;
import eu.neydev.antiskull.service.PermissionService;
import eu.neydev.antiskull.service.SkullRemovalService;
import eu.neydev.antiskull.service.SkullValidationService;
import org.jetbrains.annotations.NotNull;

/**
 * Контейнер компонентов плагина.
 *
 * Порядок создания зафиксирован: каждый сервис получает уже готовые
 * зависимости и никто не создает их себе сам. Версия 1.x собирала граф в
 * конструкторе слушателя, из-за чего сервисы нельзя было ни переиспользовать
 * в команде, ни подменить в тесте.
 */
public final class ServiceContainer {

    private final NeyAntiSkull plugin;
    private final ConfigManager configManager;
    private final ConsoleService consoleService;

    private final SkullRegistry skullRegistry;
    private final MessageService messageService;
    private final PermissionService permissionService;
    private final SkullValidationService validationService;
    private final SkullRemovalService removalService;

    public ServiceContainer(@NotNull NeyAntiSkull plugin,
                            @NotNull ConfigManager configManager,
                            @NotNull ConsoleService consoleService) {

        this.plugin = plugin;
        this.configManager = configManager;
        this.consoleService = consoleService;

        // Реестр читает список имен из конфигурации, поэтому создается первым
        this.skullRegistry = new SkullRegistry(configManager);

        // Базовый слой: сообщения и права читают только конфигурацию
        this.messageService = new MessageService(configManager);
        this.permissionService = new PermissionService(configManager);

        // Слой проверки и конфискации
        this.validationService = new SkullValidationService(configManager, skullRegistry);
        this.removalService = new SkullRemovalService();

        // Подписка на reload конфигурации - здесь, а не в конструкторе реестра:
        // компонент не должен публиковать this до завершения собственного создания
        configManager.onReload(skullRegistry::reload);

    }

    public NeyAntiSkull getPlugin() {
        return plugin;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ConsoleService getConsoleService() {
        return consoleService;
    }

    public SkullRegistry getSkullRegistry() {
        return skullRegistry;
    }

    public MessageService getMessageService() {
        return messageService;
    }

    public PermissionService getPermissionService() {
        return permissionService;
    }

    public SkullValidationService getValidationService() {
        return validationService;
    }

    public SkullRemovalService getRemovalService() {
        return removalService;
    }

}
