package eu.neydev.antiskull.config.type;

import org.jetbrains.annotations.Nullable;

/**
 * Что конфисковать у игрока при заблокированной установке головы.
 *
 * HAND забирает стек только из той руки, которой игрок ставил предмет
 * (установка из оффханда конфискует оффханд), ALL - все такие же головы
 * в инвентаре. Значение читается из settings.take_away.type без учета
 * регистра; неизвестное значение не роняет загрузку.
 */
public enum TakeAwayType {

    /** Забрать только стек из руки, использованной для установки. */
    HAND,

    /** Забрать все подходящие головы из инвентаря. */
    ALL;

    /**
     * Разбирает значение из конфигурации.
     *
     * Перечисление не знает ни про config.yml, ни про консоль: оно отвечает
     * только на вопрос «есть ли такое значение», а дефолт и предупреждение
     * выбирает менеджер конфигурации.
     *
     * @param value строка из config.yml
     * @return тип или null, если значение неизвестно либо пусто
     */
    public static @Nullable TakeAwayType fromString(@Nullable String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim();

        for (TakeAwayType type : values()) {
            if (type.name().equalsIgnoreCase(normalized)) {
                return type;
            }
        }

        return null;

    }

}
