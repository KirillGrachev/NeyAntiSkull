package com.ney.anti_skull.service;

import com.ney.anti_skull.registry.SkullRegistry;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

/**
 * Сервис валидации голов при установке.
 * Проверяет, разрешено ли игроку устанавливать данную голову.
 */
public class SkullValidationService {

    private final SkullRegistry skullRegistry;

    public SkullValidationService(SkullRegistry skullRegistry) {
        this.skullRegistry = skullRegistry;
    }

    /**
     * Проверяет валидность установки головы.
     *
     * @param event событие установки блока
     * @return true если установка разрешена, false если череп запрещён
     *
     * Логика проверки:
     * 1. Проверяет что установлена именно голова (по метаданным и типу блока)
     * 2. Извлекает отображаемое название головы
     * 3. Сверяет название с реестром запрещённых
     *
     * Возвращает true (разрешено) если:
     * - Предмет не голова
     * - Блок не превращается в голову
     * - У головы нет названия
     * - Название отсутствует в реестре запрещённых
     */
    public boolean isSkullPlacementValid(@NotNull BlockPlaceEvent event) {

        ItemStack itemStack = event.getItemInHand();
        if (itemStack == null || !itemStack.hasItemMeta()) {
            return true;
        }

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (!(itemMeta instanceof SkullMeta)) {
            return true;
        }

        BlockState blockState = event.getBlock().getState();
        if (!(blockState instanceof Skull)) {
            return true;
        }

        SkullMeta skullMeta = (SkullMeta) itemMeta;
        String displayName = skullMeta.getDisplayName();

        if (displayName == null || displayName.isEmpty()) {
            return true;
        }

        return !skullRegistry.isSkullRegistered(displayName);

    }
}