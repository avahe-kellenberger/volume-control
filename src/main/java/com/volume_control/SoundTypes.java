package com.volume_control;

public final class SoundTypes {
    public static final int EFFECT = 0; // null should also default to EFFECT
    public static final int AREA = 1;

    private SoundTypes() {
    }

    public static String getName(Integer soundType) {
        if (soundType == null || soundType == EFFECT) {
            return "sound";
        } else if (soundType == AREA) {
            return "area";
        }
        throw new Error("Invalid sound name");
    }
}
