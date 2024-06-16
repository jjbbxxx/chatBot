package com.main;

import com.STT.SpeechToText;
import com.Reecho.ReechoApi;
import com.record.AudioRecorder;
import com.xunfei.DisableWarning;
import com.xunfei.XunFeiBigModelMain;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;

public class MainController {

    public static void main(String[] args) {
        String audioFilePath = "output.pcm";
        AudioRecorder recorder = new AudioRecorder(audioFilePath);

        // 开始录音
        try {
            System.out.println("开始录音，请按回车键停止录音...");
            recorder.start();
            System.in.read();
            recorder.stop();
            System.out.println("录音结束，已保存为 " + audioFilePath + " 文件。");
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
            CountDownLatch latch = new CountDownLatch(1);
            DisableWarning.disableAccessWarnings();
            XunFeiBigModelMain.handleUserInput(speechToTextResult, latch);

            // 等待讯飞大模型响应完成
            latch.await();

            // 获取讯飞大模型的回应
            String xunFeiResponse = XunFeiBigModelMain.getLastResponse();

            // 将讯飞大模型的回答传给Reecho API
            String reechoResponse = ReechoApi.sendRequestWithText(xunFeiResponse);


        } catch (IOException | URISyntaxException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
