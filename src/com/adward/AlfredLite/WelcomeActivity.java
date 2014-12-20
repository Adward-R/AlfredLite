package com.adward.AlfredLite;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by Adward on 14/12/20.
 */
public class WelcomeActivity extends Activity {
    ImageView view;
    SharedPreferences mPreferences;
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (!mPreferences.getBoolean("display_firstlaunch",true)){
            Intent intent = new Intent();
            intent.setClass(WelcomeActivity.this,SearchActivity.class);
            startActivity(intent);
            WelcomeActivity.this.finish();
        }
        setContentView(R.layout.welcome);
        view = (ImageView) findViewById(R.id.welcome);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(WelcomeActivity.this,SearchActivity.class);
                startActivity(intent);
                WelcomeActivity.this.finish();
            }
        });
    }

}
