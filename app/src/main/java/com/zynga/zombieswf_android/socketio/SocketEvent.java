package com.zynga.zombieswf_android.socketio;

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

    public static String makeJoinObject(String gameCode) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{join:\"");
        stringBuilder.append(gameCode);
        stringBuilder.append("\"}");
        return stringBuilder.toString();
    }

    public static String makeJoinResultObject(boolean isSuccessful, int gameTime) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{join_successful:\"");
        stringBuilder.append(String.valueOf(isSuccessful));
        stringBuilder.append("\",game_time:\"");
        stringBuilder.append(String.valueOf(gameTime));
        stringBuilder.append("\"}");
        return stringBuilder.toString();
    }
}
