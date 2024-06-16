package com.Reecho;

import okhttp3.*;
import java.util.concurrent.TimeUnit;

public class ReechoApi {
    private static final String API_URL = "https://v1.reecho.cn/api/tts/simple-generate";
    private static final String TOKEN = "sk-2fbd63e62e18a4fff9b1532d5f0e0155"; // 替换为实际的Token

    public static String sendRequestWithText(String text) {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        MediaType mediaType = MediaType.parse("application/json");

        String jsonInputString = "{\n" +
                "    \"voiceId\": \"df8d3bef-4e8d-40e6-86b5-be94b71dfd75\",\n" +
                "    \"text\": \"" + text + "\",\n" +
                "    \"promptId\": \"default\",\n" +
                "    \"model\": \"reecho-neural-voice-001\",\n" +
                "    \"randomness\": 90,\n" +
                "    \"stability_boost\": 100,\n" +
                "    \"probability_optimization\": 95,\n" +
                "    \"origin_audio\": false,\n" +
                "    \"stream\": false\n" +
                "}";

        RequestBody body = RequestBody.create(mediaType, jsonInputString);
        Request request = new Request.Builder()
                .url(API_URL)
                .method("POST", body)
                .addHeader("User-Agent", "Apifox/1.0.0 (https://apifox.com)")
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + TOKEN)
                .build();

        long startTime = System.currentTimeMillis();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body() != null ? response.body().string() : null;
                if (responseBody != null) {
                    ResponseAnalyzer.analyzeResponse(responseBody, startTime);
                }
                return responseBody;
            } else {
                System.err.println("Request failed: " + response.code());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        String text = "我是大黄蜂，来自塞伯坦星球。我是一位勇敢的战士，也是变形金刚的一员。我擅长侦查和战斗，喜欢保护朋友和地球。";
        String response = sendRequestWithText(text);
        if (response != null) {
            System.out.println("Reecho API返回: " + response);
        }
    }
}
