package com.zynga.zombieswf_android;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.CountDownTimer;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.concurrent.TimeUnit;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    final int MY_ACCESS_FINE_LOCATION = 1;

    private GoogleMap mMap;

    private LocationManager mLocationManager;

    private Location mLocation;

    private TextView mCountDownTimer;

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
        if(extras == null) {
            minutesString = null;
        } else {
            minutesString = extras.getString(LobbyActivity.KEY_GAME_TIME);
        }

        int timerMinutes = Integer.parseInt(minutesString);

        new CountDownTimer(1000 * 60 * timerMinutes, 1000) { // Time in milis, countdown interval (1 second)

            public void onTick(long millisUntilFinished) {
                // Format it here
                String formattedString = String.format("%02d:%02d:%02d",
                        TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) -
                                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)));
                mCountDownTimer.setText("Time Remaining: " + formattedString);
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
                        ((AlertDialog)dialog).getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(true);
                        ((AlertDialog)dialog).getButton(AlertDialog.BUTTON_NEGATIVE).setVisibility(View.INVISIBLE);
                    }
                });

                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        goToGameScoreScreen();
                    }
                });

                dialog.show();
            }

        }.start();

        // Buttons
        Button pingButton = (Button) findViewById(R.id.ping_button);
        Button playerButton = (Button) findViewById(R.id.players_button);

        pingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: call server for all gps locations
            }
        });

        playerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: send to player screen - P2
            }
        });

    }

    private void goToGameScoreScreen() {
        Intent intent = new Intent(getApplicationContext(), GameScoreActivity.class); // TODO: make game screen
        startActivity(intent);
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
        mLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (mLocation == null) {
            return;
        }

        mMap.setMyLocationEnabled(true);

        LatLng me = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());

        // Add a marker at location and move the camera
        mMap.addMarker(new MarkerOptions().position(me).title("You"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(-151, 25)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(me));
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
}
