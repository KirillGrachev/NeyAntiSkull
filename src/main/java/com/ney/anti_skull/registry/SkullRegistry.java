package com.ney.anti_skull.registry;

import com.ney.anti_skull.config.ConfigManager;

import java.util.HashSet;
import java.util.Set;

public class SkullRegistry {

    private final ConfigManager configManager;
    private final Set<String> registeredSkulls = new HashSet<>();

    public SkullRegistry(ConfigManager configManager) {
        this.configManager = configManager;
        initializeRegisteredSkulls();
    }

    private void initializeRegisteredSkulls() {
        configManager.getDisallowedSkulls().forEach(this::registerSkull);
    }

    public void registerSkull(String skullName) {
        registeredSkulls.add(skullName);
    }

    public boolean isSkullRegistered(String skullName) {
        return registeredSkulls.contains(skullName);
    }

    public void clearRegisteredSkulls() {
        registeredSkulls.clear();
    }

    public void reloadRegistry() {
        clearRegisteredSkulls();
        initializeRegisteredSkulls();
    }
}