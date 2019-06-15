package com.sample.gojeck.sampleapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LinearLayout layout = findViewById(R.id.forecast_container);
        layout.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_up));
    }
}
