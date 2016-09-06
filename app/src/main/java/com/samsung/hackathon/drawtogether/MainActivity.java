package com.samsung.hackathon.drawtogether;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        App.L.d("onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    public void onMoveToDrawActivity(final View view)
    {
        App.L.d("onMoveToDrawActivity()");
        final Intent i = new Intent(this, DrawActivity.class);
        startActivity(i);
    }
}
