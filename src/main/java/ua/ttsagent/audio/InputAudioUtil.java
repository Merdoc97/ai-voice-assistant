package ua.ttsagent.audio;

import lombok.experimental.UtilityClass;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import java.util.Arrays;
import java.util.List;

@UtilityClass
public class InputAudioUtil {

    public List<String> getInputNames() {
        return Arrays.stream(AudioSystem.getMixerInfo())
                .map(Mixer.Info::getName)
                .filter(name -> (!name.contains("Port") && !name.contains("port")))
                .toList();
    }
}
