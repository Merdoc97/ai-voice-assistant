package ua.ttsagent.config;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.sound.sampled.AudioFormat;

@ConfigurationProperties(prefix = "audio.format")
@Data
@Validated
public class AudioFormatConfig {
    @NotNull(message = "sampleRate rate can't be null")
    @Positive(message = "sampleRate rate should be positive")
    private Integer sampleRate;
    @NotNull(message = "sampleSizeBit rate can't be null")
    @Positive(message = "sampleSizeBit rate should be positive")
    private Integer sampleSizeBit;
    @NotNull(message = "channels rate can't be null")
    @Positive(message = "channels rate should be positive")
    private Integer channels;
    @NotNull(message = "signed rate can't be null")
    private Boolean signed;
    @NotNull(message = "bigEndian rate can't be null")
    private Boolean bigEndian;

    public AudioFormat getAudioFormat() {
        return new AudioFormat(sampleRate, sampleSizeBit, channels, signed, bigEndian);
    }
}
