package com.record;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class AudioRecorder {

    private static final AudioFormat AUDIO_FORMAT = new AudioFormat(16000, 16, 1, true, true);
    private TargetDataLine line;
    private File outputFile;

    public AudioRecorder(String outputFilePath) {
        outputFile = new File(outputFilePath);
    }

    public void start() throws LineUnavailableException {
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, AUDIO_FORMAT);
        if (!AudioSystem.isLineSupported(info)) {
            throw new LineUnavailableException("Line not supported");
        }
        line = (TargetDataLine) AudioSystem.getLine(info);
        line.open(AUDIO_FORMAT);
        line.start();

        Thread recordingThread = new Thread(() -> {
            try (AudioInputStream ais = new AudioInputStream(line)) {
                AudioSystem.write(ais, AudioFileFormat.Type.WAVE, outputFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        recordingThread.start();
    }

    public void stop() {
        line.stop();
        line.close();
    }

    public static void main(String[] args) {
        AudioRecorder recorder = new AudioRecorder("output.pcm");
        try {
            System.out.println("开始录音，请按回车键停止录音...");
            recorder.start();
            System.in.read();
            recorder.stop();
            System.out.println("录音结束，已保存为 output.pcm 文件。");
        } catch (LineUnavailableException | IOException e) {
            e.printStackTrace();
        }
    }
}
