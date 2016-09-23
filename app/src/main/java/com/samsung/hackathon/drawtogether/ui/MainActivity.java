package com.samsung.hackathon.drawtogether.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.samsung.hackathon.drawtogether.App;
import com.samsung.hackathon.drawtogether.R;
import com.samsung.hackathon.drawtogether.util.FileHelper;

public class MainActivity extends AppCompatActivity {

    private Context mContext;

    // buttons
    private ImageButton mCreatorBtn;
    private ImageButton mImitatorBtn;
    private ImageButton mSendBtn;
    private ImageButton mMarketBtn;

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
            final Intent intent = new Intent(mContext, ArtworkChoiceActivity.class);
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
            final Intent intent = new Intent(mContext, ArtworkMarketActivity.class);
            startActivity(intent);
        }
    };

    private View.OnTouchListener mBtnTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                final int action = motionEvent.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        view.setAlpha(0.5f);
                        return false;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        view.setAlpha(1.0f);
                        return false;
                    case MotionEvent.ACTION_MOVE:
                        if (view.isPressed()) {
                            view.setAlpha(0.5f);
                        } else {
                            view.setAlpha(1.0f);
                        }
                        return false;
                }

                return false;
            }
    };

    private void setDefaultBackground(View view) {
        view.setBackgroundResource(R.drawable.frame);
    }

    private void setSelectedBackground(View view) {
        view.setBackgroundResource(0);
    }

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
        mCreatorBtn = (ImageButton) findViewById(R.id.creator_img);
        mCreatorBtn.setOnClickListener(mCreatorBtnClickListener);
        setDefaultBackground(mCreatorBtn);
        mCreatorBtn.setOnTouchListener(mBtnTouchListener);

        mImitatorBtn = (ImageButton) findViewById(R.id.imitator_img);
        mImitatorBtn.setOnClickListener(mImitatorBtnClickListener);
        setDefaultBackground(mImitatorBtn);
        mImitatorBtn.setOnTouchListener(mBtnTouchListener);

        mSendBtn = (ImageButton) findViewById(R.id.send_img);
        mSendBtn.setOnClickListener(mSendBtnClickListener);
        setDefaultBackground(mSendBtn);
        mSendBtn.setOnTouchListener(mBtnTouchListener);

        mMarketBtn = (ImageButton) findViewById(R.id.market_img);
        mMarketBtn.setOnClickListener(mMarketBtnClickListener);
        setDefaultBackground(mMarketBtn);
        mMarketBtn.setOnTouchListener(mBtnTouchListener);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(MainActivity.this,
                R.style.DialogTheme)
                .setTitle(R.string.dlg_end)
                .setMessage(R.string.dlg_confirm_close_app)
                .setPositiveButton(R.string.dlg_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dlg, final int which) {
                        finish();
                    }
                })
                .setNegativeButton(R.string.dlg_no, null)
                .create().show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        FileHelper.getInstance().clearCacheFiles(this);
    }
}
