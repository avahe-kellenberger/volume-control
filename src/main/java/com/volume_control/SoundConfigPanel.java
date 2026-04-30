package com.volume_control;

import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.ui.PluginPanel;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class SoundConfigPanel extends PluginPanel {

    private final VolumeControlConfig config;
    private final ConfigManager configManager;

    private JTextField nameField;
    private JTextField soundIdField;
    private JSlider volumeSlider;
    private JLabel volumeValueLabel;
    private JPanel soundList;
    private JButton submitButton;
    private SoundConfig editingConfig = null;

    private final int defaultVolume = 10;

    public SoundConfigPanel(Client client, Plugin plugin, VolumeControlConfig config, ConfigManager configManager) {
        this.config = config;
        this.configManager = configManager;
    }

    public void startPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("Add Sound Configuration");
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);
        add(titleLabel);
        add(Box.createVerticalStrut(10));

        nameField = new JTextField();
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        nameField.setAlignmentX(LEFT_ALIGNMENT);
        add(new JLabel("Sound Name:"));
        add(nameField);
        add(Box.createVerticalStrut(8));

        soundIdField = new JTextField();
        soundIdField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        soundIdField.setAlignmentX(LEFT_ALIGNMENT);
        add(new JLabel("Sound ID:"));
        add(soundIdField);
        add(Box.createVerticalStrut(8));

        volumeSlider = new JSlider(0, 127, defaultVolume);
        volumeValueLabel = new JLabel(String.valueOf(defaultVolume));
        volumeSlider.addChangeListener(e -> volumeValueLabel.setText(String.valueOf(volumeSlider.getValue())));

        JPanel volumePanel = new JPanel();
        volumePanel.setLayout(new BoxLayout(volumePanel, BoxLayout.X_AXIS));
        volumePanel.setAlignmentX(LEFT_ALIGNMENT);
        volumePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        volumePanel.add(new JLabel("Volume:"));
        volumePanel.add(Box.createHorizontalStrut(10));
        volumePanel.add(volumeSlider);
        volumePanel.add(Box.createHorizontalStrut(10));
        volumePanel.add(volumeValueLabel);
        add(volumePanel);
        add(Box.createVerticalStrut(10));

        submitButton = new JButton("Submit");
        submitButton.setAlignmentX(LEFT_ALIGNMENT);
        submitButton.setMaximumSize(new Dimension(100, 35));
        submitButton.addActionListener(e -> saveSoundConfig());
        add(submitButton);
        add(Box.createVerticalStrut(15));

        JLabel savedLabel = new JLabel("Saved Sounds");
        savedLabel.setAlignmentX(LEFT_ALIGNMENT);
        add(savedLabel);
        add(Box.createVerticalStrut(5));

        soundList = new JPanel();
        soundList.setLayout(new BoxLayout(soundList, BoxLayout.Y_AXIS));
        soundList.setAlignmentX(LEFT_ALIGNMENT);

        JScrollPane scrollPane = new JScrollPane(soundList);
        scrollPane.setPreferredSize(new Dimension(Integer.MAX_VALUE, 200));
        scrollPane.setAlignmentX(LEFT_ALIGNMENT);
        scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        add(scrollPane);

        updateSoundList();

        setFocusTraversalPolicy(new FocusTraversalPolicy() {
            @Override
            public Component getComponentAfter(Container container, Component component) {
                if (component == nameField) return soundIdField;
                if (component == soundIdField) return submitButton;
                return nameField;
            }

            @Override
            public Component getComponentBefore(Container container, Component component) {
                if (component == nameField) return submitButton;
                if (component == soundIdField) return nameField;
                if (component == submitButton) return soundIdField;
                return submitButton;
            }

            @Override
            public Component getFirstComponent(Container container) {
                return nameField;
            }

            @Override
            public Component getLastComponent(Container container) {
                return submitButton;
            }

            @Override
            public Component getDefaultComponent(Container container) {
                return nameField;
            }
        });
        setFocusCycleRoot(true);
    }

    private void saveSoundConfig() {
        if (config == null) {
            return;
        }

        try {
            String soundIdText = soundIdField.getText().trim();
            String name = nameField.getText().trim();

            if (soundIdText.isEmpty() || name.isEmpty()) {
                return;
            }

            int soundId = Integer.parseInt(soundIdText);
            int volume = volumeSlider.getValue();

            SoundConfig newConfig = new SoundConfig(soundId, name, volume);

            List<SoundConfig> existing = getSoundConfigs();
            List<SoundConfig> configs = new ArrayList<>((existing != null) ? existing : Collections.emptyList());

            if (editingConfig != null) {
                // Replace the old entry with the same ID when editing
                configs.removeIf(c -> c.getSoundId() == editingConfig.getSoundId());
                editingConfig = null;
            }

            configs.add(newConfig);
            setSoundConfigs(configs);

            soundIdField.setText("");
            nameField.setText("");
            volumeSlider.setValue(defaultVolume);

            updateSoundList();
        } catch (NumberFormatException ignored) {
        }
    }

    private void updateSoundList() {
        soundList.removeAll();

        if (config == null) {
            soundList.revalidate();
            soundList.repaint();
            return;
        }

        List<SoundConfig> soundConfigs = getSoundConfigs();
        if (soundConfigs == null || soundConfigs.isEmpty()) {
            JLabel emptyLabel = new JLabel("No sounds configured");
            emptyLabel.setAlignmentX(LEFT_ALIGNMENT);
            soundList.add(emptyLabel);
        } else {
            for (SoundConfig soundConfig : soundConfigs) {
                JPanel itemPanel = new JPanel();
                itemPanel.setLayout(new BoxLayout(itemPanel, BoxLayout.Y_AXIS));
                itemPanel.setAlignmentX(LEFT_ALIGNMENT);
                itemPanel.setPreferredSize(new Dimension(220, 65));
                itemPanel.setMaximumSize(new Dimension(220, 65));
                itemPanel.setBackground(new Color(40, 40, 40));
                itemPanel.setOpaque(true);
                itemPanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.DARK_GRAY, 1),
                        BorderFactory.createEmptyBorder(4, 6, 4, 6)
                ));

                final int nameMaxLength = 34;
                String displayName = soundConfig.getName();
                if (displayName.length() > nameMaxLength) {
                    displayName = displayName.substring(0, nameMaxLength - 3) + "...";
                }
                JLabel nameLabel = new JLabel(displayName);
                nameLabel.setForeground(Color.WHITE);
                nameLabel.setAlignmentX(LEFT_ALIGNMENT);
                itemPanel.add(nameLabel);

                JPanel detailsPanel = new JPanel();
                detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.X_AXIS));
                detailsPanel.setOpaque(false);
                detailsPanel.setAlignmentX(LEFT_ALIGNMENT);
                detailsPanel.setMaximumSize(new Dimension(220, 16));

                JLabel idLabel = new JLabel("ID: " + soundConfig.getSoundId());
                idLabel.setForeground(Color.LIGHT_GRAY);
                idLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));

                JLabel volLabel = new JLabel("Vol: " + soundConfig.getVolume());
                volLabel.setForeground(Color.LIGHT_GRAY);
                volLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));

                detailsPanel.add(idLabel);
                detailsPanel.add(Box.createHorizontalStrut(10));
                detailsPanel.add(volLabel);
                itemPanel.add(detailsPanel);

                JPanel buttonPanel = new JPanel();
                buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
                buttonPanel.setOpaque(false);
                buttonPanel.setAlignmentX(LEFT_ALIGNMENT);
                buttonPanel.setMaximumSize(new Dimension(220, 22));

                JButton editButton = new JButton("Edit");
                editButton.setPreferredSize(new Dimension(55, 20));
                editButton.setMaximumSize(new Dimension(55, 20));
                editButton.addActionListener(e -> editSoundConfig(soundConfig));

                JButton deleteButton = new JButton("Delete");
                deleteButton.setPreferredSize(new Dimension(70, 20));
                deleteButton.setMaximumSize(new Dimension(70, 20));
                deleteButton.addActionListener(e -> deleteSoundConfig(soundConfig));

                buttonPanel.add(editButton);
                buttonPanel.add(Box.createHorizontalStrut(3));
                buttonPanel.add(deleteButton);
                itemPanel.add(buttonPanel);

                soundList.add(itemPanel);
                soundList.add(Box.createVerticalStrut(5));
            }
        }

        soundList.revalidate();
        soundList.repaint();
    }

    private void deleteSoundConfig(SoundConfig soundConfig) {
        List<SoundConfig> existing = getSoundConfigs();
        List<SoundConfig> configs = new ArrayList<>((existing != null) ? existing : Collections.emptyList());
        configs.removeIf(c -> c.getSoundId() == soundConfig.getSoundId());
        setSoundConfigs(configs);
        updateSoundList();
    }

    private void editSoundConfig(SoundConfig soundConfig) {
        editingConfig = soundConfig;
        nameField.setText(soundConfig.getName());
        soundIdField.setText(String.valueOf(soundConfig.getSoundId()));
        volumeSlider.setValue(soundConfig.getVolume());
        volumeValueLabel.setText(String.valueOf(soundConfig.getVolume()));
    }

    private List<SoundConfig> getSoundConfigs() {
        return SoundConfigSerializer.deserialize(config.getSoundConfigsJson());
    }

    private void setSoundConfigs(List<SoundConfig> soundConfigs) {
        // Dedupe by sound ID
        Set<Integer> seenIds = new HashSet<>();
        List<SoundConfig> deduplicated = new ArrayList<>();

        if (soundConfigs != null) {
            for (SoundConfig cfg : soundConfigs) {
                int id = cfg.getSoundId();
                if (!seenIds.contains(id)) {
                    seenIds.add(id);
                    deduplicated.add(cfg);
                }
            }
        }

        String json = SoundConfigSerializer.serialize(deduplicated);
        config.setSoundConfigsJson(json);
        configManager.setConfiguration("soundModifier", "soundConfigs", json);
    }
}
