package com.volume_control;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SoundConfigSerializer {
    private static final Gson GSON = new Gson();
    private static final Type SOUND_CONFIG_TYPE = new TypeToken<List<SoundConfig>>() {
    }.getType();

    private SoundConfigSerializer() {
    }

    public static List<SoundConfig> deserialize(String json) {
        try {
            if (json == null || json.isEmpty()) {
                return new ArrayList<>();
            }
            return GSON.fromJson(json, SOUND_CONFIG_TYPE);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public static String serialize(List<SoundConfig> configs) {
        return GSON.toJson(configs, SOUND_CONFIG_TYPE);
    }
}
