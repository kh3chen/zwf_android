package com.zynga.zombieswf_android.socketio;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

public class ZombieApplication extends Application {

    private Socket mSocket;
    {
        try {
            mSocket = IO.socket("http://10.101.210.63:3800/");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public Socket getSocket() {
        return mSocket;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
