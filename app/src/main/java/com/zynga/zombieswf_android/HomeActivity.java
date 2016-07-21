package com.zynga.zombieswf_android;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * Created by byee on 7/21/16.
 */
public class HomeActivity extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Button createGameButton = (Button) findViewById(R.id.create_game);
        Button joinGameButton = (Button) findViewById(R.id.join_game);

        createGameButton.setOnClickListener(this);
        joinGameButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.create_game:
                Toast.makeText(this, "CREATE GAME", Toast.LENGTH_SHORT).show();
                break;
            case R.id.join_game:
                Toast.makeText(this, "JOIN GAME", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
