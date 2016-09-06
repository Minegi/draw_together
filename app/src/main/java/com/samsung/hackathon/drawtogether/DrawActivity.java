package com.samsung.hackathon.drawtogether;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.pen.Spen;
import com.samsung.hackathon.drawtogether.util.SPenSdkUtils;

public class DrawActivity extends AppCompatActivity {

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        App.L.debug("onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);
        mContext = getApplicationContext();

        initializeSPenSdk();
    }

    @Override
    protected void onDestroy() {
        App.L.debug("onDestroy()");
        super.onDestroy();


    }

    private void initializeSPenSdk() {
        App.L.d("initializeSPenSdk()");

        boolean isSPenFeatureEnabled = false;
        Spen sPenPackage = new Spen();
        try {
            sPenPackage.initialize(this);
            isSPenFeatureEnabled = sPenPackage.isFeatureEnabled(Spen.DEVICE_PEN);
            App.L.d("isSpenFeatureEnabled=" + isSPenFeatureEnabled);
        } catch (SsdkUnsupportedException e) {
            if (SPenSdkUtils.processUnsupportedException(this, e)) {
                return;
            }
        } catch (Exception e) {
            Toast.makeText(mContext, "Cannot initialize SPen SDK.",
                    Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            finish();
        }
    }

}
