package com.ney.anti_skull.config;

import java.util.List;

public interface AntiSkullConfig {

    boolean isSkullBlockingEnabled();
    boolean shouldRemoveSkull();
    boolean isListEnabled();
    boolean areMessagesEnabled();

    String getTakeAwayType();

    List<String> getDisallowedSkulls();
    List<String> getBlockedMessage();

}