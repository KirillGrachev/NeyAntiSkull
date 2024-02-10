package com.ney.neyantiskull;

import com.ney.neyantiskull.Modules.Listeners.Place;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class NeyAntiSkull extends JavaPlugin {

    private static NeyAntiSkull instance;

    @Override
    public void onEnable() {

        instance = this;
        saveDefaultConfig();

        registerListeners(Place.class);

    }

    public void registerListeners(Class<? extends Listener>... listenerClasses) {

        for (Class<? extends Listener> listenerClass : listenerClasses) {

            try {

                Listener listener = listenerClass.newInstance();
                Bukkit.getPluginManager().registerEvents(listener, NeyAntiSkull.getInstance());

            } catch (InstantiationException | IllegalAccessException e) { e.printStackTrace(); }
        }
    }

    public static NeyAntiSkull getInstance() { return NeyAntiSkull.instance; }
}