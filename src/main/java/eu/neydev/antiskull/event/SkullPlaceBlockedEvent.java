package eu.neydev.antiskull.event;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Вызывается, когда установка головы игрока признана запрещенной, -
 * до отмены BlockPlaceEvent и до конфискации предмета.
 *
 * Два контракта Bukkit соблюдены явно:
 * 1. Событие синхронное: конструктор не передает флаг async, иначе вызов
 *    из главного потока падает с "may only be triggered asynchronously".
 * 2. HandlerList собственный и статический: общий список приводил бы к тому,
 *    что слушатель одного события получал бы все события плагина.
 *
 * Отмена означает «пропустить установку»: BlockPlaceEvent не отменяется,
 * сообщение не отправляется, предмет не изымается. Так плагины регионов и
 * арены могут разрешить головы на своей территории, не выключая NeyAntiSkull
 * целиком. {@link #setTakeAway} отключает одну только конфискацию: запрет
 * остается в силе, но предмет игроку возвращают.
 */
public class SkullPlaceBlockedEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final Block block;
    private final ItemStack item;

    private boolean takeAway;
    private boolean cancelled;

    public SkullPlaceBlockedEvent(@NotNull Player player,
                                  @NotNull Block block,
                                  @NotNull ItemStack item,
                                  boolean takeAway) {
        this.player = player;
        this.block = block;
        this.item = item;
        this.takeAway = takeAway;
    }

    /**
     * Игрок, ставивший голову.
     */
    public @NotNull Player getPlayer() {
        return player;
    }

    /**
     * Блок, на место которого вставала голова.
     */
    public @NotNull Block getBlock() {
        return block;
    }

    /**
     * Предмет, которым ставился блок.
     */
    public @NotNull ItemStack getItem() {
        return item;
    }

    /**
     * Будет ли предмет изъят у игрока.
     */
    public boolean isTakeAway() {
        return takeAway;
    }

    /**
     * Включает или отключает конфискацию, не отменяя сам запрет.
     *
     * @param takeAway true, чтобы забрать предмет
     */
    public void setTakeAway(boolean takeAway) {
        this.takeAway = takeAway;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }

}
