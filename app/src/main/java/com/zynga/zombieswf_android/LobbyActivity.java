package com.zynga.zombieswf_android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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
 * Created by azeng on 7/21/16.
 */
public class LobbyActivity extends Activity {
    // Game will always be from the creator
    public static final String KEY_GAME_TIME = "keyGameTime";

    private Socket mSocket;
    private String mGameCode = "1q2w3e";

    private List<String> playerIdList = new ArrayList<>();

    private EditText mTimeEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lobby_layout);

        LayoutInflater inflater = LayoutInflater.from(this);
        inflater.inflate(R.layout.lobby_layout, null);

        mTimeEditText = (EditText) findViewById(R.id.remaining_time);

        Button startGameButton = (Button) findViewById(R.id.start_game_button);
        startGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                intent.putExtra(KEY_GAME_TIME, String.valueOf(mTimeEditText.getText()));
                startActivity(intent);
            }
        });

        TextView numberOfUsers = (TextView) findViewById(R.id.number_of_users);
        // TODO: get and set number of users based on socket

        String androidId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        playerIdList.add(androidId);

        ZombieApplication app = (ZombieApplication) getApplication();
        mSocket = app.getSocket();
        mSocket.on(SocketConstants.COLLECT, onGameEmit);
        if (!mSocket.connected()) {
            mSocket.connect();
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
                        Toast.makeText(LobbyActivity.this, "Bad JSON string!", Toast.LENGTH_SHORT).show();
                    }
                });
                return;
            }

            final String toastMessage = jsonObject.optString("toast");
            if (!TextUtils.isEmpty(toastMessage)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LobbyActivity.this, toastMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            final String gameCode = jsonObject.optString("join");
            if (!TextUtils.isEmpty(gameCode)) {
                boolean isValidGameCode;
                if (mGameCode.equals(gameCode)) {
                    isValidGameCode = true;
                } else {
                    isValidGameCode = false;
                }
                mSocket.emit(SocketConstants.EMIT, SocketEvent.makeJoinResultObject(isValidGameCode, Integer.parseInt(mTimeEditText.getText().toString())));
            }
        }
    };
}


