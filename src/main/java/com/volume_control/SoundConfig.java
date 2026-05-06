package com.volume_control;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SoundConfig {
    private int soundId;
    private String name;
    private int volume;
    private Integer soundType;
    private Boolean positional; // If area sounds should be played positionally
}

