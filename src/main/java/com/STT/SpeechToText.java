package com.STT;

import okhttp3.*;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class SpeechToText {

    public static final String API_KEY = "RuitreAfUSezG18B2sv74yqo";
    public static final String SECRET_KEY = "1SMLUlE9rwraC0Ki5jAkAhvVgCvBpvgj";
    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient().newBuilder().build();

    public static void main(String[] args) {
        SpeechToText stt = new SpeechToText();
        try {
            // 使用相对路径访问audio文件夹中的音频文件
            String audioFilePath = "src/main/resources/audio/sample.pcm";
            String result = stt.recognizeSpeech(audioFilePath);
            System.out.println("语音识别结果: " + result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String recognizeSpeech(String audioFilePath) throws IOException {
        String accessToken = getAccessToken();
        byte[] data = readFileByBytes(audioFilePath);

        MediaType mediaType = MediaType.parse("audio/pcm;rate=16000");
        RequestBody body = RequestBody.create(mediaType, data);

        Request request = new Request.Builder()
                .url("https://vop.baidu.com/server_api?dev_pid=1537&cuid=yW7DBO2EiXRkfPhogKFQTlsHYxkuOdDa&token=" + accessToken)
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .build();
        Response response = HTTP_CLIENT.newCall(request).execute();
        JSONObject res = new JSONObject(response.body().string());
        if (res.has("result")) {
            return res.getJSONArray("result").getString(0);
        } else {
            return "未识别到语音内容: " + res;
        }
    }

    /**
     * 从用户的AK，SK生成鉴权签名（Access Token）
     *
     * @return 鉴权签名（Access Token）
     * @throws IOException IO异常
     */
    private static String getAccessToken() throws IOException {
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "grant_type=client_credentials&client_id=" + API_KEY
                + "&client_secret=" + SECRET_KEY);
        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/oauth/2.0/token")
                .method("POST", body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
        Response response = HTTP_CLIENT.newCall(request).execute();
        return new JSONObject(response.body().string()).getString("access_token");
    }

    private byte[] readFileByBytes(String filePath) throws IOException {
        File file = new File(filePath);
        return Files.readAllBytes(file.toPath());
    }
}
