package com.volume_control;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.SoundEffectPlayed;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.util.List;

@Slf4j
@PluginDescriptor(
        name = "Volume Control",
        description = "Control the volume of individual sound effects.",
        tags = {"volume", "sound", "effect", "sfx"}
)
public class VolumeControl extends Plugin {
    @Inject
    private Client client;

    @Inject
    private ClientToolbar clientToolbar;

    @Inject
    private ConfigManager configManager;

    @Inject
    private VolumeControlConfig config;

    private NavigationButton navButton;

    @Provides
    VolumeControlConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(VolumeControlConfig.class);
    }

    @Override
    protected void startUp() throws Exception {
        final SoundConfigPanel panel = new SoundConfigPanel(client, this, config, configManager);
        final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "/ico.png");
        navButton = NavigationButton.builder()
                .tooltip("Volume Control")
                .icon(icon)
                .priority(5)
                .panel(panel)
                .build();
        clientToolbar.addNavigation(navButton);
        panel.startPanel();
    }

    @Override
    protected void shutDown() throws Exception {
        clientToolbar.removeNavigation(navButton);
    }

    @Subscribe
    public void onSoundEffectPlayed(SoundEffectPlayed soundEffectPlayed) {
        final List<SoundConfig> soundConfigs = SoundConfigSerializer.deserialize(config.getSoundConfigsJson());
        if (soundConfigs == null || soundConfigs.isEmpty()) {
            return;
        }

        final int soundId = soundEffectPlayed.getSoundId();
        for (SoundConfig soundConfig : soundConfigs) {
            if (soundConfig.getSoundId() != soundId) {
                continue;
            }
            soundEffectPlayed.consume();

            int configVolume = soundConfig.getVolume();
            int originalVolume = client.getPreferences().getSoundEffectVolume();

            // Volume control is dumb so we have to set it in preferences
            client.getPreferences().setSoundEffectVolume(configVolume);
            client.playSoundEffect(soundId, configVolume);
            client.getPreferences().setSoundEffectVolume(originalVolume);
            break;
        }
    }
}

