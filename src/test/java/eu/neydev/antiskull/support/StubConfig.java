package eu.neydev.antiskull.support;

import eu.neydev.antiskull.config.AntiSkullConfig;
import eu.neydev.antiskull.config.type.MessageKey;
import eu.neydev.antiskull.config.type.PermissionNode;
import eu.neydev.antiskull.config.type.TakeAwayType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Изменяемый стаб конфигурации для юнит-тестов.
 *
 * Сервисы и реестр проверяются без Bukkit и без YAML: тест задает ровно те
 * значения, которые нужны сценарию, и не зависит от порядка ключей в
 * config.yml. Дефолты повторяют поставляемый файл, поэтому «в тесте ничего
 * не настроили» означает то же, что на свежем сервере.
 */
public final class StubConfig implements AntiSkullConfig {

    private boolean pluginEnabled = true;
    private boolean listEnabled = true;
    private boolean caseSensitive = false;
    private List<String> names = List.of();
    private boolean takeAwayEnabled = true;
    private TakeAwayType takeAwayType = TakeAwayType.HAND;
    private boolean permissionsEnabled = false;
    private boolean messagesEnabled = true;
    private String prefix = "";
    private final Map<PermissionNode, String> permissions = new EnumMap<>(PermissionNode.class);
    private final Map<MessageKey, List<String>> messages = new EnumMap<>(MessageKey.class);

    public @NotNull StubConfig withPluginEnabled(boolean enabled) {
        this.pluginEnabled = enabled;
        return this;
    }

    public @NotNull StubConfig withListEnabled(boolean enabled) {
        this.listEnabled = enabled;
        return this;
    }

    public @NotNull StubConfig withCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
        return this;
    }

    public @NotNull StubConfig withNames(@NotNull String... skullNames) {
        this.names = Arrays.asList(skullNames);
        return this;
    }

    public @NotNull StubConfig withTakeAwayEnabled(boolean enabled) {
        this.takeAwayEnabled = enabled;
        return this;
    }

    public @NotNull StubConfig withTakeAwayType(@NotNull TakeAwayType type) {
        this.takeAwayType = type;
        return this;
    }

    public @NotNull StubConfig withPermissionsEnabled(boolean enabled) {
        this.permissionsEnabled = enabled;
        return this;
    }

    public @NotNull StubConfig withMessagesEnabled(boolean enabled) {
        this.messagesEnabled = enabled;
        return this;
    }

    public @NotNull StubConfig withPrefix(@NotNull String value) {
        this.prefix = value;
        return this;
    }

    public @NotNull StubConfig withPermission(@NotNull PermissionNode node, @NotNull String value) {
        permissions.put(node, value);
        return this;
    }

    public @NotNull StubConfig withMessage(@NotNull MessageKey key, @NotNull String... lines) {
        messages.put(key, Arrays.asList(lines));
        return this;
    }

    @Override
    public boolean isPluginEnabled() {
        return pluginEnabled;
    }

    @Override
    public boolean isListEnabled() {
        return listEnabled;
    }

    @Override
    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    @Override
    public @NotNull List<String> getDisallowedSkullNames() {
        return names;
    }

    @Override
    public boolean isTakeAwayEnabled() {
        return takeAwayEnabled;
    }

    @Override
    public @NotNull TakeAwayType getTakeAwayType() {
        return takeAwayType;
    }

    @Override
    public boolean arePermissionsEnabled() {
        return permissionsEnabled;
    }

    @Override
    public @NotNull String getPermission(@NotNull PermissionNode node) {
        return permissions.getOrDefault(node, node.getDefaultPermission());
    }

    @Override
    public boolean areMessagesEnabled() {
        return messagesEnabled;
    }

    @Override
    public @NotNull String getMessagePrefix() {
        return prefix;
    }

    @Override
    public @NotNull List<String> getMessages(@NotNull MessageKey key) {
        return messages.getOrDefault(key, List.of());
    }

}
