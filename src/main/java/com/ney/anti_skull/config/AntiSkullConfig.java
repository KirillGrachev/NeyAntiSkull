package com.ney.anti_skull.config;

import com.ney.anti_skull.config.type.TakeAwayType;

import java.util.List;

public interface AntiSkullConfig {

    boolean shouldRemoveSkull();

    boolean isSkullBlockingEnabled();

    boolean isListEnabled();

    boolean areMessagesEnabled();

    boolean arePermissionsEnabled();

    TakeAwayType getTakeAwayType();

    List<String> getDisallowedSkulls();

    List<String> getBlockedMessage();

    String getPermissionPlace();
    String getPermissionTakeAway();

}