package com.ney.anti_skull.registry;

import com.ney.anti_skull.config.ConfigManager;

import java.util.HashSet;
import java.util.Set;

/**
 * Реестр запрещённых игровых голов.
 * Хранит и управляет списком заблокированных названий голов из конфигурации.
 * Обеспечивает проверку принадлежности головы к запрещённым.
 */
public class SkullRegistry {

    private final ConfigManager configManager;
    private final Set<String> registeredSkulls = new HashSet<>();

    public SkullRegistry(ConfigManager configManager) {
        this.configManager = configManager;
        initializeRegisteredSkulls();
    }

    /**
     * Инициализирует реестр, загружая запрещённые головы из конфига.
     */
    private void initializeRegisteredSkulls() {
        configManager.getDisallowedSkulls().forEach(this::registerSkull);
    }

    /**
     * Регистрирует новое название головы в реестре запрещённых.
     *
     * @param skullName название головы для блокировки
     */
    public void registerSkull(String skullName) {
        registeredSkulls.add(skullName);
    }

    /**
     * Проверяет, находится ли голова в списке запрещённых.
     *
     * @param skullName отображаемое название головы для проверки
     * @return true если череп запрещён, false если разрешён или отсутствует
     */
    public boolean isSkullRegistered(String skullName) {
        return registeredSkulls.contains(skullName);
    }

    /**
     * Очищает реестр запрещённых голов.
     */
    public void clearRegisteredSkulls() {
        registeredSkulls.clear();
    }

    /**
     * Перезагружает реестр из конфигурации.
     * Сначала очищает текущий список, затем загружает заново.
     */
    public void reloadRegistry() {
        clearRegisteredSkulls();
        initializeRegisteredSkulls();
    }
}