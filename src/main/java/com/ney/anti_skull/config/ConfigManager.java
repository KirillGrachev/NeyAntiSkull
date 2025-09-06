package com.ney.anti_skull.config;

import com.ney.anti_skull.NeyAntiSkull;
import com.ney.anti_skull.config.type.TakeAwayType;
import com.ney.anti_skull.util.HexColorUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigManager implements AntiSkullConfig {

    private final NeyAntiSkull plugin;
    private FileConfiguration config;

    private static final String PATH_ENABLED = "settings.enabled";
    private static final String PATH_LIST_ENABLED = "settings.list.enabled";
    private static final String PATH_TAKE_AWAY_ENABLED = "settings.take_away.enabled";
    private static final String PATH_TAKE_AWAY_TYPE = "settings.take_away.type";
    private static final String PATH_MESSAGES_ENABLED = "messages.on_place.enabled";
    private static final String PATH_DISALLOWED_SKULLS = "settings.list.names";
    private static final String PATH_BLOCKED_MESSAGE = "messages.on_place.text";
    private static final String PATH_CASE_SENSITIVE = "settings.list.case_sensitive";
    private static final String PATH_PERMISSIONS_ENABLED = "settings.permissions.enabled";
    private static final String PATH_PERMISSION_PLACE = "settings.permissions.place";
    private static final String PATH_PERMISSION_TAKE_AWAY = "settings.permissions.take_away";

    private boolean skullBlockingEnabled;
    private boolean listEnabled;
    private boolean takeAwayEnabled;
    private boolean messagesEnabled;
    private boolean caseSensitive;
    private boolean permissionsEnabled;

    private String permissionPlace;
    private String permissionTakeAway;

    public ConfigManager(NeyAntiSkull plugin) {

        this.plugin = plugin;
        saveDefaultConfig();

        loadConfig();
        cacheConfigValues();

    }

    private void saveDefaultConfig() {
        plugin.saveDefaultConfig();
    }

    private void loadConfig() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    private void cacheConfigValues() {
        skullBlockingEnabled = config.getBoolean(PATH_ENABLED, true);
        listEnabled = config.getBoolean(PATH_LIST_ENABLED, true);
        takeAwayEnabled = config.getBoolean(PATH_TAKE_AWAY_ENABLED, false);
        messagesEnabled = config.getBoolean(PATH_MESSAGES_ENABLED, true);
        caseSensitive = config.getBoolean(PATH_CASE_SENSITIVE, false);
        permissionsEnabled = config.getBoolean(PATH_PERMISSIONS_ENABLED, true);
        permissionPlace = config.getString(PATH_PERMISSION_PLACE, "antiskull.place");
        permissionTakeAway = config.getString(PATH_PERMISSION_TAKE_AWAY, "antiskull.takeaway");
    }

    @Override
    public boolean isSkullBlockingEnabled() {
        return skullBlockingEnabled;
    }

    @Override
    public boolean shouldRemoveSkull() {
        return takeAwayEnabled;
    }

    @Override
    public boolean isListEnabled() {
        return listEnabled;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    @Override
    public boolean areMessagesEnabled() {
        return messagesEnabled;
    }

    @Override
    public boolean arePermissionsEnabled() {
        return permissionsEnabled;
    }

    @Override
    public String getPermissionPlace() {
        return permissionPlace;
    }

    @Override
    public String getPermissionTakeAway() {
        return permissionTakeAway;
    }

    @Override
    public List<String> getDisallowedSkulls() {
        return config.getStringList(PATH_DISALLOWED_SKULLS).stream()
                .map(HexColorUtil::color)
                .collect(Collectors.toList());
    }

    @Override
    public TakeAwayType getTakeAwayType() {
        String configValue = config.getString(PATH_TAKE_AWAY_TYPE, "HAND");
        try {
            return TakeAwayType.valueOf(configValue.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid take_away.type: '" + configValue + "'. Using default: HAND.");
            return TakeAwayType.HAND;
        }
    }

    @Override
    public List<String> getBlockedMessage() {
        return config.getStringList(PATH_BLOCKED_MESSAGE).stream()
                .map(HexColorUtil::color)
                .collect(Collectors.toList());
    }

    public void reload() {
        loadConfig();
        cacheConfigValues();
    }
}