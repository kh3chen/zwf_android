package com.zynga.zombieswf_android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.zynga.zombieswf_android.socketio.SocketConstants;
import com.zynga.zombieswf_android.socketio.SocketEvent;
import com.zynga.zombieswf_android.socketio.ZombieApplication;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by byee on 7/21/16.
 */
public class HomeActivity extends Activity implements View.OnClickListener {

    private Socket mSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Button createGameButton = (Button) findViewById(R.id.create_game);
        Button joinGameButton = (Button) findViewById(R.id.join_game);

        createGameButton.setOnClickListener(this);
        joinGameButton.setOnClickListener(this);

        ZombieApplication app = (ZombieApplication) getApplication();
        mSocket = app.getSocket();
        mSocket.on(SocketConstants.COLLECT, onGameEmit);
        mSocket.connect();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.create_game:
                onCreateGameClicked();
                startActivity(new Intent(this, LobbyActivity.class));
                break;
            case R.id.join_game:
                startActivity(new Intent(this, JoinGameActivity.class));
                break;
        }
    }

    private void onCreateGameClicked() {
        // mSocket.emit(SocketConstants.EMIT, SocketEvent.makeToastObject("SUCCESSFUL TOAST!"));
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
                        Toast.makeText(HomeActivity.this, "Bad JSON string!", Toast.LENGTH_SHORT).show();
                    }
                });
                return;
            }

            final String toast = jsonObject.optString("toast");
            if (!TextUtils.isEmpty(toast)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(HomeActivity.this, toast, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    };
}
