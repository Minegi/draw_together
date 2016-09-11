package com.samsung.hackathon.drawtogether;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        App.L.d("onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    public void onMoveToCreatorActivity(final View view)
    {
        App.L.d("onMoveToCreatorActivity()");
        final Intent i = new Intent(this, CreateorActivity.class);
        startActivity(i);
    }
}
