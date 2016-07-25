package com.zynga.zombieswf_android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
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
    public static final String KEY_PLAYER_LIST = "keyPlayerList";
    public static final String KEY_GAME_TIME = "keyGameTime";
    public static final String KEY_IS_CREATOR = "keyIsCreator";
    public static final String KEY_IS_ZOMBIE = "keyIsZombie";

    private Socket mSocket;
    private String mGameCode = "1q2w3e";
    private boolean mIsZombie;

    private List<String> playerIdList = new ArrayList<>();

    private int mNumZombies = 0;

    private EditText mTimeEditText;
    private Button mStartGameButton;
    private boolean mIsCreator = false;

    private String mAndroidId;
    private String mGameTime;

    TextView numberOfUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lobby_layout);

        LayoutInflater inflater = LayoutInflater.from(this);
        inflater.inflate(R.layout.lobby_layout, null);

        mTimeEditText = (EditText) findViewById(R.id.remaining_time);

        mStartGameButton = (Button) findViewById(R.id.start_game_button);
        mStartGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mIsZombie = true;
                mSocket.emit(SocketConstants.EMIT, SocketEvent.startGame());
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                intent.putExtra(LobbyActivity.KEY_PLAYER_LIST, TextUtils.join(",", playerIdList));
                intent.putExtra(KEY_GAME_TIME, mTimeEditText.getText().toString());
                intent.putExtra(KEY_IS_ZOMBIE, mIsZombie);
                startActivity(intent);
            }
        });

        mAndroidId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        playerIdList.add(mAndroidId);

        //numberOfUsers = (TextView) findViewById(R.id.number_of_users);
        //numberOfUsers.setText(playerIdList.size());

        String[] playerList = { "athompson", "kliang", "azeng", "byee", "kchen", "coostenbrug", "knguyen", "mzhong", "dshi", "psung", "ecampbell" };
        ListView playerListView = (ListView) findViewById(R.id.player_list);
        playerListView.setAdapter(new ArrayAdapter<String>(this,R.layout.z_player_list,R.id.list_content, playerList));

        ZombieApplication app = (ZombieApplication) getApplication();
        mSocket = app.getSocket();
        mSocket.on(SocketConstants.COLLECT, onGameEmit);
        if (!mSocket.connected()) {
            mSocket.connect();
        }

        // See if we are creator
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            mIsCreator = false;
            mGameTime = "2";
            mIsZombie = false;
        } else {
            mIsCreator = extras.getBoolean(LobbyActivity.KEY_IS_CREATOR);
            mGameTime = extras.getString(LobbyActivity.KEY_GAME_TIME);
            mIsZombie = extras.getBoolean(LobbyActivity.KEY_IS_ZOMBIE);
        }
        if (!mIsCreator) {
            styleForNonCreator();
        }
    }

    private void styleForNonCreator() {
        mTimeEditText.setEnabled(false);
        mTimeEditText.setText(String.valueOf(mGameTime));
        mStartGameButton.setText("Waiting ...");
        mStartGameButton.setEnabled(false);
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
                String otherPlayerId = jsonObject.optString("id");
                if (mGameCode.equals(gameCode) && !TextUtils.isEmpty(otherPlayerId)) {
                    isValidGameCode = true;
                    playerIdList.add(otherPlayerId);
                    //numberOfUsers.setText(playerIdList.size());
                } else {
                    isValidGameCode = false;
                }
                boolean isZombie = mNumZombies == 0 || mNumZombies < playerIdList.size() * 0.2;
                if (isZombie) {
                    mNumZombies++;
                }
                mSocket.emit(SocketConstants.EMIT, SocketEvent.makeJoinResultObject(isValidGameCode, mAndroidId, Integer.parseInt(mTimeEditText.getText().toString()), isZombie));
            }

            // listen for game start
            final String startGame = jsonObject.optString("start_game");
            if (!TextUtils.isEmpty(startGame)) {
                Intent intent = new Intent(LobbyActivity.this, MapsActivity.class);
                intent.putExtra(LobbyActivity.KEY_PLAYER_LIST, TextUtils.join(",", playerIdList));
                intent.putExtra(LobbyActivity.KEY_GAME_TIME, mGameTime);
                intent.putExtra(LobbyActivity.KEY_IS_ZOMBIE, mIsZombie);
                startActivity(intent);
            }
        }
    };
}


