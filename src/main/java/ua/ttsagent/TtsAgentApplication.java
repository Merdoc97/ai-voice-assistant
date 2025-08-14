package ua.ttsagent;


import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import ua.ttsagent.controller.MainController;

@SpringBootApplication
public class TtsAgentApplication extends Application {


    private static ConfigurableApplicationContext springContext;

    public static void main(String[] args) {
        springContext=SpringApplication.run(TtsAgentApplication.class, args);
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        var controller=springContext.getBean(MainController.class);
        Scene scene = new Scene(controller.createUI(), 400, 200);
        primaryStage.setTitle("DropDown UI");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() {
        springContext.close();
    }


}
