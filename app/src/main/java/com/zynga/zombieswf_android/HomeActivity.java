package com.zynga.zombieswf_android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.zynga.zombieswf_android.socketio.ChatApplication;

import org.json.JSONObject;

import java.util.Random;

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

        ChatApplication app = (ChatApplication) getApplication();
        mSocket = app.getSocket();
        mSocket.on("gameEmit", onGameEmit);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.create_game:
                onCreateGameClicked();
                break;
            case R.id.join_game:
                startActivity(new Intent(this, JoinGameActivity.class));
                break;
        }
    }

    private void onCreateGameClicked() {
        mSocket.emit("gameEmit", "Player " + new Random().nextInt(100) + " has created a game!");
    }

    private Emitter.Listener onGameEmit = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];

            Toast.makeText(HomeActivity.this, data.toString(), Toast.LENGTH_SHORT).show();
        }
    };
}
