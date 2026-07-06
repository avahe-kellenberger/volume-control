package com.volume_control;

import com.google.gson.Gson;
import com.google.inject.Provides;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.AreaSoundEffectPlayed;
import net.runelite.api.events.SoundEffectPlayed;
import net.runelite.client.callback.ClientThread;
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
    private ClientThread clientThread;

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

    public void playSound(int soundId, int volume) {
        clientThread.invoke(() -> {
            int originalVolume = client.getPreferences().getSoundEffectVolume();
            // Volume control is dumb so we have to set it in preferences
            client.getPreferences().setSoundEffectVolume(volume);
            // Play sound at max volume, capped by the overall preferences volume.
            // 0 is a weird "special" case - it will play at max volume if setSoundEffectVolume(0) is called.
            client.playSoundEffect(soundId, (volume == 0) ? 0 : 127);
            client.getPreferences().setSoundEffectVolume(originalVolume);
        });
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

            playSound(soundId, soundConfig.getVolume());
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

            int scaledVolume = getScaledVolume(event, soundConfig, soundConfig.getVolume());
            playSound(soundId, scaledVolume);
            break;
        }
    }

    private int getScaledVolume(AreaSoundEffectPlayed event, SoundConfig soundConfig, int configVolume) {
        if (!soundConfig.getPositional()) {
            return configVolume;
        }

        // Calc distance from sound source
        LocalPoint playerLocation = client.getLocalPlayer().getLocalLocation();
        double dist = distance(
                playerLocation.getSceneX(),
                playerLocation.getSceneY(),
                event.getSceneX(),
                event.getSceneY()
        );
        double volumeScale = 1.0 - (Math.min(1.0, dist / event.getRange()));
        return (int) Math.floor(volumeScale * (double) configVolume);
    }

    private static double distance(int x1, int y1, int x2, int y2) {
        long dx = (long) x2 - x1;
        long dy = (long) y2 - y1;
        return Math.sqrt((double) dx * dx + (double) dy * dy);
    }


}