package ua.ttsagent.factory;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import lombok.experimental.UtilityClass;


@UtilityClass
public class ButtonFactory {

    public Button createButton(String text, String styleClass,
                               EventHandler<ActionEvent> eventHandler) {
        Button button = new Button(text);
        button.getStyleClass().addAll("action-button", styleClass);
        button.setPrefWidth(130);
        button.setMinHeight(38);
        button.setOnAction(eventHandler);
        return button;
    }
}
