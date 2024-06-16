package com.Reecho;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ResponseAnalyzer {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void analyzeResponse(String jsonResponse, long startTime) {
        try {
            JsonNode response = objectMapper.readTree(jsonResponse);
            int status = response.get("status").asInt();
            switch (status) {
                case 200:
                    String audioUrl = response.get("data").get("audio").asText();
                    System.out.println("Audio URL: " + audioUrl);

                    long endTime = System.currentTimeMillis();
                    System.out.println("Request successful. Total time taken: " + (endTime - startTime) + " ms");
                    AudioPlayer.playAudioFromUrl(audioUrl);
                    break;
                case 400:
                    System.err.println("Error 400: Request parameter error.");
                    break;
                case 403:
                    System.err.println("Error 403: Insufficient remaining points.");
                    break;
                case 404:
                    System.err.println("Error 404: Model or voice not found.");
                    break;
                case 500:
                    System.err.println("Error 500: Internal server error.");
                    break;
                default:
                    System.err.println("Error " + status + ": " + response.get("message").asText());
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
