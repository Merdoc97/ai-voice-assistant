package ua.ttsagent.controller;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ua.ttsagent.factory.ButtonFactory;
import ua.ttsagent.factory.DropDownFactory;

import java.util.Collection;
import java.util.List;


@Slf4j
@Component
@NoArgsConstructor
public class MainController {

    public Pane createUI() {
        ComboBox<String> inputs = DropDownFactory.createDropDown(List.of("OPTION1","OPTION2"));
        ComboBox<String> language = DropDownFactory.createDropDown(List.of("RU","EN"));
        TextArea outputArea = createTextArea();
        Button startButton = ButtonFactory.createButton("START", Color.RED, createStartEventHandler(inputs, outputArea));
        Button stopButton = ButtonFactory.createButton("STOP", Color.BLUE, setStopButtonHandler(outputArea));

        HBox topRow = new HBox(10, inputs,language, startButton, stopButton);
        VBox layout = new VBox(10, topRow, outputArea);
        layout.setPadding(new Insets(15));

        return layout;
    }


    private TextArea createTextArea() {
        TextArea outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setPromptText("TEXT output");
        outputArea.setPrefHeight(100);
        VBox.setVgrow(outputArea, Priority.ALWAYS);
        outputArea.setWrapText(true);
        return outputArea;
    }

    private EventHandler<ActionEvent> createStartEventHandler(ComboBox<String> comboBox,
                                                              TextArea outputArea) {
        return
                e -> {
                    String selected = comboBox.getValue();
                    if (selected != null) {
                        outputArea.setText(selected);
                    } else {
                        outputArea.setText("No selection");
                    }
                };
    }

    private EventHandler<ActionEvent> setStopButtonHandler(TextArea outputArea) {
        return e -> outputArea.setText("Stop is activated");
    }
}