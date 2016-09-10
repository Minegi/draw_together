package com.samsung.hackathon.drawtogether;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.pen.Spen;
import com.samsung.android.sdk.pen.SpenSettingPenInfo;
import com.samsung.android.sdk.pen.document.SpenNoteDoc;
import com.samsung.android.sdk.pen.document.SpenPageDoc;
import com.samsung.android.sdk.pen.engine.SpenColorPickerListener;
import com.samsung.android.sdk.pen.engine.SpenSurfaceView;
import com.samsung.android.sdk.pen.settingui.SpenSettingPenLayout;
import com.samsung.hackathon.drawtogether.util.SPenSdkUtils;

import java.io.IOException;

public class DrawActivity extends AppCompatActivity {

    private Context mContext;

    private SpenSurfaceView mSpenSurfaceView;
    private SpenNoteDoc mSpenNoteDoc;
    private SpenPageDoc mSpenPageDoc;

    private boolean mIsSpenFeatureEnabled;

    // layouts
    private SpenSettingPenLayout mPenSettingView;
    private FrameLayout mSpenViewContainer;
    private RelativeLayout mSpenViewLayout;

    // buttons
    private ImageView mPenBtn;

    // listeners
    private final View.OnClickListener mPenBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
            if (mPenSettingView.isShown()) {
                mPenSettingView.setVisibility(View.GONE);
            } else {
                mPenSettingView.setViewMode(SpenSettingPenLayout.VIEW_MODE_NORMAL);
                mPenSettingView.setVisibility(View.VISIBLE);
            }
        }
    };

    private final SpenColorPickerListener mColorPickerListener = new SpenColorPickerListener() {
        @Override
        public void onChanged(int color, int x, int y) {
            if (mPenSettingView != null) {
                SpenSettingPenInfo penInfo = mPenSettingView.getInfo();
                penInfo.color = color;
                mPenSettingView.setInfo(penInfo);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        App.L.debug("onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);
        mContext = getApplicationContext();

        mSpenViewContainer = (FrameLayout) findViewById(R.id.spen_view_container);
        mSpenViewLayout = (RelativeLayout) findViewById(R.id.spen_view_layout);

        initializeSpenSdk();
        createSpenSettingView();
        createSpenSurfaceView();
        createSpenNoteDoc();
        createSpenPageDoc();
        initializePenSettingInfo();

        initializeOtherViews();
    }

    private void initializeOtherViews() {
        mPenBtn = (ImageView) findViewById(R.id.pen_btn);
        mPenBtn.setOnClickListener(mPenBtnClickListener);
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

        // destroy SpenNoteDoc
        if (mSpenNoteDoc != null) {
            try {
                mSpenNoteDoc.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                mSpenNoteDoc = null;
            }
        }
    }

    private void initializeSpenSdk() {
        App.L.d("initializeSpenSdk()");

        mIsSpenFeatureEnabled = false;
        Spen sPenPackage = new Spen();
        try {
            sPenPackage.initialize(this);
            mIsSpenFeatureEnabled = sPenPackage.isFeatureEnabled(Spen.DEVICE_PEN);
            App.L.d("isSpenFeatureEnabled=" + mIsSpenFeatureEnabled);
        } catch (SsdkUnsupportedException e) {
            if (SPenSdkUtils.processUnsupportedException(this, e)) {
                return;
            }
        } catch (Exception e) {
            Toast.makeText(mContext, "Cannot initialize Spen SDK.",
                    Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            finish();
        }
    }

    private void createSpenSettingView() {
        App.L.d("createSpenSettingView()");

        mPenSettingView = new SpenSettingPenLayout(mContext, "", mSpenViewLayout);
        mSpenViewContainer.addView(mPenSettingView);
    }

    private void createSpenSurfaceView() {
        App.L.d("createSpenSurfaceView()");

        mSpenSurfaceView = new SpenSurfaceView(mContext);

        if (mSpenSurfaceView == null) {
            Toast.makeText(mContext, "Can't create SpenSurfaceView!", Toast.LENGTH_LONG).show();
            finish();
        }

        mSpenSurfaceView.setToolTipEnabled(true);
        mSpenViewLayout.addView(mSpenSurfaceView);
        mPenSettingView.setCanvasView(mSpenSurfaceView);
    }

    private void createSpenNoteDoc() {
        App.L.d("createSpenNoteDoc()");
        Display display = getWindowManager().getDefaultDisplay();
        Rect rect = new Rect();
        display.getRectSize(rect);

        try {
            mSpenNoteDoc = new SpenNoteDoc(mContext, rect.width(), rect.height());
        } catch (IOException e) {
            Toast.makeText(mContext, "Can't create SPenNoteDoc", Toast.LENGTH_LONG).show();
            e.printStackTrace();
            finish();
        }
    }

    private void createSpenPageDoc() {
        App.L.d("createSpenPageDoc()");
        mSpenPageDoc = mSpenNoteDoc.appendPage();
        mSpenPageDoc.setBackgroundColor(0xffffff);
        mSpenPageDoc.clearHistory();
        mSpenSurfaceView.setPageDoc(mSpenPageDoc, true);

        if (!mIsSpenFeatureEnabled) {
            mSpenSurfaceView.setToolTypeAction(SpenSurfaceView.TOOL_FINGER,
                    SpenSurfaceView.ACTION_STROKE);
            Toast.makeText(mContext, "Device does not support Spen.\n" +
                    "You can draw strokes with your finger.", Toast.LENGTH_LONG).show();
        }
    }

    private void initializePenSettingInfo() {
        App.L.d("initializePenSettingInfo()");
        final SpenSettingPenInfo penInfo = new SpenSettingPenInfo();
        penInfo.color = Color.BLACK;
        penInfo.size = 10;
        mSpenSurfaceView.setPenSettingInfo(penInfo);
        mPenSettingView.setInfo(penInfo);
        mSpenSurfaceView.setColorPickerListener(mColorPickerListener);
    }
}
