package eu.neydev.antiskull;

import eu.neydev.antiskull.command.AntiSkullCommand;
import eu.neydev.antiskull.command.AntiSkullSubCommand;
import eu.neydev.antiskull.command.CommandDispatcher;
import eu.neydev.antiskull.command.sub.InfoSubCommand;
import eu.neydev.antiskull.command.sub.ReloadSubCommand;
import eu.neydev.antiskull.config.ConfigManager;
import eu.neydev.antiskull.event.EventDispatcher;
import eu.neydev.antiskull.listener.SkullPlaceListener;
import eu.neydev.antiskull.service.ConsoleService;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Точка входа плагина.
 *
 * Класс не final осознанно (в 1.x он был final): интеграционные тесты
 * MockBukkit грузят плагин через прокси-подкласс, а наследование JavaPlugin
 * в продакшене никто не использует - подклассы создаются только тестовой
 * средой.
 */
public class NeyAntiSkull extends JavaPlugin {

    private static final String COMMAND_ANTI_SKULL = "antiskull";

    private ConsoleService consoleService;
    private ConfigManager configManager;
    private ServiceContainer services;
    private @Nullable CommandDispatcher commandDispatcher;

    @Override
    public void onEnable() {

        this.consoleService = new ConsoleService(this);
        this.configManager = new ConfigManager(this, consoleService);
        this.services = new ServiceContainer(this, configManager, consoleService);

        new EventDispatcher(this).registerEvents(new SkullPlaceListener(this,
                configManager,
                services.getValidationService(),
                services.getRemovalService(),
                services.getPermissionService(),
                services.getMessageService()));

        this.commandDispatcher = new CommandDispatcher(this, consoleService);
        commandDispatcher.registerCommand(COMMAND_ANTI_SKULL, new AntiSkullCommand(
                services.getMessageService(),
                services.getPermissionService(),
                buildSubCommands()
        ));

        getLogger().info("NeyAntiSkull started successfully.");

    }

    @Override
    public void onDisable() {

        if (commandDispatcher != null) {
            commandDispatcher.unregisterCommand(COMMAND_ANTI_SKULL);
        }

        getLogger().info("NeyAntiSkull stopped.");

    }

    public ConsoleService getConsoleService() {
        return consoleService;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ServiceContainer getServices() {
        return services;
    }

    /**
     * Собирает подкоманды: слушатель и команда получают зависимости из
     * контейнера, а не тянут их из плагина сами.
     */
    private @NotNull List<AntiSkullSubCommand> buildSubCommands() {
        return List.of(
                new ReloadSubCommand(configManager, services.getSkullRegistry(),
                        services.getMessageService()),
                new InfoSubCommand(this, configManager, services.getSkullRegistry(),
                        services.getMessageService())
        );
    }

}
