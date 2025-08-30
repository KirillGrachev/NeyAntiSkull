package com.ney.anti_skull.event;

import com.ney.anti_skull.NeyAntiSkull;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class EventDispatcher {

    private final NeyAntiSkull plugin;

    public EventDispatcher(NeyAntiSkull plugin) {
        this.plugin = plugin;
    }

    public void registerEvents(Listener @NotNull ... listeners) {
        for (Listener listener : listeners) {
            Bukkit.getPluginManager().registerEvents(listener, plugin);
        }
    }
}