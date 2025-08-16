package ua.ttsagent.config;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.nio.file.Path;

@ConfigurationProperties(prefix = "file.output")
@Validated
@Data
public class FileOutputConfig {
    @NotNull(message = "outputDir can't be null")
    private Path outputDir;
}
