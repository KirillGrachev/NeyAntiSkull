package com.ney.anti_skull.config;

import java.util.List;

public interface AntiSkullConfig {

    boolean isSkullBlockingEnabled();
    boolean shouldRemoveSkull();
    boolean isListEnabled();
    boolean areMessagesEnabled();

    List<String> getDisallowedSkulls();
    List<String> getBlockedMessage();

}