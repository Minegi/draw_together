package com.samsung.hackathon.drawtogether.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.samsung.hackathon.drawtogether.App;
import com.samsung.hackathon.drawtogether.R;
import com.samsung.hackathon.drawtogether.util.FileHelper;

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
