package ua.ttsagent.audio;

import jakarta.validation.constraints.NotEmpty;

import java.io.File;

public interface VoiceHandler {
    File startHandleVoice(@NotEmpty String input);
}
