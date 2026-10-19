package eu.neydev.antiskull;

import eu.neydev.antiskull.config.ConfigManager;
import eu.neydev.antiskull.event.EventDispatcher;
import eu.neydev.antiskull.listener.SkullPlaceListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class NeyAntiSkull extends JavaPlugin {

    private ConfigManager configManager;

    @Override
    public void onEnable() {

        this.configManager = new ConfigManager(this);

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