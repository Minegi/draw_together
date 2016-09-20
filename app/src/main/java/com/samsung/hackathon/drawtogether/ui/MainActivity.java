package com.samsung.hackathon.drawtogether.ui;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.samsung.hackathon.drawtogether.App;
import com.samsung.hackathon.drawtogether.R;

public class MainActivity extends AppCompatActivity {

    private Context mContext;

    // buttons
    private ImageView mCreatorBtn;
    private ImageView mImitatorBtn;
    private ImageView mSendBtn;
    private ImageView mMarketBtn;

    // listeners
    private View.OnClickListener mCreatorBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
            final Intent intent = new Intent(mContext, CreatorActivity.class);
            startActivity(intent);
        }
    };

    private View.OnClickListener mImitatorBtnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(final View view) {
            final Intent intent = new Intent(mContext, ImitatorActivity.class);
//            final Intent intent = new Intent(mContext, ArtworkChoiceActivity.class);
            startActivity(intent);
        }
    };

    private View.OnClickListener mSendBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
            Toast.makeText(mContext, "Send", Toast.LENGTH_LONG).show();
        }
    };

    private View.OnClickListener mMarketBtnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(final View view) {
            Toast.makeText(mContext, "Market", Toast.LENGTH_LONG).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        App.L.d("");
        mContext = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
    }

    private void initializeViews() {
        App.L.d("");
        mCreatorBtn = (ImageView) findViewById(R.id.creator_img);
        mCreatorBtn.setOnClickListener(mCreatorBtnClickListener);

        mImitatorBtn = (ImageView) findViewById(R.id.imitator_img);
        mImitatorBtn.setOnClickListener(mImitatorBtnClickListener);

        mSendBtn = (ImageView) findViewById(R.id.send_img);
        mSendBtn.setOnClickListener(mSendBtnClickListener);

        mMarketBtn = (ImageView) findViewById(R.id.market_img);
        mMarketBtn.setOnClickListener(mMarketBtnClickListener);
    }
}
