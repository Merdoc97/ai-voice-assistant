package ua.ttsagent.audio;

import jakarta.validation.constraints.NotEmpty;
import javafx.scene.control.TextArea;

import java.io.File;
import java.util.function.DoubleConsumer;

public interface VoiceHandler {
    void startHandleVoice(@NotEmpty String input, TextArea outputArea, DoubleConsumer levelListener);
    File stopsHandleVoice(TextArea outputArea);
}
