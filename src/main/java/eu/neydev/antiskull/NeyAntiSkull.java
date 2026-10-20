package eu.neydev.antiskull;

import eu.neydev.antiskull.config.ConfigManager;
import eu.neydev.antiskull.event.EventDispatcher;
import eu.neydev.antiskull.listener.SkullPlaceListener;
import eu.neydev.antiskull.service.ConsoleService;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Точка входа плагина.
 */
public final class NeyAntiSkull extends JavaPlugin {

    private ConsoleService consoleService;
    private ConfigManager configManager;

    @Override
    public void onEnable() {

        this.consoleService = new ConsoleService(this);
        this.configManager = new ConfigManager(this, consoleService);

        // Регистрация слушателей
        new EventDispatcher(this).registerEvents(
                new SkullPlaceListener(this)
        );

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
