package ua.ttsagent.audio;

import jakarta.validation.constraints.NotEmpty;
import javafx.scene.control.TextArea;

import java.io.File;

public interface VoiceHandler {
    void startHandleVoice(@NotEmpty String input);
    File stopsHandleVoice(TextArea outputArea);
}
