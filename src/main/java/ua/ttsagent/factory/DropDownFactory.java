package ua.ttsagent.factory;

import javafx.scene.control.ComboBox;
import lombok.experimental.UtilityClass;

import java.util.Collection;

@UtilityClass
public class DropDownFactory {

    public ComboBox createDropDown(Collection<String> collection) {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getItems().addAll(collection);
        comboBox.setPromptText(collection.stream().findFirst().orElseThrow(() -> new RuntimeException("Can't create dropdown collection should not be empty")));
        return comboBox;
    }
}
