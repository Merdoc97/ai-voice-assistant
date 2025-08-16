package ua.ttsagent.ai;

import javafx.scene.control.TextArea;

import java.io.File;

public interface TTSService {

    String ttsRequest(File inputFile, String language, TextArea outputArea);
}
