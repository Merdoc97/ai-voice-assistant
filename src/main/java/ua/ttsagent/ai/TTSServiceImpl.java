package ua.ttsagent.ai;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileUrlResource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
class TTSServiceImpl implements TTSService {
    private final OpenAiAudioTranscriptionModel transcriptionModel;
    private final ChatModel chatModel;
    @Value("${ai.promtText:\"<question>\"}")
    private String promptText;
    @Override
    public void ttsRequest(File inputFile, String language, TextArea outputArea) {
        if (inputFile == null) {
            outputArea.setText("File is null please try again later.");
            return;
        }
        log.info("TTS request for file {} in language {}", inputFile, language);
        var textFromAudio = recognizeSpeech(inputFile);
        log.info("Prompt text is: {}",promptText);
        Platform.runLater(() -> outputArea.appendText("---------------------\r\n"));
        Platform.runLater(() -> outputArea.appendText("The question is: " + textFromAudio+"\r\n"));
        Platform.runLater(() -> outputArea.appendText("The answer is: \r\n"));
        var template = PromptTemplate.builder()
                .renderer(StTemplateRenderer.builder()
                        .startDelimiterToken('<')
                        .endDelimiterToken('>')
                        .build())
                .template(promptText)
                .build();
        chatModel.stream(template.render(Map.of("question",textFromAudio)))
                .doOnNext(text -> Platform.runLater(() -> outputArea.appendText(text)))
                .then().block();
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
}
