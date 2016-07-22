package com.zynga.zombieswf_android;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.zynga.zombieswf_android.socketio.SocketConstants;
import com.zynga.zombieswf_android.socketio.SocketEvent;
import com.zynga.zombieswf_android.socketio.ZombieApplication;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by byee on 7/21/16.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class JoinGameActivity extends Activity implements View.OnClickListener {
    private Socket mSocket;

    private String mAndroidId;

    private int mGameTime;

    private boolean mIsZombie;

    private List<String> playerIdList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_game);

        Button confirmButton = (Button) findViewById(R.id.confirm);
        confirmButton.setOnClickListener(this);

        mAndroidId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        playerIdList.add(mAndroidId);
        //EditText gameCode = (EditText) findViewById(R.id.game_code);
        //gameCode.setLetterSpacing((float) 0.1);

        ZombieApplication app = (ZombieApplication) getApplication();
        mSocket = app.getSocket();
        mSocket.on(SocketConstants.COLLECT, onGameEmit);
        if (!mSocket.connected()) {
            mSocket.connect();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.confirm:
                mSocket.emit(SocketConstants.EMIT, SocketEvent.makeJoinObject(mAndroidId, "1q2w3e"));
                break;
        }
    }

    private Emitter.Listener onGameEmit = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(((String) args[0]));
            } catch (JSONException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(JoinGameActivity.this, "Bad JSON string!", Toast.LENGTH_SHORT).show();
                    }
                });
                return;
            }

            final String joinSuccessful = jsonObject.optString("join_successful");
            if (!TextUtils.isEmpty(joinSuccessful)) {
                if (Boolean.parseBoolean(joinSuccessful)) {
                    mGameTime = jsonObject.optInt("game_time");
                    playerIdList.add(jsonObject.optString("id"));
                    mIsZombie = jsonObject.optBoolean("zombie");
                    /*
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(JoinGameActivity.this, "Successfully joined! Waiting for game to start...", Toast.LENGTH_SHORT).show();
                        }
                    });
                    */
                    // go to create game screen with things disabled
                    Intent intent = new Intent(getApplicationContext(), LobbyActivity.class);
                    intent.putExtra(LobbyActivity.KEY_IS_CREATOR, false);
                    intent.putExtra(LobbyActivity.KEY_IS_ZOMBIE, mIsZombie);
                    // Add map time
                    // Intent intent = new Intent(JoinGameActivity.this, MapsActivity.class);
                    intent.putExtra(LobbyActivity.KEY_GAME_TIME, String.valueOf(mGameTime));
                    startActivity(intent);

                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(JoinGameActivity.this, "Unable to join.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
//
//            final String startGame = jsonObject.optString("start_game");
//            if (!TextUtils.isEmpty(startGame)) {
//                Intent intent = new Intent(JoinGameActivity.this, MapsActivity.class);
//                intent.putExtra(LobbyActivity.KEY_PLAYER_LIST, TextUtils.join(",", playerIdList));
//                intent.putExtra(LobbyActivity.KEY_GAME_TIME, String.valueOf(mGameTime));
//                startActivity(intent);
//            }
        }
    };
}
