package com.ney.anti_skull.config;

import com.ney.anti_skull.NeyAntiSkull;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ConfigManagerTest {

    private ConfigManager configManager;

    @BeforeEach
    void setUp() throws IOException {

        NeyAntiSkull mockPlugin = mock(NeyAntiSkull.class);
        File tempConfigFile = File.createTempFile("config", ".yml");
        tempConfigFile.deleteOnExit();

        YamlConfiguration testConfig = new YamlConfiguration();
        testConfig.set("settings.enabled", true);
        testConfig.set("settings.take_away", false);
        testConfig.set("settings.list.enabled", true);
        testConfig.set("messages.on_place.enabled", true);

        testConfig.save(tempConfigFile);

        when(mockPlugin.getDataFolder()).thenReturn(tempConfigFile.getParentFile());
        when(mockPlugin.getConfig()).thenReturn(testConfig);

        configManager = new ConfigManager(mockPlugin);

    }

    @Test
    void isSkullBlockingEnabled_returnsTrue() {
        assertThat(configManager.isSkullBlockingEnabled()).isTrue();
    }

    @Test
    void shouldRemoveSkull_returnsFalse() {
        assertThat(configManager.shouldRemoveSkull()).isFalse();
    }

    @Test
    void isListEnabled_returnsTrue() {
        assertThat(configManager.isListEnabled()).isTrue();
    }

    @Test
    void areMessagesEnabled_returnsTrue() {
        assertThat(configManager.areMessagesEnabled()).isTrue();
    }
}