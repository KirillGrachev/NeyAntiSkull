package com.ney.anti_skull.config;

import com.ney.anti_skull.NeyAntiSkull;
import com.ney.anti_skull.util.HexColorUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Менеджер конфигурации плагина.
 * Реализует загрузку и парсинг настроек из config.yml.
 */
public class ConfigManager implements AntiSkullConfig {

    private final NeyAntiSkull plugin;
    private FileConfiguration config;

    public ConfigManager(NeyAntiSkull plugin) {
        this.plugin = plugin;
        saveDefaultConfig();
        loadConfig();
    }

    private void saveDefaultConfig() {
        plugin.saveDefaultConfig();
    }

    private void loadConfig() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    @Override
    public boolean isSkullBlockingEnabled() {
        return config.getBoolean("settings.enabled", true);
    }

    @Override
    public boolean shouldRemoveSkull() {
        return config.getBoolean("settings.take_away.enabled", false);
    }

    @Override
    public boolean isListEnabled() {
        return config.getBoolean("settings.list.enabled", true);
    }

    @Override
    public boolean areMessagesEnabled() {
        return config.getBoolean("messages.on_place.enabled", true);
    }

    @Override
    public List<String> getDisallowedSkulls() {
        return config.getStringList("settings.list.names").stream()
                .map(HexColorUtil::color)
                .collect(Collectors.toList());
    }

    @Override
    public String getTakeAwayType() {
        return config.getString("settings.take_away.type", "HAND").toUpperCase();
    }

    @Override
    public List<String> getBlockedMessage() {
        return config.getStringList("messages.on_place.text").stream()
                .map(HexColorUtil::color)
                .collect(Collectors.toList());
    }
}