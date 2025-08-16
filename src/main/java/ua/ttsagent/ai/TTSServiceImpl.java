package ua.ttsagent.ai;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.core.io.FileUrlResource;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@Slf4j
@RequiredArgsConstructor
class TTSServiceImpl implements TTSService {
    private final OpenAiAudioTranscriptionModel transcriptionModel;
    private final ChatModel chatModel;

    @Override
    public String ttsRequest(File inputFile, String language, TextArea outputArea) {
        log.info("TTS request for file {} in language {}", inputFile, language);
        var textFromAudio = recognizeSpeech(inputFile);
        Platform.runLater(() -> outputArea.setText("The question is: " + textFromAudio));
        return answerToQuestion(textFromAudio, language);
    }

    @SneakyThrows
    private String recognizeSpeech(File inputFile) {
        log.info("Recognizing speech from file {}", inputFile);
        AudioTranscriptionPrompt prompt = new AudioTranscriptionPrompt(new FileUrlResource(inputFile.toURL()), OpenAiAudioTranscriptionOptions.builder()
                .build());
        var result = transcriptionModel.call(prompt).getResult();
        log.info("Transcription result: {}", result.getOutput());
        return result.getOutput();
    }

    private String answerToQuestion(String question, String language) {
        log.info("Answering to question: {} ", question, language);
        return chatModel.call(question);
    }
}
