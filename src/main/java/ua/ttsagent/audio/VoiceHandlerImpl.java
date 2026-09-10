package ua.ttsagent.audio;

import jakarta.validation.constraints.NotEmpty;
import javafx.scene.control.TextArea;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import ua.ttsagent.config.AudioFormatConfig;
import ua.ttsagent.config.FileOutputConfig;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.UUID;

import static ua.ttsagent.audio.HandlerAction.START;
import static ua.ttsagent.audio.HandlerAction.STOP;

@Component
@Slf4j
@RequiredArgsConstructor
@Validated
class VoiceHandlerImpl implements VoiceHandler {
    private final AudioFormatConfig audioFormatConfig;
    private final FileOutputConfig fileOutputConfig;
    private volatile HandlerAction action = STOP;
    private LinkedList<Voice> voiceList = new LinkedList<>();


    @SneakyThrows
    @Override
    public void startHandleVoice(@NotEmpty String input, TextArea outputArea) {
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

        File resultFile = new File(fileOutputConfig.getOutputDir().toString() + "/" + UUID.randomUUID() + "out.wav");
        StreamingWavWriter writer = null;
        action = START;
        try {
            if (resultFile.exists()) {
                resultFile.delete();
            }
            resultFile.createNewFile();
            writer = new StreamingWavWriter(resultFile, 44100, 16, 1);
            voiceList.push(new Voice(resultFile, targetLine, inputMixer, writer));
            while (action != STOP) {
                targetLine.read(buffer, 0, buffer.length);
                writer.writeData(buffer);
            }
        } catch (IOException e) {
            log.debug("IOException {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error creating wav file", e);
        }
    }


    @Override
    public File stopsHandleVoice(TextArea outputArea) {
        action = STOP;
        Voice voice = voiceList.poll();
        voiceList.clear();
        if (voice != null) {

            var resultFile = voice.resultFile();
            try {
                var writer = voice.writer();
                var targetDataLine = voice.targetDataLine();
                var inputMixer = voice.inputMixer();
                if (writer != null) {
                    writer.close();
                    targetDataLine.close();
                    inputMixer.close();
                    resultFile.deleteOnExit();
                    log.info("Wav writer is closed, file {} is deleted", resultFile.getName());
                }
            } catch (Exception e) {
                outputArea.setText("Error something wrong please try again later.");
                log.error("Error creating wav file", e);
            }

            return resultFile;
        }
        return null;
    }

    record Voice(File resultFile, TargetDataLine targetDataLine,
                 Mixer inputMixer, StreamingWavWriter writer) {
    }
}
