package ua.ttsagent.factory;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import lombok.experimental.UtilityClass;


@UtilityClass
public class ButtonFactory {

    public Button createButton(String text, Color color,
                               EventHandler<ActionEvent> eventHandler) {
        Button button = new Button(text);
        button.setTextFill(color);
        button.setPrefWidth(100);
        button.setOnAction(eventHandler);
        return button;
    }
}
