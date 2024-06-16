package com.Reecho;

import javazoom.jl.player.advanced.AdvancedPlayer;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AudioPlayer {
    public static void playAudioFromUrl(String audioUrl) {
        try {
            URL url = new URL(audioUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            try (InputStream in = new BufferedInputStream(connection.getInputStream())) {
                AdvancedPlayer player = new AdvancedPlayer(in);
                player.play();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
