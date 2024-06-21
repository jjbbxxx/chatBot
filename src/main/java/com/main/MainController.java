package com.main;

import com.STT.SpeechToText;
import com.Reecho.ReechoApi;
import com.record.AudioRecorder;
import com.xunfei.DisableWarning;
import com.xunfei.XunFeiBigModelMain;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainController {

    private static final String audioFilePath = "output.pcm";
    private static final int MAX_RECORDING_TIME = 15; // 最大录音时间15秒

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("按回车键开始录音...");
            scanner.nextLine();

            AudioRecorder recorder = new AudioRecorder(audioFilePath);
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            CountDownLatch latch = new CountDownLatch(1);

            // 开始录音
            try {
                System.out.println("开始录音，请按回车键停止录音...");
                recorder.start();

                // 安排15秒后停止录音
                scheduler.schedule(() -> {
                    if (latch.getCount() > 0) {
                        recorder.stop();
                        System.out.println("录音超时，已自动停止。");
                        latch.countDown();
                    }
                }, MAX_RECORDING_TIME, TimeUnit.SECONDS);

                // 等待用户按回车键停止录音
                scanner.nextLine();
                if (latch.getCount() > 0) {
                    recorder.stop();
                    System.out.println("录音结束，已保存为 " + audioFilePath + " 文件。");
                    latch.countDown();
                }
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            // 语音识别和大模型处理
            SpeechToText stt = new SpeechToText();
            try {
                String speechToTextResult = stt.recognizeSpeech(audioFilePath);
                System.out.println("语音识别结果: " + speechToTextResult);

                // 调用讯飞大模型处理语音识别结果
                CountDownLatch modelLatch = new CountDownLatch(1);
                DisableWarning.disableAccessWarnings();
                XunFeiBigModelMain.handleUserInput(speechToTextResult, modelLatch);

                // 等待讯飞大模型响应完成
                modelLatch.await();

                // 获取讯飞大模型的回应
                String xunFeiResponse = XunFeiBigModelMain.getLastResponse();
                //System.out.println("讯飞大模型回答: " + xunFeiResponse);
                // 将讯飞大模型的回答传给Reecho API
                String reechoResponse = ReechoApi.sendRequestWithText(xunFeiResponse);
                if (reechoResponse != null) {
                    System.out.println("Reecho API返回: " + reechoResponse);
                }

            } catch (IOException | URISyntaxException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
