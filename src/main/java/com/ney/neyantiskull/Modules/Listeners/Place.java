package com.ney.neyantiskull.Modules.Listeners;

import com.ney.neyantiskull.Modules.Utils.MainUtil;
import org.bukkit.SkullType;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class Place implements Listener {

    private final MainUtil mainUtil;
    public Place() { this.mainUtil = new MainUtil(); }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {

        ItemStack itemStack = event.getItemInHand();
        ItemMeta itemMeta = itemStack.getItemMeta();
        BlockState blockState = event.getBlock().getState();

        if (!(blockState instanceof Skull) || !mainUtil.SettingsEnabled)
            return;

        Skull skull = (Skull) blockState;
        Player player = event.getPlayer();

        if (skull.getSkullType() == SkullType.PLAYER && itemMeta instanceof SkullMeta) {

            SkullMeta skullMeta = (SkullMeta) itemMeta;

            if (skullMeta != null) {

                String displayname = skullMeta.getDisplayName();

                if (mainUtil.SettingsListEnabled && mainUtil.disallowedSkull.stream().
                        anyMatch(string -> string.equalsIgnoreCase(displayname))) {

                    event.setCancelled(true);
                    if (mainUtil.MessagesOnPlace) mainUtil.getStringList("Messages.on_place.text").forEach(player::sendMessage);

                } else if (!mainUtil.SettingsListEnabled) {

                    event.setCancelled(true);
                    if (mainUtil.MessagesOnPlace) mainUtil.getStringList("Messages.on_place.text").forEach(player::sendMessage);

                }
            }

            if (mainUtil.SettingsTakeAway) itemStack.setAmount(0);

        }
    }
}