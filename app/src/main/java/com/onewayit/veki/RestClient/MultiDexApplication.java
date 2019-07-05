package com.onewayit.veki.RestClient;

import android.app.Application;
import android.content.Context;

import androidx.multidex.MultiDex;


/**
 * Created by owitsol6 on 26-05-2016.
 */
public class MultiDexApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
