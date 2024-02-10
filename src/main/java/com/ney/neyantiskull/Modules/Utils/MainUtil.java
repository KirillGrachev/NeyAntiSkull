package com.ney.neyantiskull.Modules.Utils;

import com.ney.neyantiskull.NeyAntiSkull;

import java.util.List;
import java.util.stream.Collectors;

import static com.ney.neyantiskull.Modules.Utils.Hex.color;

public class MainUtil {

    public Boolean getBoolean (String message) { return NeyAntiSkull.getInstance().getConfig().getBoolean(message); }
    public List<String> getStringList(String message) {

        return NeyAntiSkull.getInstance().getConfig().getStringList(message)
                .stream()
                .map(Hex::color)
                .collect(Collectors.toList());

    }

    public List<String> getStringListWithoutColor(String message) { return NeyAntiSkull.getInstance().getConfig().getStringList(message); }

    public List<String> disallowedSkull = getStringList("Settings.list.names");
    public boolean SettingsEnabled = getBoolean("Settings.enabled");
    public boolean SettingsTakeAway = getBoolean("Settings.take_away");
    public boolean SettingsListEnabled = getBoolean("Settings.list.enabled");
    public boolean MessagesOnPlace = getBoolean("Messages.on_place.enabled");

}