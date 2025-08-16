package ua.ttsagent.factory;

import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TextAreaFactory {
    public TextArea createTextArea(String text) {
        TextArea outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setPromptText(text);
        outputArea.setPrefHeight(100);
        VBox.setVgrow(outputArea, Priority.ALWAYS);
        outputArea.setWrapText(true);
        return outputArea;
    }
}
