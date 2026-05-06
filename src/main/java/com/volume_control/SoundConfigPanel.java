package com.volume_control;

import com.google.gson.Gson;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.PluginPanel;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.*;
import java.util.List;

public class SoundConfigPanel extends PluginPanel {

    private final VolumeControl plugin;
    private final VolumeControlConfig config;
    private final ConfigManager configManager;
    private final Gson gson;

    private JTextField nameField;
    private JSpinner soundIdSpinner;
    private ButtonGroup soundTypeGroup;
    private JCheckBox positionalCheckbox;
    private JSlider volumeSlider;
    private JSpinner volumeSpinner;
    private JLabel volumeValueLabel;
    private JPanel soundList;
    private JButton submitButton;
    private SoundConfig editingConfig = null;

    private final int defaultVolume = 63;

    public SoundConfigPanel(
            VolumeControl plugin,
            VolumeControlConfig config,
            ConfigManager configManager,
            Gson gson
    ) {
        this.plugin = plugin;
        this.config = config;
        this.configManager = configManager;
        this.gson = gson;
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
        add(new JLabel("Sound Name: *"));
        add(nameField);
        add(Box.createVerticalStrut(8));

        nameField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                updateSubmitButtonState();
            }

            public void removeUpdate(DocumentEvent e) {
                updateSubmitButtonState();
            }

            public void changedUpdate(DocumentEvent e) {
                updateSubmitButtonState();
            }
        });

        soundIdSpinner = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
        soundIdSpinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        soundIdSpinner.setAlignmentX(LEFT_ALIGNMENT);
        add(new JLabel("Sound ID: *"));
        add(soundIdSpinner);
        add(Box.createVerticalStrut(8));

        JFormattedTextField soundIdTextField = ((JSpinner.NumberEditor) soundIdSpinner.getEditor()).getTextField();
        soundIdTextField.setHorizontalAlignment(JTextField.LEFT);
        soundIdTextField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                SwingUtilities.invokeLater(soundIdTextField::selectAll);
            }
        });

        soundIdSpinner.addChangeListener(e -> updateSubmitButtonState());

        soundTypeGroup = new ButtonGroup();
        JPanel typePanel = new JPanel();
        typePanel.setLayout(new BoxLayout(typePanel, BoxLayout.X_AXIS));
        typePanel.setAlignmentX(LEFT_ALIGNMENT);
        typePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));

        JRadioButton effectButton = new JRadioButton("Sound", true);
        effectButton.setActionCommand(String.valueOf(SoundTypes.EFFECT));
        JRadioButton areaButton = new JRadioButton("Area");
        areaButton.setActionCommand(String.valueOf(SoundTypes.AREA));

        soundTypeGroup.add(effectButton);
        soundTypeGroup.add(areaButton);

        typePanel.add(new JLabel("Type:"));
        typePanel.add(Box.createHorizontalStrut(10));
        typePanel.add(effectButton);
        typePanel.add(Box.createHorizontalStrut(15));
        typePanel.add(areaButton);
        typePanel.add(Box.createHorizontalGlue());
        add(typePanel);
        add(Box.createVerticalStrut(8));

        positionalCheckbox = new JCheckBox("Positional audio");
        positionalCheckbox.setAlignmentX(LEFT_ALIGNMENT);
        positionalCheckbox.setEnabled(false);
        positionalCheckbox.setSelected(false);
        add(positionalCheckbox);
        add(Box.createVerticalStrut(8));

        effectButton.addActionListener(e -> {
            positionalCheckbox.setEnabled(false);
            positionalCheckbox.setSelected(false);
        });

        areaButton.addActionListener(e -> {
            positionalCheckbox.setEnabled(true);
            positionalCheckbox.setSelected(true);
        });

        volumeSlider = new JSlider(0, 127, defaultVolume);
        volumeSpinner = new JSpinner(new SpinnerNumberModel(defaultVolume, 0, 127, 1));
        volumeValueLabel = new JLabel(String.valueOf(defaultVolume));

        volumeSpinner.setPreferredSize(new Dimension(60, 25));
        volumeSpinner.setMaximumSize(new Dimension(60, 25));

        JFormattedTextField volumeTextField = ((JSpinner.NumberEditor) volumeSpinner.getEditor()).getTextField();
        volumeTextField.setHorizontalAlignment(JTextField.LEFT);
        volumeTextField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                volumeTextField.selectAll();
            }

            @Override
            public void focusLost(FocusEvent e) {
                try {
                    int value = Integer.parseInt(volumeTextField.getText());
                    value = Math.max(0, Math.min(127, value));
                    volumeSpinner.setValue(value);
                    volumeSlider.setValue(value);
                } catch (NumberFormatException ignored) {
                    volumeSpinner.setValue(defaultVolume);
                    volumeSlider.setValue(defaultVolume);
                }
            }
        });

        volumeSlider.addChangeListener(e -> {
            int value = volumeSlider.getValue();
            volumeValueLabel.setText(String.valueOf(value));
            volumeSpinner.setValue(value);
        });

        volumeSpinner.addChangeListener(e -> {
            int value = ((Number) volumeSpinner.getValue()).intValue();
            volumeSlider.setValue(value);
        });

        JPanel volumePanel = new JPanel();
        volumePanel.setLayout(new BoxLayout(volumePanel, BoxLayout.X_AXIS));
        volumePanel.setAlignmentX(LEFT_ALIGNMENT);
        volumePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        volumePanel.add(new JLabel("Volume:"));
        volumePanel.add(Box.createHorizontalStrut(10));
        volumePanel.add(volumeSlider);
        volumePanel.add(Box.createHorizontalStrut(8));
        volumePanel.add(volumeSpinner);
        volumePanel.add(Box.createHorizontalGlue());
        add(volumePanel);
        add(Box.createVerticalStrut(10));

        submitButton = new JButton("Submit");
        submitButton.setAlignmentX(LEFT_ALIGNMENT);
        submitButton.setMaximumSize(new Dimension(100, 35));
        submitButton.setEnabled(false);
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
        scrollPane.setAlignmentX(LEFT_ALIGNMENT);
        scrollPane.setPreferredSize(new Dimension(Integer.MAX_VALUE, 632));
        scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 632));
        add(scrollPane, "grow");

        updateSoundList();

        setFocusTraversalPolicy(new FocusTraversalPolicy() {
            @Override
            public Component getComponentAfter(Container container, Component component) {
                if (component == nameField) return soundIdSpinner;
                if (component == soundIdSpinner) return submitButton;
                return nameField;
            }

            @Override
            public Component getComponentBefore(Container container, Component component) {
                if (component == nameField) return submitButton;
                if (component == soundIdSpinner) return nameField;
                if (component == submitButton) return soundIdSpinner;
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
            String name = nameField.getText().trim();
            int soundId = ((Number) soundIdSpinner.getValue()).intValue();

            if (soundId <= 0 || name.isEmpty()) {
                return;
            }

            int soundType = Integer.parseInt(soundTypeGroup.getSelection().getActionCommand());
            SoundConfig newConfig = new SoundConfig(
                    soundId,
                    name,
                    volumeSlider.getValue(),
                    soundType,
                    // Only save positional for area sounds, null for effect sounds
                    (soundType == SoundTypes.AREA) ? positionalCheckbox.isSelected() : null
            );

            List<SoundConfig> existing = getSoundConfigs();
            List<SoundConfig> configs = new ArrayList<>((existing != null) ? existing : Collections.emptyList());

            if (editingConfig != null) {
                // Replace the old entry with the same ID and soundType when editing
                int editingSoundType = editingConfig.getSoundType() != null ? editingConfig.getSoundType() : SoundTypes.EFFECT;
                configs.removeIf(
                        c -> c.getSoundId() == editingConfig.getSoundId() &&
                                (c.getSoundType() != null ? c.getSoundType() : SoundTypes.EFFECT) == editingSoundType
                );
                editingConfig = null;
            }

            configs.add(newConfig);
            setSoundConfigs(configs);

            soundIdSpinner.setValue(0);
            nameField.setText("");
            volumeSlider.setValue(defaultVolume);
            volumeSpinner.setValue(defaultVolume);
            positionalCheckbox.setSelected(false);
            positionalCheckbox.setEnabled(false);
            soundTypeGroup.clearSelection();
            soundTypeGroup.getElements().nextElement().setSelected(true);

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

                JLabel typeLabel = new JLabel("Type: " + SoundTypes.getName(soundConfig.getSoundType()));
                typeLabel.setForeground(Color.LIGHT_GRAY);
                typeLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));

                detailsPanel.add(idLabel);
                detailsPanel.add(Box.createHorizontalStrut(10));
                detailsPanel.add(volLabel);
                detailsPanel.add(Box.createHorizontalStrut(10));
                detailsPanel.add(typeLabel);
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
        // Force parent scroll pane to update its layout
        SwingUtilities.invokeLater(() -> {
            Container parent = soundList.getParent();
            if (parent != null) {
                parent.revalidate();
                parent.repaint();
            }
        });
    }

    private void updateSubmitButtonState() {
        boolean hasValidSoundName = !nameField.getText().trim().isEmpty();
        boolean hasValidSoundId = ((Number) soundIdSpinner.getValue()).intValue() > 0;
        submitButton.setEnabled(hasValidSoundName && hasValidSoundId);
    }

    private void deleteSoundConfig(SoundConfig soundConfig) {
        List<SoundConfig> existing = getSoundConfigs();
        List<SoundConfig> configs = new ArrayList<>((existing != null) ? existing : Collections.emptyList());
        configs.removeIf(c -> c.getSoundId() == soundConfig.getSoundId() &&
                ((c.getSoundType() != null ? c.getSoundType() : SoundTypes.EFFECT) ==
                        (soundConfig.getSoundType() != null ? soundConfig.getSoundType() : SoundTypes.EFFECT)));
        setSoundConfigs(configs);
        updateSoundList();
    }

    private void editSoundConfig(SoundConfig soundConfig) {
        editingConfig = soundConfig;
        nameField.setText(soundConfig.getName());
        soundIdSpinner.setValue(soundConfig.getSoundId());
        volumeSlider.setValue(soundConfig.getVolume());
        volumeSpinner.setValue(soundConfig.getVolume());

        int soundType = soundConfig.getSoundType() != null ? soundConfig.getSoundType() : SoundTypes.EFFECT;
        for (AbstractButton button : Collections.list(soundTypeGroup.getElements())) {
            if (button.getActionCommand().equals(String.valueOf(soundType))) {
                button.setSelected(true);
                break;
            }
        }

        // Set positional checkbox state based on sound type
        if (soundType == SoundTypes.AREA) {
            positionalCheckbox.setEnabled(true);
            positionalCheckbox.setSelected(soundConfig.getPositional() != null ? soundConfig.getPositional() : true);
        } else {
            positionalCheckbox.setEnabled(false);
            positionalCheckbox.setSelected(false);
        }
    }

    private List<SoundConfig> getSoundConfigs() {
        return SoundConfigSerializer.deserialize(this.gson, config.getSoundConfigsJson());
    }

    private void setSoundConfigs(List<SoundConfig> soundConfigs) {
        // Dedupe by sound ID and soundType combination
        Set<String> seenKeys = new HashSet<>();
        List<SoundConfig> deduplicated = new ArrayList<>();

        if (soundConfigs != null) {
            for (SoundConfig cfg : soundConfigs) {
                int soundType = cfg.getSoundType() != null ? cfg.getSoundType() : SoundTypes.EFFECT;
                String key = cfg.getSoundId() + ":" + soundType;
                if (seenKeys.add(key)) {
                    deduplicated.add(cfg);
                }
            }
        }

        String json = SoundConfigSerializer.serialize(this.gson, deduplicated);
        config.setSoundConfigsJson(json);
        configManager.setConfiguration("soundModifier", "soundConfigs", json);
        this.plugin.setSoundConfigs(deduplicated);
    }
}
