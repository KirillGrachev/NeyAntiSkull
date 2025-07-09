package com.ney.anti_skull.config;

import java.util.List;

/**
 * Конфигурационный интерфейс для плагина NeyAntiSkull.
 * Предоставляет доступ к настройкам управления головами.
 */
public interface AntiSkullConfig {

    /**
     * Проверяет, включена ли блокировка голов.
     *
     * @return true если блокировка активна, false в противном случае
     */
    boolean isSkullBlockingEnabled();

    /**
     * Проверяет, нужно ли удалять голову при блокировке установки.
     *
     * @return true если голова должна быть удалена, false иначе
     */
    boolean shouldRemoveSkull();

    /**
     * Проверяет, используется ли список запрещённых голов.
     *
     * @return true если список активен, false в противном случае
     */
    boolean isListEnabled();

    /**
     * Проверяет, показывать ли сообщения игроку при блокировке.
     *
     * @return true если сообщения включены, false иначе
     */
    boolean areMessagesEnabled();

    /**
     * Возвращает тип удаления заблокированных голов.
     *
     * @return тип удаления ("HAND" - из руки, "ALL" - все из инвентаря)
     */
    String getTakeAwayType();

    /**
     * Возвращает список запрещённых названий голов.
     *
     * @return список имён голов, которые нельзя устанавливать
     */
    List<String> getDisallowedSkulls();

    /**
     * Возвращает сообщение о блокировке для игрока.
     *
     * @return список строк сообщения (с поддержкой цветов)
     */
    List<String> getBlockedMessage();

}