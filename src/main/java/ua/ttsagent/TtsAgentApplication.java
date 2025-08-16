package ua.ttsagent;


import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
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
        Scene scene = new Scene(controller.createUI(), 600, 200);
        primaryStage.setTitle("STT agent");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() {
        springContext.close();
    }


}
