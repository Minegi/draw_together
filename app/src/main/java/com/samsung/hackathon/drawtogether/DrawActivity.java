package com.samsung.hackathon.drawtogether;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.pen.Spen;
import com.samsung.android.sdk.pen.engine.SpenSurfaceView;
import com.samsung.hackathon.drawtogether.util.SPenSdkUtils;

public class DrawActivity extends AppCompatActivity {

    private Context mContext;
    private SpenSurfaceView mSpenSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        App.L.debug("onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);
        mContext = getApplicationContext();

        initializeSPenSdk();
        createSPenSurfaceView();
    }

    @Override
    protected void onDestroy() {
        App.L.debug("onDestroy()");
        super.onDestroy();

        // destroy SpenSurfaceView
        if (mSpenSurfaceView != null) {
            mSpenSurfaceView.close();
            mSpenSurfaceView = null;
        }
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

    private void createSPenSurfaceView() {
        App.L.d("createSPenSurfaceView()");

        final RelativeLayout sPenViewLayout = (RelativeLayout) findViewById(R.id.spen_view_layout);
        mSpenSurfaceView = new SpenSurfaceView(mContext);

        if (mSpenSurfaceView == null) {
            Toast.makeText(mContext, "Can't create SPenSurfaceView!", Toast.LENGTH_LONG).show();
            finish();
        }

        sPenViewLayout.addView(mSpenSurfaceView);
    }
}
