package eu.neydev.antiskull;

import eu.neydev.antiskull.config.ConfigManager;
import eu.neydev.antiskull.event.EventDispatcher;
import eu.neydev.antiskull.listener.SkullPlaceListener;
import eu.neydev.antiskull.registry.SkullRegistry;
import eu.neydev.antiskull.service.ConsoleService;
import eu.neydev.antiskull.service.MessageService;
import eu.neydev.antiskull.service.PermissionService;
import eu.neydev.antiskull.service.SkullRemovalService;
import eu.neydev.antiskull.service.SkullValidationService;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Точка входа плагина.
 */
public final class NeyAntiSkull extends JavaPlugin {

    private ConsoleService consoleService;
    private ConfigManager configManager;
    private SkullRegistry skullRegistry;
    private MessageService messageService;
    private PermissionService permissionService;
    private SkullValidationService validationService;
    private SkullRemovalService removalService;

    @Override
    public void onEnable() {

        this.consoleService = new ConsoleService(this);
        this.configManager = new ConfigManager(this, consoleService);

        this.skullRegistry = new SkullRegistry(configManager);
        this.messageService = new MessageService(configManager);
        this.permissionService = new PermissionService(configManager);
        this.validationService = new SkullValidationService(configManager, skullRegistry);
        this.removalService = new SkullRemovalService();

        // Подписка на reload конфигурации - здесь, а не в конструкторе реестра:
        // компонент не должен публиковать this до завершения собственного создания
        configManager.onReload(skullRegistry::reload);

        // Регистрация слушателей
        new EventDispatcher(this).registerEvents(new SkullPlaceListener(this,
                configManager,
                validationService,
                removalService,
                permissionService,
                messageService));

        getLogger().info("NeyAntiSkull started successfully.");

    }

    @Override
    public void onDisable() {
        getLogger().info("NeyAntiSkull stopped.");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

}
