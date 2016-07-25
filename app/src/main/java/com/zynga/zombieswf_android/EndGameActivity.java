package com.zynga.zombieswf_android;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zynga.zombieswf_android.socketio.SocketConstants;
import com.zynga.zombieswf_android.socketio.SocketEvent;
import com.zynga.zombieswf_android.socketio.ZombieApplication;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by athompson on 16-07-22.
 */
public class EndGameActivity extends Activity {

    public final static String KEY_HUMAN_SCORE = "keyHumanScore";
    public final static String KEY_ZOMBIE_SCORE = "keyZombieScore";

    // Set The Winning Team
    boolean humansWin = false;
    int humanScore_value = 4;
    int zombieScore_value = 11;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_game);

        RelativeLayout layout =(RelativeLayout) findViewById(R.id.endgame_bg);
        TextView winnerText = (TextView) findViewById(R.id.winner_text);
        LinearLayout humansContainer =(LinearLayout) findViewById(R.id.human_score_container);
        LinearLayout zombiesContainer =(LinearLayout) findViewById(R.id.zombie_score_container);
        TextView zombieScore = (TextView) findViewById(R.id.zombieScore);
        TextView humanScore = (TextView) findViewById(R.id.humanScore);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            humanScore_value = extras.getInt(KEY_HUMAN_SCORE);
            zombieScore_value = extras.getInt(KEY_ZOMBIE_SCORE);
        } else {
            humanScore_value = 4;
            zombieScore_value = 11;
        }

        if (humanScore_value > zombieScore_value) {
            humansWin = true;
        } else {
            humansWin = false;
        }

        if (humansWin) {
            winnerText.setText("Humans Win!");
            layout.setBackgroundResource(R.drawable.humans_bg);
            humansContainer.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccentBlue));
            zombiesContainer.setBackgroundColor(ContextCompat.getColor(this, R.color.colorDark));
        }
        else {
            winnerText.setText("Zombies Win!");
            layout.setBackgroundResource(R.drawable.zombies_bg);
            humansContainer.setBackgroundColor(ContextCompat.getColor(this, R.color.colorDark));
            zombiesContainer.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccentGreenGrey));
        }

        zombieScore.setText(String.valueOf(zombieScore_value));
        humanScore.setText(String.valueOf(humanScore_value));

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
