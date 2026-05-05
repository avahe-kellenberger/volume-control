package com.volume_control;

import com.google.gson.Gson;
import com.google.inject.Provides;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.AreaSoundEffectPlayed;
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
import java.util.Collections;
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

    @Inject
    private Gson gson;

    private NavigationButton navButton;

    @Setter
    private List<SoundConfig> soundConfigs = Collections.emptyList();

    @Provides
    VolumeControlConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(VolumeControlConfig.class);
    }

    @Override
    protected void startUp() throws Exception {
        final SoundConfigPanel panel = new SoundConfigPanel(this, config, configManager, gson);
        final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "/ico.png");
        navButton = NavigationButton.builder()
                .tooltip("Volume Control")
                .icon(icon)
                .priority(5)
                .panel(panel)
                .build();
        clientToolbar.addNavigation(navButton);
        panel.startPanel();
        this.soundConfigs = SoundConfigSerializer.deserialize(this.gson, config.getSoundConfigsJson());
    }

    @Override
    protected void shutDown() throws Exception {
        clientToolbar.removeNavigation(navButton);
    }

    @Subscribe
    public void onSoundEffectPlayed(SoundEffectPlayed event) {
        if (soundConfigs == null || soundConfigs.isEmpty()) {
            return;
        }

        final int soundId = event.getSoundId();
        for (SoundConfig soundConfig : soundConfigs) {
            if (soundConfig.getSoundId() != soundId) {
                continue;
            }

            // Only handle EFFECT type (or null which defaults to EFFECT)
            int soundType = soundConfig.getSoundType() != null ? soundConfig.getSoundType() : SoundTypes.EFFECT;
            if (soundType != SoundTypes.EFFECT) {
                continue;
            }

            event.consume();

            int originalVolume = client.getPreferences().getSoundEffectVolume();
            int configVolume = soundConfig.getVolume();

            // Volume control is dumb so we have to set it in preferences
            client.getPreferences().setSoundEffectVolume(configVolume);
            // Play sound at max volume, capped by the overall preferences volume.
            // 0 is a weird "special" case - it will play at max volume if setSoundEffectVolume(0) is called.
            client.playSoundEffect(soundId, (configVolume == 0) ? 0 : 127);
            client.getPreferences().setSoundEffectVolume(originalVolume);
            break;
        }
    }

    @Subscribe
    public void onAreaSoundEffectPlayed(AreaSoundEffectPlayed event) {
        if (soundConfigs == null || soundConfigs.isEmpty()) {
            return;
        }

        final int soundId = event.getSoundId();
        for (SoundConfig soundConfig : soundConfigs) {
            if (soundConfig.getSoundId() != soundId || soundConfig.getSoundType() != SoundTypes.AREA) {
                continue;
            }

            event.consume();

            int originalVolume = client.getPreferences().getAreaSoundEffectVolume();
            int configVolume = soundConfig.getVolume();

            // TODO: Test this thoroughly, not sure if this is correct.
            // I need a good example sound effect that is easily replicable.
            int volume = (configVolume == 0) ? 0 : 127;
            client.getPreferences().setAreaSoundEffectVolume(volume);
            client.playSoundEffect(soundId, event.getSceneX(), event.getSceneY(), event.getRange(), event.getDelay());
            client.getPreferences().setAreaSoundEffectVolume(originalVolume);
            break;
        }
    }
}

