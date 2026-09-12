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
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.UUID;
import java.util.function.DoubleConsumer;

import static ua.ttsagent.audio.HandlerAction.START;
import static ua.ttsagent.audio.HandlerAction.STOP;

@Component
@Slf4j
@RequiredArgsConstructor
@Validated
class VoiceHandlerImpl implements VoiceHandler {
    private final AudioFormatConfig audioFormatConfig;
    private final FileOutputConfig fileOutputConfig;
    private final Object voiceLock = new Object();

    private volatile HandlerAction action = STOP;
    private volatile Session session;

    @SneakyThrows
    @Override
    public void startHandleVoice(@NotEmpty String input, TextArea outputArea, DoubleConsumer levelListener) {
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
        action = START;
        log.info("Started capturing audio from {}", input);

        byte[] buffer = new byte[4096];
        try {
            synchronized (voiceLock) {
                session = createSession(targetLine, inputMixer);
            }
            while (action != STOP) {
                int read = targetLine.read(buffer, 0, buffer.length);
                if (read > 0) {
                    Session current = session;
                    if (current != null) {
                        synchronized (voiceLock) {
                            Session lockedCurrent = session;
                            if (lockedCurrent != null) {
                                lockedCurrent.writer().writeData(buffer);
                            }
                        }
                    }
                    updateLevel(buffer, read, levelListener);
                }
            }
            publishSilent(levelListener);
        } catch (IOException e) {
            log.debug("IOException {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error creating wav file", e);
        } finally {
            synchronized (voiceLock) {
                closeSession(session, outputArea, false);
                session = null;
            }
        }
    }

    @Override
    public File snapshotHandleVoice(TextArea outputArea) {
        synchronized (voiceLock) {
            Session current = session;
            if (current == null) {
                return null;
            }

            try {
                File sourceFile = current.resultFile();
                if (!sourceFile.exists() || sourceFile.length() <= 44) {
                    return null;
                }

                File snapshotFile = new File(fileOutputConfig.getOutputDir().toString() + "/" + UUID.randomUUID() + "-snapshot.wav");
                copyWaveSnapshot(sourceFile, snapshotFile);
                snapshotFile.deleteOnExit();
                return snapshotFile;
            } catch (Exception e) {
                outputArea.setText("Error something wrong please try again later.");
                log.error("Error creating wav snapshot", e);
                return null;
            }
        }
    }

    @Override
    public void rolloverHandleVoice(TextArea outputArea) {
        synchronized (voiceLock) {
            Session current = session;
            if (current == null) {
                return;
            }

            try {
                File previous = current.resultFile();
                current.writer().close();
                previous.deleteOnExit();
                session = createSession(current.targetDataLine(), current.inputMixer());
                log.info("Rolled over recording file from {}", previous.getName());
            } catch (Exception e) {
                outputArea.setText("Error something wrong please try again later.");
                log.error("Error rolling over wav file", e);
            }
        }
    }

    @Override
    public File stopsHandleVoice(TextArea outputArea) {
        synchronized (voiceLock) {
            action = STOP;
            Session current = session;
            session = null;
            if (current != null) {
                return closeSession(current, outputArea, true);
            }
            return null;
        }
    }

    private Session createSession(TargetDataLine targetLine, Mixer inputMixer) throws IOException {
        File resultFile = new File(fileOutputConfig.getOutputDir().toString() + "/" + UUID.randomUUID() + "out.wav");
        if (resultFile.exists()) {
            resultFile.delete();
        }
        resultFile.createNewFile();
        StreamingWavWriter writer = new StreamingWavWriter(resultFile, 44100, 16, 1);
        return new Session(resultFile, targetLine, inputMixer, writer);
    }

    private File closeSession(Session current, TextArea outputArea, boolean returnResultFile) {
        File resultFile = current.resultFile();
        try {
            current.writer().close();
            current.targetDataLine().close();
            current.inputMixer().close();
            resultFile.deleteOnExit();
            log.info("Wav writer is closed, file {} is deleted", resultFile.getName());
        } catch (Exception e) {
            outputArea.setText("Error something wrong please try again later.");
            log.error("Error creating wav file", e);
        }
        return returnResultFile ? resultFile : null;
    }

    private void copyWaveSnapshot(File sourceFile, File snapshotFile) throws IOException {
        try (RandomAccessFile source = new RandomAccessFile(sourceFile, "r");
             RandomAccessFile target = new RandomAccessFile(snapshotFile, "rw")) {
            long length = source.length();
            if (length <= 44) {
                return;
            }

            byte[] data = new byte[(int) length];
            source.seek(0);
            source.readFully(data);
            target.setLength(0);
            target.write(data);
            int dataSize = (int) (length - 44);
            target.seek(4);
            target.write(intToLE(36 + dataSize));
            target.seek(40);
            target.write(intToLE(dataSize));
        }
    }

    private static byte[] intToLE(int value) {
        return new byte[] {
                (byte) (value),
                (byte) (value >> 8),
                (byte) (value >> 16),
                (byte) (value >> 24)
        };
    }

    private record Session(File resultFile, TargetDataLine targetDataLine,
                           Mixer inputMixer, StreamingWavWriter writer) {
    }

    private void updateLevel(byte[] buffer, int read, DoubleConsumer levelListener) {
        if (levelListener == null || read <= 0) {
            return;
        }
        long sum = 0;
        for (int i = 0; i + 1 < read; i += 2) {
            int sample = (buffer[i] & 0xff) | (buffer[i + 1] << 8);
            if (sample > 32767) {
                sample -= 65536;
            }
            sum += (long) sample * sample;
        }
        double rms = Math.sqrt(sum / (double) Math.max(1, read / 2));
        double normalized = Math.min(1.0, rms / 12000.0);
        levelListener.accept(normalized);
    }

    private void publishSilent(DoubleConsumer levelListener) {
        if (levelListener != null) {
            levelListener.accept(0.0);
        }
    }
}
