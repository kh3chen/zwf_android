package com.zynga.zombieswf_android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by azeng on 7/21/16.
 */
public class LobbyActivity extends Activity {
    // Game will always be from the creator
    private static final String KEY_GAME_TIME = "keyGameTime";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lobby_layout);

        LayoutInflater inflater = LayoutInflater.from(this);
        inflater.inflate(R.layout.lobby_layout, null);

        final EditText timeEditText = (EditText) findViewById(R.id.remaining_time);

        Button startGameButton = (Button) findViewById(R.id.start_game_button);
        startGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                intent.putExtra(KEY_GAME_TIME, String.valueOf(timeEditText.getText()));
                startActivity(intent);
            }
        });

        TextView numberOfUsers = (TextView) findViewById(R.id.number_of_users);
        // TODO: get and set number of users based on socket
    }

}


