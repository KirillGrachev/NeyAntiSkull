package eu.neydev.antiskull.service;

import eu.neydev.antiskull.config.AntiSkullConfig;
import eu.neydev.antiskull.registry.SkullRegistry;
import org.bukkit.Material;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.block.Skull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Проверка установки головы игрока.
 *
 * Порядок проверок - от дешевых и общих к дорогим и частным: выключенный
 * плагин и не-голова отсеиваются до обращения к мета-данным предмета,
 * а legacy-имя читается один раз и только если до него дошло дело.
 * BlockPlaceEvent приходит на каждый поставленный блок, поэтому лишний
 * getItemMeta() на установке земли стоил бы дороже самого плагина.
 */
public class SkullValidationService {

    private final AntiSkullConfig config;
    private final SkullRegistry skullRegistry;

    public SkullValidationService(@NotNull AntiSkullConfig config,
                                  @NotNull SkullRegistry skullRegistry) {
        this.config = config;
        this.skullRegistry = skullRegistry;
    }

    /**
     * Разрешена ли установка из этого события.
     *
     * @param event событие установки блока
     * @return true если блок можно оставить
     */
    public boolean isPlacementAllowed(@NotNull BlockPlaceEvent event) {

        if (!config.isPluginEnabled()) {
            return true;
        }

        if (!isSkullItem(event) || !isSkullBlock(event)) {
            return true;
        }

        // Выключенный список означает «блокировать все головы», а не «разрешить
        // все»: settings.list.enabled: false используется как тотальный запрет
        if (!config.isListEnabled()) {
            return false;
        }

        return !skullRegistry.isListed(displayName(event));

    }

    private boolean isSkullItem(@NotNull BlockPlaceEvent event) {
        return event.getItemInHand().getType() == Material.PLAYER_HEAD;
    }

    private boolean isSkullBlock(@NotNull BlockPlaceEvent event) {
        return event.getBlock().getState() instanceof Skull;
    }

    /**
     * Читает видимое имя головы в legacy-формате.
     *
     * getDisplayName, а не Adventure-displayName: имена в config.yml проходят
     * через HexColorUtil и содержат коды §, поэтому сравнивать их с
     * компонентом пришлось бы через сериализацию, а legacy-строка совпадает
     * с конфигом напрямую. Пустая строка вместо null означает «имени нет»,
     * и безымянная голова списком не блокируется.
     */
    private @Nullable String displayName(@NotNull BlockPlaceEvent event) {

        ItemMeta meta = event.getItemInHand().getItemMeta();

        if (!(meta instanceof SkullMeta)) {
            return null;
        }

        return meta.getDisplayName();

    }

}
