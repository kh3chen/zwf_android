package com.zynga.zombieswf_android;

import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.zynga.zombieswf_android.socketio.SocketConstants;
import com.zynga.zombieswf_android.socketio.SocketEvent;
import com.zynga.zombieswf_android.socketio.ZombieApplication;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    // default to 2 minutes
    private final int PING_COOLDOWN_TIME_MILLIS = 0;//2 * 1000 * 60;
    final int MY_ACCESS_FINE_LOCATION = 1;

    private GoogleMap mMap;
    private LocationManager mLocationManager;
    private Location mLocation;
    private TextView mCountDownTimer;
    private Button mPingButton;

    private String mRequestId = UUID.randomUUID().toString();
    private List<Marker> mMarkerList;
    private boolean mIsZombie;

    private Socket mSocket;
    private static final String TAG = "stickynotes";
    private boolean mResumed = false;
    private boolean mWriteMode = false;
    NfcAdapter mNfcAdapter;

    PendingIntent mNfcPendingIntent;
    IntentFilter[] mWriteTagFilters;
    IntentFilter[] mNdefExchangeFilters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mCountDownTimer = (TextView) findViewById(R.id.count_down_timer);

        // Timer stuff
        String minutesString;

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            minutesString = "2";
            mIsZombie = false;
        } else {
            minutesString = extras.getString(LobbyActivity.KEY_GAME_TIME);
            mIsZombie = extras.getBoolean(LobbyActivity.KEY_IS_ZOMBIE);
        }

        int timerMinutes = Integer.parseInt("60");//minutesString);

        new CountDownTimer(1000 * 60 * timerMinutes, 1000) { // Time in milis, countdown interval (1 second)

            public void onTick(long millisUntilFinished) {
                mCountDownTimer.setText("Time Remaining: " + formatCountDownTimeString(millisUntilFinished));
            }

            public void onFinish() {
                mCountDownTimer.setText("Round Ended!");
                AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this)
                        .setTitle("Round Over!")
                        .setMessage("Click NEXT to see who won!")
                        .setPositiveButton("NEXT", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                goToGameScoreScreen();
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                goToGameScoreScreen();
                                dialog.dismiss();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert);
                AlertDialog dialog = builder.create();

                dialog.setOnShowListener(new DialogInterface.OnShowListener() {

                    @Override
                    public void onShow(DialogInterface dialog) {
                        ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(true);
                        ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE).setVisibility(View.INVISIBLE);
                    }
                });

                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        goToGameScoreScreen();
                    }
                });

                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        goToGameScoreScreen();
                    }
                });

                dialog.show();
            }

        }.start();

        // Buttons
        mPingButton = (Button) findViewById(R.id.ping_button);
        Button playerButton = (Button) findViewById(R.id.players_button);

        mPingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ping();

                // set timer cooldown to 2 minutes
                mPingButton.setEnabled(false);

                new CountDownTimer(PING_COOLDOWN_TIME_MILLIS, 1000) { // Time in milis, countdown interval (1 second)

                    public void onTick(long millisUntilFinished) {
                        mPingButton.setText("Available in: " + formatCountDownTimeString(millisUntilFinished));
                    }

                    public void onFinish() {
                        mPingButton.setEnabled(true);
                    }

                }.start();

            }
        });

        if (mIsZombie) {
            findViewById(R.id.blood).setVisibility(View.VISIBLE);
        }

        playerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: send to player screen - P2
            }
        });

        ZombieApplication app = (ZombieApplication) getApplication();
        mSocket = app.getSocket();
        mSocket.on(SocketConstants.COLLECT, onGameEmit);
        if (!mSocket.connected()) {
            mSocket.connect();
        }

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        // Handle all of our received NFC intents in this activity.
        mNfcPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        // Intent filters for reading a note from a tag or exchanging over p2p.
        IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndefDetected.addDataType("text/plain");
        } catch (IntentFilter.MalformedMimeTypeException e) { }
        mNdefExchangeFilters = new IntentFilter[] { ndefDetected };

        // Intent filters for writing to a tag
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        mWriteTagFilters = new IntentFilter[] { tagDetected };

        mMarkerList = new ArrayList<>();
    }

    private String formatCountDownTimeString(long millisUntilFinished) {
        return String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
                TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)));
    }

    private void goToGameScoreScreen() {
        Intent intent = new Intent(getApplicationContext(), GameScoreActivity.class);
        startActivity(intent);
    }

    private void ping() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMarkerList.clear();
        mMap.clear();

        mLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        mSocket.emit(SocketConstants.EMIT, SocketEvent.makePing(mRequestId));
    }

    private void addMarker(LatLng latLng) {
        // Add a marker at location and move the camera
        mMarkerList.add(mMap.addMarker(new MarkerOptions().position(latLng)));

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()));
        for (Marker marker : mMarkerList) {
            builder.include(marker.getPosition());
        }
        LatLngBounds bounds = builder.build();

        int padding = 200; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

        mMap.animateCamera(cu);
    }

    /**
     * consider putting these in a NfcUtils class
     * @param intent
     * @return
     */
    NdefMessage[] getNdefMessages(Intent intent) {
        // Parse the intent
        NdefMessage[] msgs = null;
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Parcelable[] rawMsgs =
                    intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            } else {
                // Unknown tag type
                byte[] empty = new byte[] {};
                NdefRecord record =
                        new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, empty, empty);
                NdefMessage msg = new NdefMessage(new NdefRecord[] {
                        record
                });
                msgs = new NdefMessage[] {
                        msg
                };
            }
        } else {
            Log.d("ERROR", "Unknown intent.");
            finish();
        }
        return msgs;
    }

    @Override
    protected void onResume(){
        super.onResume();
        mResumed = true;
        // Sticky notes received from Android
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            NdefMessage[] messages = getNdefMessages(getIntent());
            byte[] payload = messages[0].getRecords()[0].getPayload();
            setNoteBody(new String(payload));
            setIntent(new Intent()); // Consume this intent.
        }
        enableNdefExchangeMode();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mResumed = false;
        mNfcAdapter.disableForegroundNdefPush(this);
    }


    @Override
    protected void onNewIntent(Intent intent) {
        // NDEF exchange mode
        if (!mWriteMode && NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            NdefMessage[] msgs = getNdefMessages(intent);
            promptForContent(msgs[0]);
        }

        // Tag writing mode
        // TODO: disable this for people who are human -- only allow one person to push.
        if (mWriteMode && NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            writeTag(getNoteAsNdef(), detectedTag);
        }
    }

    private TextWatcher mTextWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

        }

        @Override
        public void afterTextChanged(Editable arg0) {
            if (mResumed) {
                mNfcAdapter.enableForegroundNdefPush(MapsActivity.this, getNoteAsNdef());
            }
        }
    };

    private View.OnClickListener mTagWriter = new View.OnClickListener() {
        @Override
        public void onClick(View arg0) {
            // Write to a tag for as long as the dialog is shown.
            disableNdefExchangeMode();
            enableTagWriteMode();

            new android.app.AlertDialog.Builder(MapsActivity.this).setTitle("Touch tag to write")
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            disableTagWriteMode();
                            enableNdefExchangeMode();
                        }
                    }).create().show();
        }
    };

    private void promptForContent(final NdefMessage msg) {
        String body = new String(msg.getRecords()[0].getPayload());
        setNoteBody(body);
    }

    private void setNoteBody(String body) {
        if (!mIsZombie && TextUtils.equals(body, "Zombie")) {
            findViewById(R.id.blood).setVisibility(View.VISIBLE);
        }
    }

    private NdefMessage getNoteAsNdef() {
        // TODO: Make this different string, based on human or zombie?
        if (mIsZombie) {
            byte[] textBytes = "Zombie".toString().getBytes();
            NdefRecord textRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, "text/plain".getBytes(),
                    new byte[]{}, textBytes);
            return new NdefMessage(new NdefRecord[]{
                    textRecord
            });
        }
        else {
            byte[] textBytes = "Human".toString().getBytes();
            NdefRecord textRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, "text/plain".getBytes(),
                    new byte[]{}, textBytes);
            return new NdefMessage(new NdefRecord[]{
                    textRecord
            });
        }
    }

    private void enableNdefExchangeMode() {
        mNfcAdapter.enableForegroundNdefPush(MapsActivity.this, getNoteAsNdef());
        mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, mNdefExchangeFilters, null);
    }

    private void disableNdefExchangeMode() {
        mNfcAdapter.disableForegroundNdefPush(this);
        mNfcAdapter.disableForegroundDispatch(this);
    }

    private void enableTagWriteMode() {
        mWriteMode = true;
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        mWriteTagFilters = new IntentFilter[] {
                tagDetected
        };
        mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, mWriteTagFilters, null);
    }

    private void disableTagWriteMode() {
        mWriteMode = false;
        mNfcAdapter.disableForegroundDispatch(this);
    }

    boolean writeTag(NdefMessage message, Tag tag) {
        int size = message.toByteArray().length;

        try {
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                ndef.connect();

                if (!ndef.isWritable()) {
                    toast("Tag is read-only.");
                    return false;
                }
                if (ndef.getMaxSize() < size) {
                    toast("Tag capacity is " + ndef.getMaxSize() + " bytes, message is " + size
                            + " bytes.");
                    return false;
                }

                ndef.writeNdefMessage(message);
                toast("Wrote message to pre-formatted tag.");
                return true;
            } else {
                NdefFormatable format = NdefFormatable.get(tag);
                if (format != null) {
                    try {
                        format.connect();
                        format.format(message);
                        toast("Formatted tag and wrote message");
                        return true;
                    } catch (IOException e) {
                        toast("Failed to format tag.");
                        return false;
                    }
                } else {
                    toast("Tag doesn't support NDEF.");
                    return false;
                }
            }
        } catch (Exception e) {
            toast("Failed to write tag");
        }

        return false;
    }

    private void toast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_ACCESS_FINE_LOCATION);
            return;
        }

        mMap.setMyLocationEnabled(true);

        mLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (mLocation == null) {
            return;
        }

        LatLng myLocation = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());

        mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(15.0f));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onMapReady(mMap);
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_ACCESS_FINE_LOCATION);
                }
        }
    }

    // Prevent exiting game on back press
    @Override
    public void onBackPressed() {
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
                        Toast.makeText(MapsActivity.this, "Bad JSON string!", Toast.LENGTH_SHORT).show();
                    }
                });
                return;
            }

            final String ping = jsonObject.optString("ping");
            if (!TextUtils.isEmpty(ping)) {
                if (ActivityCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MapsActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_ACCESS_FINE_LOCATION);
                    return;
                }
                mSocket.emit(SocketConstants.EMIT, SocketEvent.makeLocationObject(mLocation, ping, mIsZombie));
            }

            final JSONObject location = jsonObject.optJSONObject("location");
            if (location != null) {
                final double latitude = location.optDouble("lat", 0);
                final double longitude = location.optDouble("long", 0);
                final String requestId = jsonObject.optString("id", "");
                final boolean isZombie = jsonObject.optBoolean("zombie", true);
                if (latitude != 0 && longitude != 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!TextUtils.equals(mRequestId, requestId) && !isZombie) {
                                addMarker(new LatLng(latitude, longitude));
                            }
                        }
                    });
                }
            }
        }
    };
}
