package ua.ttsagent.controller;

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

import java.util.concurrent.CompletableFuture;


@Slf4j
@Component
@RequiredArgsConstructor
public class MainController {

    private final VoiceHandler voiceHandler;
    private final TTSService ttsService;

    public Pane createUI() {
        ComboBox<String> inputs = DropDownFactory.createDropDown(InputAudioUtil.getInputNames());
        TextArea outputArea = TextAreaFactory.createTextArea("Text to speech output");
        Button startButton = ButtonFactory.createButton("START", Color.RED, null);
        startButton.setOnAction(createStartEventHandler(inputs, outputArea, startButton));
        Button stopButton = ButtonFactory.createButton("STOP", Color.BLUE, null);
        stopButton.setOnAction(setStopButtonHandler(outputArea, startButton));
        Button clearButton = ButtonFactory.createButton("CLEAR", Color.GRAY, clearButtonHandler(outputArea));

        HBox topRow = new HBox(10, inputs, startButton, stopButton, clearButton);
        VBox layout = new VBox(10, topRow, outputArea);
        layout.setPadding(new Insets(15));
        return layout;
    }

    private EventHandler<ActionEvent> clearButtonHandler(TextArea outputArea) {
        return e->{
            outputArea.setText("");
        };
    }


    @SneakyThrows
    private EventHandler<ActionEvent> createStartEventHandler(ComboBox<String> comboBox,
                                                              TextArea outputArea,
                                                              Button startButton) {

        return
                e -> {
                    startButton.setText("RECORDING...");
                    outputArea.appendText("processing...\r\n");
                    String selected = comboBox.getValue();
                    if (selected != null) {
                        CompletableFuture.runAsync(() -> voiceHandler.startHandleVoice(selected))
                                .orTimeout(30, java.util.concurrent.TimeUnit.SECONDS);
                    } else {
                        startButton.setText("START");
                        outputArea.appendText("Input device not selected\r\n");
                    }
                };
    }

    @SneakyThrows
    private EventHandler<ActionEvent> setStopButtonHandler(TextArea outputArea, Button startButton) {
        log.info("Stop event handler is activated");
        return e -> {
            startButton.setText("START");
            CompletableFuture.supplyAsync(() -> voiceHandler.stopsHandleVoice(outputArea))
                    .thenAccept(file -> ttsService.ttsRequest(file, null, outputArea))
                    .orTimeout(30, java.util.concurrent.TimeUnit.SECONDS);
        };
    }
}