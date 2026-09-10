package ua.ttsagent;


import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.StageStyle;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import ua.ttsagent.config.AudioFormatConfig;
import ua.ttsagent.config.FileOutputConfig;
import ua.ttsagent.controller.MainController;

@SpringBootApplication
@EnableConfigurationProperties(value = {AudioFormatConfig.class, FileOutputConfig.class})
public class TtsAgentApplication extends Application {


    private static ConfigurableApplicationContext springContext;

    public static void main(String[] args) {


        SpringApplication app = new SpringApplication(TtsAgentApplication.class);
        app.setWebApplicationType(WebApplicationType.NONE); // 👈 отключает Tomcat/Jetty/Netty
        springContext = app.run(args);
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        var controller = springContext.getBean(MainController.class);
        Scene scene = new Scene(controller.createUI(primaryStage), 1024, 600);
        scene.getStylesheets().add(getClass().getResource("/styles/app.css").toExternalForm());
        scene.setFill(Color.TRANSPARENT);
        primaryStage.setTitle("Voice AI Assistant");
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() {
        springContext.close();
    }


}
