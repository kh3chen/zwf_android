package com.zynga.zombieswf_android.socketio;

import android.location.Location;

/**
 * Created by byee on 7/22/16.
 */
public class SocketEvent {

    public static String makeToastObject(String message) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{toast:\"");
        stringBuilder.append(message);
        stringBuilder.append("\"}");
        return stringBuilder.toString();
    }

    public static String makeJoinObject(String androidId, String gameCode) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{join:\"");
        stringBuilder.append(gameCode);
        stringBuilder.append("\",id:\"");
        stringBuilder.append(androidId);
        stringBuilder.append("\"}");
        return stringBuilder.toString();
    }

    public static String makeJoinResultObject(boolean isSuccessful, String androidId, int gameTime) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{join_successful:\"");
        stringBuilder.append(String.valueOf(isSuccessful));
        stringBuilder.append("\",game_time:\"");
        stringBuilder.append(String.valueOf(gameTime));
        stringBuilder.append("\",id:\"");
        stringBuilder.append(androidId);
        stringBuilder.append("\"}");
        return stringBuilder.toString();
    }

    public static String makePing() {
        return "{ping:\"now\"}";
    }

    public static String makeLocationObject(Location location, String requestId) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{location:{lat:");
        stringBuilder.append(String.valueOf(location.getLatitude()));
        stringBuilder.append(",long:");
        stringBuilder.append(String.valueOf(location.getLongitude()));
        stringBuilder.append("}");
        stringBuilder.append(",id:");
        stringBuilder.append(requestId);
        stringBuilder.append("}");
        return stringBuilder.toString();
    }

    public static String startGame() {
        return "{start_game:\"now\"}";
    }
}
