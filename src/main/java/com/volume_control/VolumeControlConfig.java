package com.volume_control;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("soundModifier")
public interface VolumeControlConfig extends Config {
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
}
