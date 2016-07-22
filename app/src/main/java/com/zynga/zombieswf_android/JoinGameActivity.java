package com.zynga.zombieswf_android;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by byee on 7/21/16.
 */
public class JoinGameActivity extends Activity implements View.OnClickListener {

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_game);

        Button confirmButton = (Button) findViewById(R.id.confirm);
        confirmButton.setOnClickListener(this);

        //EditText gameCode = (EditText) findViewById(R.id.game_code);
        //gameCode.setLetterSpacing((float) 0.1);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.confirm:
                startActivity(new Intent(this, MapsActivity.class));
                break;
        }
    }
}
