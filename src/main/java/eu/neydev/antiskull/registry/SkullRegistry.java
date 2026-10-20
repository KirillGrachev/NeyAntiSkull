package eu.neydev.antiskull.registry;

import eu.neydev.antiskull.config.AntiSkullConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Реестр запрещенных имен голов.
 *
 * Имена нормализуются один раз при загрузке и хранятся в Set, а не в
 * Map&lt;String, Boolean&gt;: значение в версии 1.x никогда не читалось,
 * а keySet у newKeySet дает ту же потокобезопасность без коробки Boolean
 * на каждое имя. Регистр приводится по настройке settings.list.case_sensitive,
 * и нормализация живет именно здесь, а не в вызывающем коде, - иначе
 * добавление и сравнение рано или поздно разъехались бы.
 *
 * Приведение регистра идет через Locale.ROOT: toLowerCase() без локали на
 * сервере с турецкой локалью превратил бы "I" в "ı", и имя из конфига
 * перестало бы совпадать само с собой.
 *
 * Подписка на reload конфигурации оформляется снаружи (ServiceContainer),
 * а не в конструкторе: компонент не должен публиковать this до завершения
 * собственного создания.
 */
public final class SkullRegistry {

    private final AntiSkullConfig config;
    private final Set<String> names = ConcurrentHashMap.newKeySet();

    public SkullRegistry(@NotNull AntiSkullConfig config) {

        this.config = config;

        reload();

    }

    /**
     * Перечитывает список из конфигурации.
     *
     * Список пересобирается целиком, а не дополняется: иначе имена, удаленные
     * из config.yml, продолжали бы блокироваться до рестарта.
     */
    public void reload() {

        names.clear();

        for (String name : config.getDisallowedSkullNames()) {

            if (!name.isEmpty()) {
                names.add(normalize(name));
            }

        }

    }

    /**
     * Проверяет видимое имя головы по списку.
     *
     * @param displayName legacy-имя предмета или null, если имени нет
     * @return true если имя запрещено
     */
    public boolean isListed(@Nullable String displayName) {

        if (displayName == null || displayName.isEmpty()) {
            return false;
        }

        return names.contains(normalize(displayName));

    }

    /**
     * Количество имен в реестре (для диагностики и сообщения о reload).
     */
    public int size() {
        return names.size();
    }

    /**
     * Нормализованные имена списка - только для чтения.
     */
    public @NotNull Set<String> getNames() {
        return Collections.unmodifiableSet(names);
    }

    private @NotNull String normalize(@NotNull String name) {
        return config.isCaseSensitive() ? name : name.toLowerCase(Locale.ROOT);
    }

}
