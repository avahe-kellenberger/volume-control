package com.volume_control;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("soundModifier")
public interface VolumeControlConfig extends Config {
    @ConfigItem(
            keyName = "hideSidePanelButton",
            name = "Hide side panel button",
            description = "Hide the Volume Control button in the sidebar",
            position = 0
    )
    default boolean hideSidePanelButton() {
        return false;
    }

    @ConfigItem(
            keyName = "soundConfigs",
            name = "Sound Configurations",
            description = "List of custom sound configurations",
            hidden = true
    )
    default String getSoundConfigsJson() {
        return "[]";
    }

    @ConfigItem(
            keyName = "soundConfigs",
            name = "",
            description = "",
            hidden = true
    )
    void setSoundConfigsJson(String json);

    @ConfigItem(
            keyName = "soundSortOrder",
            name = "Sound Sort Order",
            description = "Sort order for the saved sounds list",
            hidden = true
    )
    default SoundSortOrder getSoundSortOrder() {
        return SoundSortOrder.ADDED_OLDEST_FIRST;
    }

    @ConfigItem(
            keyName = "soundSortOrder",
            name = "",
            description = "",
            hidden = true
    )
    void setSoundSortOrder(SoundSortOrder sortOrder);
}
