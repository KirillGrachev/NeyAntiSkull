package com.ney.anti_skull;

import com.ney.anti_skull.config.ConfigManager;
import com.ney.anti_skull.event.EventDispatcher;
import com.ney.anti_skull.listener.SkullPlaceListener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Главный класс плагина NeyAntiSkull.
 * Управляет жизненным циклом и инициализирует компоненты.
 */
public final class NeyAntiSkull extends JavaPlugin {

    private ConfigManager configManager;

    @Override
    public void onEnable() {

        this.configManager = new ConfigManager(this);

        // Регистрация слушателей
        new EventDispatcher(this).registerEvents(
                new SkullPlaceListener(this)
        );

        getLogger().info("NeyAntiSkull успешно запущен!");

    }

    @Override
    public void onDisable() {
        getLogger().info("NeyAntiSkull остановлен!");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}