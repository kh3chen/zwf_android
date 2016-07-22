package com.zynga.zombieswf_android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Created by azeng on 7/22/16.
 */
public class GameScoreActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_score_layout);

        // Display humans and zombies score
        TextView zombiesScore = (TextView) findViewById(R.id.zombies_count);
        TextView humansScore = (TextView) findViewById(R.id.humans_count);

        // TODO: set these as actual server numbers
        zombiesScore.setText("Zombies\n100");
        humansScore.setText("Humans\n100");

        Button playAgainButton = (Button) findViewById(R.id.play_again_button);
        playAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                startActivity(intent);
            }
        });
    }
}
