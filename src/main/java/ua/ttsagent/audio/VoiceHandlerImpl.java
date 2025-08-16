package ua.ttsagent.audio;

import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import ua.ttsagent.config.AudioFormatConfig;
import ua.ttsagent.config.FileOutputConfig;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import java.io.File;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.UUID;

import static ua.ttsagent.audio.HandlerAction.STOP;

@Component
@Slf4j
@RequiredArgsConstructor
@Validated
class VoiceHandlerImpl implements VoiceHandler {
    private final AudioFormatConfig audioFormatConfig;
    private final FileOutputConfig fileOutputConfig;
    private volatile HandlerAction action = STOP;

    @SneakyThrows
    @Override
    public File startHandleVoice(@NotEmpty String input) {
        var audioFormat = audioFormatConfig.getAudioFormat();
        var inputInfo = Arrays.stream(AudioSystem.getMixerInfo())
                .filter(info -> info.getName().equalsIgnoreCase(input.toLowerCase()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Device " + input + " not found"));

        DataLine.Info targetInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
        var inputMixer = AudioSystem.getMixer(inputInfo);
        TargetDataLine targetLine = (TargetDataLine) inputMixer.getLine(targetInfo);
        targetLine.open(audioFormat);
        targetLine.start();
        action = HandlerAction.START;
        log.info("Started capturing audio from {}", input);
        byte[] buffer = new byte[4096];

        var resultFile = new File(fileOutputConfig.getOutputDir().toString() + "/" + UUID.randomUUID()+"out.wav");
        StreamingWavWriter writer = null;
        try {
            if (resultFile.exists()) {
                resultFile.delete();
            }
            resultFile.createNewFile();
            writer = new StreamingWavWriter(resultFile, 44100, 16, 1);
            var timeout = ZonedDateTime.now().plus(3, java.time.temporal.ChronoUnit.SECONDS);
            while (!ZonedDateTime.now().isAfter(timeout)) {
                targetLine.read(buffer, 0, buffer.length);
                writer.writeData(buffer);
            }
        } catch (Exception e) {
            log.error("Error creating wav file", e);
        } finally {
            if (writer != null) {
                log.info("Closing wav writer");
                writer.close();
                targetLine.close();
                inputMixer.close();
                resultFile.deleteOnExit();
            }
        }
        return resultFile;
    }

}
