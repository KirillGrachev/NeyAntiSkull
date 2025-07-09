package com.ney.anti_skull.mocks;

import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockEventFactory {

    public static @NotNull BlockPlaceEvent createSkullPlaceEvent(String displayName) {

        ItemStack mockItem = mock(ItemStack.class);
        SkullMeta skullMeta = mock(SkullMeta.class);

        when(skullMeta.getDisplayName()).thenReturn(displayName);
        when(mockItem.getItemMeta()).thenReturn(skullMeta);

        BlockState mockBlockState = mock(Skull.class);
        when(mockBlockState.getType()).thenReturn(org.bukkit.Material.PLAYER_HEAD);
        when(((Skull) mockBlockState).getSkullType()).thenReturn(org.bukkit.SkullType.PLAYER);

        BlockPlaceEvent mockEvent = mock(BlockPlaceEvent.class);
        when(mockEvent.getItemInHand()).thenReturn(mockItem);
        when(mockEvent.getBlock()).thenReturn(mock(org.bukkit.block.Block.class));
        when(mockEvent.getBlock().getState()).thenReturn(mockBlockState);
        when(mockEvent.getPlayer()).thenReturn(mock(org.bukkit.entity.Player.class));

        return mockEvent;

    }
}