package com.samsung.hackathon.drawtogether.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.samsung.hackathon.drawtogether.App;
import com.samsung.hackathon.drawtogether.R;

import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        App.L.d("");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    public void onMoveToCreatorActivity(final View view)
    {
        App.L.d("");
        final Intent i = new Intent(this, CreateorActivity.class);
        startActivity(i);
    }
}
