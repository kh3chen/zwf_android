package com.zynga.zombieswf_android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Created by byee on 7/21/16.
 */
public class JoinGameActivity extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_game);

        Button confirmButton = (Button) findViewById(R.id.confirm);
        confirmButton.setOnClickListener(this);
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
