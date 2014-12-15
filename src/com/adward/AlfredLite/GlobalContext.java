package com.adward.AlfredLite;

import android.app.Application;

/**
 * Created by Adward on 14/12/10.
 */
public class GlobalContext extends Application {
    private static GlobalContext instance;

    public static GlobalContext getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        instance = this;
    }
}
