package ua.ttsagent.audio;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class StreamingWavWriter {
    private final RandomAccessFile raf;
    private final int sampleRate, bitsPerSample, channels;
    private int dataSize = 0;

    public StreamingWavWriter(File file, int sampleRate, int bitsPerSample, int channels) throws IOException {
        this.sampleRate = sampleRate;
        this.bitsPerSample = bitsPerSample;
        this.channels = channels;
        this.raf = new RandomAccessFile(file, "rw");
        writeHeaderPlaceholder();
    }

    private void writeHeaderPlaceholder() throws IOException {
        raf.seek(0);
        raf.write("RIFF".getBytes());
        raf.write(intToLE(0)); // Placeholder for ChunkSize
        raf.write("WAVE".getBytes());

        raf.write("fmt ".getBytes());
        raf.write(intToLE(16)); // PCM
        raf.write(shortToLE((short) 1)); // AudioFormat
        raf.write(shortToLE((short) channels));
        raf.write(intToLE(sampleRate));
        int byteRate = sampleRate * channels * bitsPerSample / 8;
        int blockAlign = channels * bitsPerSample / 8;
        raf.write(intToLE(byteRate));
        raf.write(shortToLE((short) blockAlign));
        raf.write(shortToLE((short) bitsPerSample));

        raf.write("data".getBytes());
        raf.write(intToLE(0)); // Placeholder for Subchunk2Size
    }

    public void writeData(byte[] buffer) throws IOException {
        raf.seek(raf.length());
        raf.write(buffer);
        dataSize += buffer.length;
    }

    public void close() throws IOException {
        // Update sizes in header
        raf.seek(4);
        raf.write(intToLE(36 + dataSize)); // ChunkSize
        raf.seek(40);
        raf.write(intToLE(dataSize)); // Subchunk2Size
        raf.close();
    }

    private static byte[] intToLE(int value) {
        return new byte[] {
                (byte)(value),
                (byte)(value >> 8),
                (byte)(value >> 16),
                (byte)(value >> 24)
        };
    }

    private static byte[] shortToLE(short value) {
        return new byte[] {
                (byte)(value),
                (byte)(value >> 8)
        };
    }
}