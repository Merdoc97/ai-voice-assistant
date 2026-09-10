package ua.ttsagent.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.DoubleConsumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class MainController {

    private static final List<String> INTERVAL_VALUES = List.of("----", "5", "10", "15", "20", "25", "30", "35", "40", "45");

    private final VoiceHandler voiceHandler;
    private final TTSService ttsService;
    private final List<Region> waveBars = new ArrayList<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "transcribe-interval");
        thread.setDaemon(true);
        return thread;
    });

    private volatile ScheduledFuture<?> intervalTask;
    private volatile boolean intervalModeActive;

    public Pane createUI(Stage stage) {
        ComboBox<String> inputs = DropDownFactory.createDropDown(InputAudioUtil.getInputNames());
        TextArea outputArea = TextAreaFactory.createTextArea("Your transcript and AI response will appear here");

        CheckBox transcribeOnly = new CheckBox("transcribe only");
        transcribeOnly.getStyleClass().add("transcribe-only-toggle");

        ComboBox<String> intervalCombo = createIntervalCombo();
        Label intervalLabel = new Label("interval seconds");
        VBox intervalRow = new VBox(4, intervalLabel, intervalCombo);
        intervalRow.setAlignment(Pos.CENTER_LEFT);
        intervalRow.setVisible(false);
        intervalRow.setManaged(false);

        transcribeOnly.selectedProperty().addListener((obs, oldValue, selected) -> {
            intervalRow.setVisible(selected);
            intervalRow.setManaged(selected);
        });

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
        HBox.setHgrow(spacer, Priority.ALWAYS);
        titleRow.getChildren().addAll(titleBlock, spacer, windowControls);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        titleRow.getStyleClass().add("title-row");

        Button startButton = ButtonFactory.createButton("START", "primary", null);
        startButton.setOnAction(createStartEventHandler(inputs, outputArea, startButton, transcribeOnly, intervalCombo));
        Button stopButton = ButtonFactory.createButton("STOP", "secondary", null);
        stopButton.setOnAction(setStopButtonHandler(outputArea, startButton, transcribeOnly));
        Button clearButton = ButtonFactory.createButton("CLEAR", "ghost", clearButtonHandler(outputArea));

        VBox checkboxBlock = new VBox(6, transcribeOnly, intervalRow);
        HBox controls = new HBox(10, inputs, checkboxBlock, startButton, stopButton, clearButton);
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.getStyleClass().add("controls-row");

        HBox waveMeter = createWaveMeter();

        VBox header = new VBox(4, titleRow);
        header.getStyleClass().add("header-block");

        VBox outputBlock = new VBox(outputArea);
        outputBlock.getStyleClass().add("output-block");
        VBox.setVgrow(outputBlock, Priority.ALWAYS);
        outputBlock.setMinHeight(0);

        VBox layout = new VBox(18, header, separator, controls, waveMeter, outputBlock);
        layout.setPadding(new Insets(22));
        layout.getStyleClass().add("main-root");
        VBox.setVgrow(outputBlock, Priority.ALWAYS);
        return layout;
    }

    private ComboBox<String> createIntervalCombo() {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getItems().addAll(INTERVAL_VALUES);
        comboBox.setValue("----");
        comboBox.setPromptText("transcribe interval");
        comboBox.setPrefWidth(150);
        comboBox.getStyleClass().add("interval-selector");
        return comboBox;
    }

    private HBox createWaveMeter() {
        waveBars.clear();
        HBox meter = new HBox(3);
        meter.getStyleClass().add("wave-meter");
        meter.setMinHeight(38);
        meter.setPrefHeight(38);
        meter.setMaxHeight(38);
        meter.setAlignment(Pos.CENTER);
        meter.setFillHeight(true);

        for (int i = 0; i < 56; i++) {
            Region bar = new Region();
            bar.getStyleClass().add("wave-bar");
            bar.setMinWidth(0);
            bar.setPrefWidth(0);
            bar.setPrefHeight(10);
            bar.setMaxHeight(32);
            bar.setMinHeight(3);
            HBox.setHgrow(bar, Priority.ALWAYS);
            waveBars.add(bar);
            meter.getChildren().add(bar);
        }
        resetWaveMeter();
        return meter;
    }

    private void updateWaveMeter(double level) {
        double normalized = Math.max(0.0, Math.min(1.0, level));
        int activeBars = (int) Math.round(normalized * waveBars.size());
        for (int i = 0; i < waveBars.size(); i++) {
            Region bar = waveBars.get(i);
            double distance = Math.abs((waveBars.size() / 2.0) - i) / (waveBars.size() / 2.0);
            double heightFactor = Math.max(0.25, normalized * (1.0 - distance * 0.35));
            double height = 4 + (28 * heightFactor);
            bar.setPrefHeight(height);
            bar.setOpacity(i < activeBars ? 1.0 : 0.25);
        }
    }

    private void resetWaveMeter() {
        waveBars.forEach(bar -> {
            bar.setPrefHeight(6);
            bar.setOpacity(0.2);
        });
    }

    private EventHandler<ActionEvent> clearButtonHandler(TextArea outputArea) {
        return e -> outputArea.setText("");
    }

    @SneakyThrows
    private EventHandler<ActionEvent> createStartEventHandler(ComboBox<String> comboBox,
                                                              TextArea outputArea,
                                                              Button startButton,
                                                              CheckBox transcribeOnly,
                                                              ComboBox<String> intervalCombo) {
        return e -> {
            startButton.setText("RECORDING...");
            if (!outputArea.getText().isBlank()) {
                outputArea.appendText("\r\n");
            }
            outputArea.appendText("Processing...\r\n");

            String selectedDevice = comboBox.getValue();
            if (selectedDevice == null) {
                startButton.setText("START");
                outputArea.appendText("Input device not selected.\r\n");
                resetWaveMeter();
                return;
            }

            DoubleConsumer levelListener = level -> Platform.runLater(() -> updateWaveMeter(level));
            if (transcribeOnly.isSelected() && !"----".equals(intervalCombo.getValue())) {
                startIntervalMode(selectedDevice, outputArea, levelListener, Integer.parseInt(intervalCombo.getValue()));
            } else {
                cancelIntervalMode();
                CompletableFuture.runAsync(() -> voiceHandler.startHandleVoice(selectedDevice, outputArea, levelListener))
                        .orTimeout(30, TimeUnit.SECONDS);
            }
        };
    }

    @SneakyThrows
    private EventHandler<ActionEvent> setStopButtonHandler(TextArea outputArea, Button startButton, CheckBox transcribeOnly) {
        log.info("Stop event handler is activated");
        return e -> {
            startButton.setText("START");
            resetWaveMeter();
            cancelIntervalMode();
            CompletableFuture.supplyAsync(() -> voiceHandler.stopsHandleVoice(outputArea))
                    .thenAccept(file -> ttsService.ttsRequest(file, null, outputArea, transcribeOnly.isSelected()))
                    .orTimeout(30, TimeUnit.SECONDS);
        };
    }

    private void startIntervalMode(String deviceName, TextArea outputArea, DoubleConsumer levelListener, int intervalSeconds) {
        cancelIntervalMode();
        intervalModeActive = true;
        CompletableFuture.runAsync(() -> voiceHandler.startHandleVoice(deviceName, outputArea, levelListener))
                .orTimeout(30, TimeUnit.SECONDS);
        intervalTask = scheduler.scheduleAtFixedRate(() -> {
            if (!intervalModeActive) {
                return;
            }

            CompletableFuture.supplyAsync(() -> voiceHandler.stopsHandleVoice(outputArea))
                    .thenAccept(file -> {
                        if (file != null) {
                            ttsService.ttsRequest(file, null, outputArea, true);
                        }
                        if (intervalModeActive) {
                            CompletableFuture.runAsync(() -> voiceHandler.startHandleVoice(deviceName, outputArea, levelListener))
                                    .orTimeout(30, TimeUnit.SECONDS);
                        }
                    })
                    .orTimeout(30, TimeUnit.SECONDS);
        }, intervalSeconds, intervalSeconds, TimeUnit.SECONDS);
    }

    private void cancelIntervalMode() {
        intervalModeActive = false;
        ScheduledFuture<?> task = intervalTask;
        if (task != null) {
            task.cancel(true);
            intervalTask = null;
        }
    }
}
