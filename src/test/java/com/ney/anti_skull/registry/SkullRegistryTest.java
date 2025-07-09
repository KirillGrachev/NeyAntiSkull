package com.ney.anti_skull.registry;

import com.ney.anti_skull.config.ConfigManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class SkullRegistryTest {

    private ConfigManager mockConfigManager;
    private SkullRegistry skullRegistry;

    @BeforeEach
    void setUp() {

        mockConfigManager = Mockito.mock(ConfigManager.class);
        when(mockConfigManager.getDisallowedSkulls()).thenReturn(List.of("head1", "head2"));

        skullRegistry = new SkullRegistry(mockConfigManager);

    }

    @Test
    void isSkullRegistered_returnsTrueForExistingName() {
        assertThat(skullRegistry.isSkullRegistered("head1")).isTrue();
    }

    @Test
    void isSkullRegistered_returnsFalseForUnknownName() {
        assertThat(skullRegistry.isSkullRegistered("head3")).isFalse();
    }

    @Test
    void reloadRegistry_clearsAndReinitializes() {

        when(mockConfigManager.getDisallowedSkulls()).thenReturn(List.of("head3", "head4"));

        skullRegistry.reloadRegistry();

        assertThat(skullRegistry.isSkullRegistered("head3")).isTrue();
        assertThat(skullRegistry.isSkullRegistered("head1")).isFalse();

    }
}