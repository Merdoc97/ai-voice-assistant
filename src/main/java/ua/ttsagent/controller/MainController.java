package ua.ttsagent.controller;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Separator;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
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

    public Pane createUI(Stage stage) {
        ComboBox<String> inputs = DropDownFactory.createDropDown(InputAudioUtil.getInputNames());
        TextArea outputArea = TextAreaFactory.createTextArea("Your transcript and AI response will appear here");
        Label title = new Label("Voice AI Assistant");
        title.getStyleClass().add("app-title");
        Label subtitle = new Label("Record from a selected input device, transcribe speech, and get an AI response.");
        subtitle.getStyleClass().add("app-subtitle");
        Separator separator = new Separator();
        separator.getStyleClass().add("section-separator");

        Button minimizeButton = ButtonFactory.createButton("–", "window-control", e -> stage.setIconified(true));
        minimizeButton.getStyleClass().add("minimize-control");
        Tooltip.install(minimizeButton, new Tooltip("Minimize"));

        Button closeButton = ButtonFactory.createButton("×", "window-control", e -> stage.close());
        closeButton.getStyleClass().add("close-control");
        Tooltip.install(closeButton, new Tooltip("Close"));

        Button maximizeButton = ButtonFactory.createButton("▢", "window-control", e -> stage.setMaximized(!stage.isMaximized()));
        maximizeButton.getStyleClass().add("maximize-control");
        Tooltip.install(maximizeButton, new Tooltip("Maximize / Restore"));

        HBox windowControls = new HBox(8, minimizeButton, maximizeButton, closeButton);
        windowControls.setAlignment(Pos.CENTER_RIGHT);
        windowControls.getStyleClass().add("window-controls");

        HBox titleRow = new HBox(12);
        VBox titleBlock = new VBox(4, title, subtitle);
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        titleRow.getChildren().addAll(titleBlock, spacer, windowControls);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        titleRow.getStyleClass().add("title-row");

        Button startButton = ButtonFactory.createButton("START", "primary", null);
        startButton.setOnAction(createStartEventHandler(inputs, outputArea, startButton));
        Button stopButton = ButtonFactory.createButton("STOP", "secondary", null);
        stopButton.setOnAction(setStopButtonHandler(outputArea, startButton));
        Button clearButton = ButtonFactory.createButton("CLEAR", "ghost", clearButtonHandler(outputArea));

        HBox controls = new HBox(10, inputs, startButton, stopButton, clearButton);
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.getStyleClass().add("controls-row");

        VBox header = new VBox(4, titleRow);
        header.getStyleClass().add("header-block");

        VBox outputBlock = new VBox(outputArea);
        outputBlock.getStyleClass().add("output-block");
        VBox.setVgrow(outputBlock, Priority.ALWAYS);
        outputBlock.setMinHeight(0);

        VBox layout = new VBox(18, header, separator, controls, outputBlock);
        layout.setPadding(new Insets(22));
        layout.getStyleClass().add("main-root");
        VBox.setVgrow(outputBlock, Priority.ALWAYS);
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
                    outputArea.clear();
                    outputArea.appendText("Processing...\r\n");
                    String selected = comboBox.getValue();
                    if (selected != null) {
                        CompletableFuture.runAsync(() -> voiceHandler.startHandleVoice(selected, outputArea))
                                .orTimeout(30, java.util.concurrent.TimeUnit.SECONDS);
                    } else {
                        startButton.setText("START");
                        outputArea.appendText("Input device not selected.\r\n");
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
