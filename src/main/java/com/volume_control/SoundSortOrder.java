package com.volume_control;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public enum SoundSortOrder {
    ADDED_OLDEST_FIRST("Added (oldest first)"),
    ADDED_NEWEST_FIRST("Added (newest first)"),
    NAME_ASCENDING("Name (A to Z)"),
    NAME_DESCENDING("Name (Z to A)"),
    AREA_FIRST("Type (Area first)"),
    SOUND_FIRST("Type (Sound first)");

    private final String label;

    SoundSortOrder(String label) {
        this.label = label;
    }

    public List<SoundConfig> sort(List<SoundConfig> configs) {
        // Stored order tracks when sounds were added; sorting only changes the display.
        List<SoundConfig> sorted = new ArrayList<>(configs);
        Comparator<SoundConfig> byName = Comparator.comparing(
                SoundConfig::getName, String.CASE_INSENSITIVE_ORDER);
        Comparator<SoundConfig> byType = Comparator.comparingInt(
                sound -> sound.getSoundType() != null ? sound.getSoundType() : SoundTypes.EFFECT);

        switch (this) {
            case ADDED_NEWEST_FIRST:
                Collections.reverse(sorted);
                break;
            case NAME_ASCENDING:
                sorted.sort(byName);
                break;
            case NAME_DESCENDING:
                sorted.sort(byName.reversed());
                break;
            case AREA_FIRST:
                sorted.sort(byType.reversed().thenComparing(byName));
                break;
            case SOUND_FIRST:
                sorted.sort(byType.thenComparing(byName));
                break;
            case ADDED_OLDEST_FIRST:
                break;
        }

        return sorted;
    }

    @Override
    public String toString() {
        return label;
    }
}
