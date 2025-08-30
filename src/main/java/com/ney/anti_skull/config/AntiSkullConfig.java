package com.ney.anti_skull.config;

import com.ney.anti_skull.config.type.TakeAwayType;

import java.util.List;

public interface AntiSkullConfig {

    boolean isSkullBlockingEnabled();

    boolean shouldRemoveSkull();

    boolean isListEnabled();

    boolean areMessagesEnabled();

    boolean arePermissionsEnabled();

    TakeAwayType getTakeAwayType();

    List<String> getDisallowedSkulls();

    List<String> getBlockedMessage();

}