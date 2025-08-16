package ua.ttsagent.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ua.ttsagent.ai.TTSService;
import ua.ttsagent.audio.InputAudioUtil;
import ua.ttsagent.audio.VoiceHandler;
import ua.ttsagent.factory.ButtonFactory;
import ua.ttsagent.factory.DropDownFactory;
import ua.ttsagent.factory.TextAreaFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;


@Slf4j
@Component
@RequiredArgsConstructor
public class MainController {

    private final VoiceHandler voiceHandler;
    private final TTSService ttsService;

    public Pane createUI() {
        ComboBox<String> inputs = DropDownFactory.createDropDown(InputAudioUtil.getInputNames());
        ComboBox<String> language = DropDownFactory.createDropDown(List.of("RU", "EN"));
        TextArea outputArea = TextAreaFactory.createTextArea("Text to speech output");
        Button startButton = ButtonFactory.createButton("START", Color.RED, createStartEventHandler(inputs, outputArea, language));
        Button stopButton = ButtonFactory.createButton("STOP", Color.BLUE, setStopButtonHandler(outputArea));

        HBox topRow = new HBox(10, inputs, language, startButton, stopButton);
        VBox layout = new VBox(10, topRow, outputArea);
        layout.setPadding(new Insets(15));
        return layout;
    }


    @SneakyThrows
    private EventHandler<ActionEvent> createStartEventHandler(ComboBox<String> comboBox,
                                                              TextArea outputArea,
                                                              ComboBox<String> language) {
        log.info("Start event handler is activated");
        Platform.runLater(()->outputArea.setText("processing..."));
        return
                e -> {
                    String selected = comboBox.getValue();
                    String languageValue = language.getValue();
                    if (selected != null) {
                        CompletableFuture.supplyAsync(() -> voiceHandler.startHandleVoice(selected))
                                .thenApply(file -> ttsService.ttsRequest(file,languageValue,outputArea))
                                .thenAccept(res -> Platform.runLater(()->outputArea.setText(res.toString())))
                                .join();
                    } else {
                        outputArea.setText("Input device not selected");
                    }
                };
    }

    @SneakyThrows
    private EventHandler<ActionEvent> setStopButtonHandler(TextArea outputArea) {

        log.info("Stop event handler is activated");
        return e -> outputArea.setText("processed file ");
    }
}