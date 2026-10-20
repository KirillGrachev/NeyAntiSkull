package eu.neydev.antiskull.config;

import eu.neydev.antiskull.NeyAntiSkull;
import eu.neydev.antiskull.config.type.ConsoleMessage;
import eu.neydev.antiskull.config.type.MessageKey;
import eu.neydev.antiskull.config.type.PermissionNode;
import eu.neydev.antiskull.config.type.TakeAwayType;
import eu.neydev.antiskull.service.ConsoleService;
import eu.neydev.antiskull.util.HexColorUtil;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

/**
 * Менеджер конфигурации NeyAntiSkull.
 *
 * Значения читаются один раз и собираются в неизменяемый снапшот, который
 * подменяется одной volatile-записью. Слушатель установки блоков вызывается
 * на каждом BlockPlaceEvent, то есть постоянно: до снапшота он на каждом
 * таком вызове перечитывал getStringList из YAML и перекрашивал имена,
 * теперь чтение конфигурации вообще не выходит за пределы reload.
 *
 * Версия 1.x держала десять отдельных полей, перезаписываемых по очереди,
 * поэтому reload можно было застать наполовину примененным (список уже
 * новый, права еще старые). Некорректные значения загрузку не роняют:
 * подставляется дефолт, а в консоль уходит предупреждение через
 * {@link ConsoleService}.
 *
 * Конфигурации 1.x читаются без ручной миграции: messages.on_place и вся
 * секция settings.permissions находятся по старым путям с единственной
 * подсказкой LEGACY_PATH в консоль, файл на диске не переписывается.
 *
 * Источник истины один - {@code plugin.getConfig()}: отдельного парсинга
 * файла менеджер не ведет, reload использует штатный reloadConfig().
 * Поврежденный YAML не тонет в тишине: Bukkit глотает
 * InvalidConfigurationException и отдает пустую конфигурацию, поэтому файл
 * перечитывается явно и ошибка уходит в SEVERE-лог со стектрейсом.
 */
public class ConfigManager implements AntiSkullConfig {

    private final NeyAntiSkull plugin;
    private final ConsoleService consoleService;
    private final List<Runnable> reloadListeners = new CopyOnWriteArrayList<>();

    private static final String PATH_ENABLED = "settings.enabled";
    private static final String PATH_LIST_ENABLED = "settings.list.enabled";
    private static final String PATH_LIST_CASE_SENSITIVE = "settings.list.case_sensitive";
    private static final String PATH_LIST_NAMES = "settings.list.names";
    private static final String PATH_TAKE_AWAY_ENABLED = "settings.take_away.enabled";
    private static final String PATH_TAKE_AWAY_TYPE = "settings.take_away.type";
    private static final String PATH_PERMISSIONS_ENABLED = "permissions.enabled";
    private static final String PATH_LEGACY_PERMISSIONS_ENABLED = "settings.permissions.enabled";
    private static final String PATH_MESSAGES_ENABLED = "messages.enabled";
    private static final String PATH_MESSAGE_PREFIX = "messages.prefix";

    /**
     * Дефолты совпадают с поставляемым config.yml. В 1.x код и файл
     * расходились (take_away.enabled: true в файле при false в коде,
     * permissions.enabled: false в файле при true в коде), и расхождение
     * всплывало ровно тогда, когда файл потерян или бит, - то есть в самый
     * неподходящий момент.
     */
    private static final boolean DEFAULT_LIST_ENABLED = true;
    private static final boolean DEFAULT_CASE_SENSITIVE = false;
    private static final boolean DEFAULT_TAKE_AWAY_ENABLED = true;
    private static final boolean DEFAULT_PERMISSIONS_ENABLED = false;
    private static final TakeAwayType DEFAULT_TAKE_AWAY_TYPE = TakeAwayType.HAND;

    /** Окрашенные дефолты: fallback {@link #getMessages} не пересобирает строки на каждый вызов. */
    private static final Map<MessageKey, List<String>> DEFAULT_MESSAGES;

    static {

        Map<MessageKey, List<String>> defaults = new EnumMap<>(MessageKey.class);

        for (MessageKey key : MessageKey.values()) {
            defaults.put(key, coloredLines(key.getDefaultMessage()));
        }

        DEFAULT_MESSAGES = Collections.unmodifiableMap(defaults);

    }

    private record ListSection(boolean enabled, boolean caseSensitive, List<String> names) {
    }

    private record TakeAwaySection(boolean enabled, TakeAwayType type) {
    }

    private record PermissionsSection(boolean enabled, Map<PermissionNode, String> permissions) {
    }

    private record MessagingSection(boolean enabled,
                                    String prefix,
                                    Map<MessageKey, List<String>> messages) {
    }

    private record ConfigSnapshot(boolean pluginEnabled,
                                  ListSection list,
                                  TakeAwaySection takeAway,
                                  PermissionsSection permissions,
                                  MessagingSection messaging) {
    }

    /**
     * Текущий снапшот. Единственное изменяемое поле менеджера; подменяется
     * атомарно, поэтому составные значения никогда не читаются «наполовину».
     */
    private volatile ConfigSnapshot snapshot;

    private FileConfiguration config;

    public ConfigManager(@NotNull NeyAntiSkull plugin, @NotNull ConsoleService consoleService) {

        this.plugin = plugin;
        this.consoleService = consoleService;

        plugin.saveDefaultConfig();

        this.config = plugin.getConfig();

        validateReadable();
        this.snapshot = buildSnapshot();

    }

    /**
     * Перезагружает конфигурацию и уведомляет подписанные компоненты.
     */
    public void reload() {

        plugin.reloadConfig();

        this.config = plugin.getConfig();

        validateReadable();

        this.snapshot = buildSnapshot();

        reloadListeners.forEach(Runnable::run);

    }

    /**
     * Подписывает компонент на перезагрузку конфигурации.
     *
     * @param listener действие, выполняемое после reload
     */
    public void onReload(@NotNull Runnable listener) {
        reloadListeners.add(listener);
    }

    @Override
    public boolean isPluginEnabled() {
        return snapshot.pluginEnabled();
    }

    @Override
    public boolean isListEnabled() {
        return snapshot.list().enabled();
    }

    @Override
    public boolean isCaseSensitive() {
        return snapshot.list().caseSensitive();
    }

    @Override
    public @NotNull List<String> getDisallowedSkullNames() {
        return snapshot.list().names();
    }

    @Override
    public boolean isTakeAwayEnabled() {
        return snapshot.takeAway().enabled();
    }

    @Override
    public @NotNull TakeAwayType getTakeAwayType() {
        return snapshot.takeAway().type();
    }

    @Override
    public boolean arePermissionsEnabled() {
        return snapshot.permissions().enabled();
    }

    @Override
    public @NotNull String getPermission(@NotNull PermissionNode node) {
        return snapshot.permissions().permissions().getOrDefault(node, node.getDefaultPermission());
    }

    @Override
    public boolean areMessagesEnabled() {
        return snapshot.messaging().enabled();
    }

    @Override
    public @NotNull String getMessagePrefix() {
        return snapshot.messaging().prefix();
    }

    @Override
    public @NotNull List<String> getMessages(@NotNull MessageKey key) {
        return snapshot.messaging().messages()
                .getOrDefault(key, DEFAULT_MESSAGES.getOrDefault(key, List.of()));
    }

    /**
     * Явно перечитывает файл, если результат штатной загрузки пуст:
     * поврежденный YAML должен кричать в консоль, а не тихо работать на дефолтах.
     */
    private void validateReadable() {

        File configFile = new File(plugin.getDataFolder(), "config.yml");

        if (!configFile.exists() || !config.getKeys(false).isEmpty()) {
            return;
        }

        try {
            new YamlConfiguration().load(configFile);
        } catch (IOException | InvalidConfigurationException exception) {
            plugin.getLogger().log(Level.SEVERE,
                    "config.yml is broken and cannot be parsed - built-in defaults are used. "
                            + "Fix the file and run /antiskull reload.",
                    exception);
        }

    }

    private @NotNull ConfigSnapshot buildSnapshot() {
        return new ConfigSnapshot(
                config.getBoolean(PATH_ENABLED, true),
                buildListSection(),
                buildTakeAwaySection(),
                buildPermissionsSection(),
                buildMessagingSection()
        );
    }

    private @NotNull ListSection buildListSection() {
        return new ListSection(
                config.getBoolean(PATH_LIST_ENABLED, DEFAULT_LIST_ENABLED),
                config.getBoolean(PATH_LIST_CASE_SENSITIVE, DEFAULT_CASE_SENSITIVE),
                readColoredNames(PATH_LIST_NAMES)
        );
    }

    private @NotNull TakeAwaySection buildTakeAwaySection() {
        return new TakeAwaySection(
                config.getBoolean(PATH_TAKE_AWAY_ENABLED, DEFAULT_TAKE_AWAY_ENABLED),
                parseTakeAwayType()
        );
    }

    private @NotNull PermissionsSection buildPermissionsSection() {

        Map<PermissionNode, String> permissions = new EnumMap<>(PermissionNode.class);

        for (PermissionNode node : PermissionNode.values()) {

            String path = permissionPath(node);
            permissions.put(node, config.getString(path, node.getDefaultPermission()));

        }

        return new PermissionsSection(
                readBooleanWithLegacy(PATH_PERMISSIONS_ENABLED,
                        PATH_LEGACY_PERMISSIONS_ENABLED, DEFAULT_PERMISSIONS_ENABLED),
                Collections.unmodifiableMap(permissions)
        );

    }

    private @NotNull MessagingSection buildMessagingSection() {

        Map<MessageKey, List<String>> messages = new EnumMap<>(MessageKey.class);

        for (MessageKey key : MessageKey.values()) {

            String path = keyedPath("messages.", key.getConfigKey(), key.getLegacyConfigKey());

            if (!config.getBoolean(path + ".enabled", key.isDefaultEnabled())) {
                messages.put(key, List.of());
                continue;
            }

            messages.put(key, readColoredLines(path + ".text", key.getDefaultMessage()));

        }

        return new MessagingSection(
                config.getBoolean(PATH_MESSAGES_ENABLED, true),
                color(config.getString(PATH_MESSAGE_PREFIX, "")),
                Collections.unmodifiableMap(messages)
        );

    }

    /**
     * Читает режим конфискации с предупреждением об неизвестном значении.
     *
     * @return тип из конфигурации или {@link #DEFAULT_TAKE_AWAY_TYPE}
     */
    private @NotNull TakeAwayType parseTakeAwayType() {

        String raw = config.getString(PATH_TAKE_AWAY_TYPE, DEFAULT_TAKE_AWAY_TYPE.name());
        TakeAwayType parsed = TakeAwayType.fromString(raw);

        if (parsed != null) {
            return parsed;
        }

        consoleService.log(ConsoleMessage.INVALID_VALUE,
                "path", PATH_TAKE_AWAY_TYPE,
                "value", String.valueOf(raw),
                "defaultValue", DEFAULT_TAKE_AWAY_TYPE.name());
        return DEFAULT_TAKE_AWAY_TYPE;

    }

    /**
     * Возвращает путь узла права с откатом на секцию 1.x.
     *
     * @param node узел права
     * @return путь для чтения значения
     */
    private @NotNull String permissionPath(@NotNull PermissionNode node) {

        String path = "permissions." + node.getConfigKey();

        if (config.isSet(path) || node.getLegacyPath() == null) {
            return path;
        }

        String legacyPath = node.getLegacyPath();

        if (!config.isSet(legacyPath)) {
            return path;
        }

        consoleService.log(ConsoleMessage.LEGACY_PATH,
                "path", legacyPath,
                "replacement", path);
        return legacyPath;

    }

    private boolean readBooleanWithLegacy(@NotNull String path,
                                          @NotNull String legacyPath,
                                          boolean defaultValue) {

        if (config.isSet(path)) {
            return config.getBoolean(path, defaultValue);
        }

        if (config.isSet(legacyPath)) {

            consoleService.log(ConsoleMessage.LEGACY_PATH,
                    "path", legacyPath,
                    "replacement", path);
            return config.getBoolean(legacyPath, defaultValue);

        }

        return defaultValue;

    }

    /**
     * Возвращает путь ключа сообщения с откатом на устаревший.
     *
     * Новый путь побеждает всегда: если серверовладелец продублировал ключ,
     * читается актуальный, а предупреждение не печатается.
     *
     * @param prefix    префикс секции ({@code messages.})
     * @param key       текущий ключ
     * @param legacyKey ключ версии 1.x или null, если ключ появился в 2.0.0
     * @return путь для чтения значения
     */
    private @NotNull String keyedPath(@NotNull String prefix,
                                      @NotNull String key,
                                      @Nullable String legacyKey) {

        String path = prefix + key;

        if (config.isSet(path) || legacyKey == null) {
            return path;
        }

        String legacyPath = prefix + legacyKey;

        if (!config.isSet(legacyPath)) {
            return path;
        }

        consoleService.log(ConsoleMessage.LEGACY_PATH,
                "path", legacyPath,
                "replacement", path);
        return legacyPath;

    }

    /**
     * Читает список запрещенных имен, окрашивая каждое.
     *
     * Окраска здесь не косметика, а часть сравнения: видимое имя предмета
     * Bukkit отдает legacy-строкой с символами §, и имя из config.yml должно
     * пройти тот же путь, иначе "&aPlayer Head" в файле никогда не совпадет
     * с "§aPlayer Head" в предмете. Пустые элементы списка пропускаются:
     * пустое имя означало бы блокировку всех безымянных голов.
     */
    private @NotNull List<String> readColoredNames(@NotNull String path) {

        Object raw = config.get(path);

        if (raw == null) {
            return List.of();
        }

        if (raw instanceof List<?> list) {

            List<String> names = new ArrayList<>(list.size());

            for (Object element : list) {

                if (element == null) {
                    continue;
                }

                String name = color(String.valueOf(element));

                if (!name.isEmpty()) {
                    names.add(name);
                }

            }

            return Collections.unmodifiableList(names);

        }

        String single = color(String.valueOf(raw));
        return single.isEmpty() ? List.of() : List.of(single);

    }

    private @NotNull List<String> readColoredLines(@NotNull String path, @NotNull String defaultValue) {

        Object raw = config.get(path);

        if (raw == null) {
            return coloredLines(defaultValue);
        }

        if (raw instanceof List<?> list) {

            List<String> lines = new ArrayList<>(list.size());

            for (Object element : list) {
                if (element != null) {
                    lines.add(color(String.valueOf(element)));
                }
            }

            return Collections.unmodifiableList(lines);

        }

        return coloredLines(String.valueOf(raw));

    }

    /**
     * Окрашивает текст и режет его на строки.
     *
     * Многострочные дефолты (usage, info, blocked) живут одним значением с \n,
     * а отправляются списком: так один и тот же текст одинаково читается и в
     * коде, и в config.yml, где он задан списком строк. Лимит -1 у split
     * обязателен - без него хвостовые пустые строки теряются, и дефолт
     * сообщения расходится с тем, что лежит в файле.
     */
    private static @NotNull List<String> coloredLines(@NotNull String text) {
        return List.of(HexColorUtil.color(text).split("\n", -1));
    }

    private @NotNull String color(@Nullable String text) {
        return HexColorUtil.color(text);
    }

}
