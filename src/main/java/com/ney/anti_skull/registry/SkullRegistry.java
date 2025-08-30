package com.ney.anti_skull.registry;

import com.ney.anti_skull.config.ConfigManager;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SkullRegistry {

    private final ConfigManager configManager;
    private final Map<String, Boolean> registeredSkulls = new ConcurrentHashMap<>();

    public SkullRegistry(ConfigManager configManager) {
        this.configManager = configManager;
        initializeRegisteredSkulls();
    }

    private String normalizeSkullName(String name) {
        return configManager.isCaseSensitive() ? name : name.toLowerCase();
    }

    private void initializeRegisteredSkulls() {
        configManager.getDisallowedSkulls().forEach(name ->
                registeredSkulls.put(normalizeSkullName(name), Boolean.TRUE));
    }

    public void registerSkull(String skullName) {
        registeredSkulls.put(normalizeSkullName(skullName), Boolean.TRUE);
    }

    public boolean isSkullRegistered(String skullName) {
        return registeredSkulls.containsKey(normalizeSkullName(skullName));
    }

    public void clearRegisteredSkulls() {
        registeredSkulls.clear();
    }

    public void reloadRegistry() {
        clearRegisteredSkulls();
        initializeRegisteredSkulls();
    }

    public Collection<String> getRegisteredSkulls() {
        return registeredSkulls.keySet();
    }
}