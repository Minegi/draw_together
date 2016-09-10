package com.samsung.hackathon.drawtogether;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.pen.Spen;
import com.samsung.android.sdk.pen.SpenSettingEraserInfo;
import com.samsung.android.sdk.pen.SpenSettingPenInfo;
import com.samsung.android.sdk.pen.SpenSettingViewInterface;
import com.samsung.android.sdk.pen.document.SpenNoteDoc;
import com.samsung.android.sdk.pen.document.SpenPageDoc;
import com.samsung.android.sdk.pen.engine.SpenColorPickerListener;
import com.samsung.android.sdk.pen.engine.SpenSurfaceView;
import com.samsung.android.sdk.pen.engine.SpenTouchListener;
import com.samsung.android.sdk.pen.settingui.SpenSettingEraserLayout;
import com.samsung.android.sdk.pen.settingui.SpenSettingPenLayout;
import com.samsung.hackathon.drawtogether.util.SPenSdkUtils;

import java.io.IOException;

public class DrawActivity extends AppCompatActivity {

    private Context mContext;

    private SpenSurfaceView mSpenSurfaceView;
    private SpenNoteDoc mSpenNoteDoc;
    private SpenPageDoc mSpenPageDoc;

    private boolean mIsSpenFeatureEnabled;

    // constants
    private final int SPEN = SpenSettingViewInterface.TOOL_SPEN;

    // layouts
    private SpenSettingPenLayout mPenSettingView;
    private SpenSettingEraserLayout mEraserSettingView;
    private FrameLayout mSpenViewContainer;
    private RelativeLayout mSpenViewLayout;

    // buttons
    private ImageView mPenBtn;
    private ImageView mEraserBtn;
    private ImageView mUndoBtn;
    private ImageView mRedoBtn;

    // listeners
    private final View.OnClickListener mPenBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
            if (mPenSettingView.isShown()) {
                mPenSettingView.setVisibility(View.GONE);
            } else {
                mPenSettingView.setViewMode(SpenSettingPenLayout.VIEW_MODE_NORMAL);
                // TODO: [Low] 나타날 때 포지션 지정 (setPosition)
                mPenSettingView.setVisibility(View.VISIBLE);
            }
            mSpenSurfaceView.setToolTypeAction(SPEN, SpenSurfaceView.ACTION_STROKE);
            selectButton(mPenBtn);
        }
    };

    private final View.OnClickListener mEraserBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
            if (mEraserSettingView.isShown()) {
                mEraserSettingView.setVisibility(View.GONE);
            } else {
                // TODO: [Low] 나타날 때 포지션 지정 (setPosition)
                mEraserSettingView.setVisibility(View.VISIBLE);
            }
            mSpenSurfaceView.setToolTypeAction(SPEN, SpenSurfaceView.ACTION_ERASER);
            selectButton(mEraserBtn);
        }
    };

    private final View.OnClickListener mUndoAndRedoBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
            if (mSpenPageDoc == null) {
                return;
            }

            if (view.equals(mUndoBtn)) {
                if (mSpenPageDoc.isUndoable()) {
                    SpenPageDoc.HistoryUpdateInfo[] userData = mSpenPageDoc.undo();
                    mSpenSurfaceView.updateUndo(userData);
                }
            } else if (view.equals(mRedoBtn)) {
                if (mSpenPageDoc.isRedoable()) {
                    SpenPageDoc.HistoryUpdateInfo[] userData = mSpenPageDoc.redo();
                    mSpenSurfaceView.updateRedo(userData);
                }
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

    private final SpenSettingEraserLayout.EventListener mEraserListener =
            new SpenSettingEraserLayout.EventListener() {
        @Override
        public void onClearAll() {
            // TODO: [Low] 모두 지우시겠습니까? 같은 confirm dialog 달아야 됨
            mSpenPageDoc.removeAllObject();
            mSpenSurfaceView.update();
        }
    };

    private final SpenTouchListener mPreTouchSurfaceViewListener = new SpenTouchListener() {
        @Override
        public boolean onTouch(final View view, final MotionEvent motionEvent) {
            // Drawing 중에는 버튼을 disable 시킨다
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN:
                    enableButton(false);
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    enableButton(true);
                    break;
            }

            return false;
        }
    };

    private final SpenPageDoc.HistoryListener mHistoryListener = new SpenPageDoc.HistoryListener() {
        @Override
        public void onCommit(final SpenPageDoc pageDoc) {

        }

        @Override
        public void onUndoable(final SpenPageDoc pageDoc, final boolean undoable) {
            mUndoBtn.setEnabled(undoable);
        }

        @Override
        public void onRedoable(final SpenPageDoc pageDoc, final boolean redoable) {
            mRedoBtn.setEnabled(redoable);
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
        initializeSettingInfo();

        initializeOtherViews();
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

        if (mPenSettingView != null) {
            mPenSettingView.close();
        }

        if (mEraserSettingView != null) {
            mEraserSettingView.close();
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
                // TODO: [Low] 스트링 리소스화
                Toast.makeText(mContext, "This device cannot support Spen SDK.",
                        Toast.LENGTH_LONG).show();
                finish();
            }
        } catch (Exception e) {
            // TODO: [Low] 스트링 리소스화
            Toast.makeText(mContext, "Cannot initialize Spen SDK.",
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
            finish();
        }
    }

    private void createSpenSettingView() {
        App.L.d("createSpenSettingView()");

        mPenSettingView = new SpenSettingPenLayout(mContext, "", mSpenViewLayout);
        mEraserSettingView = new SpenSettingEraserLayout(mContext, "", mSpenViewLayout);

        mSpenViewContainer.addView(mPenSettingView);
        mSpenViewContainer.addView(mEraserSettingView);
    }

    private void createSpenSurfaceView() {
        App.L.d("createSpenSurfaceView()");

        mSpenSurfaceView = new SpenSurfaceView(mContext);

        if (mSpenSurfaceView == null) {
            // TODO: [Low] 스트링 리소스화
            Toast.makeText(mContext, "Can't create SpenSurfaceView!", Toast.LENGTH_LONG).show();
            finish();
        }

        mSpenSurfaceView.setToolTipEnabled(true);
        mSpenViewLayout.addView(mSpenSurfaceView);

        mPenSettingView.setCanvasView(mSpenSurfaceView);
        mEraserSettingView.setCanvasView(mSpenSurfaceView);
    }

    private void createSpenNoteDoc() {
        App.L.d("createSpenNoteDoc()");
        Display display = getWindowManager().getDefaultDisplay();
        Rect rect = new Rect();
        display.getRectSize(rect);

        try {
            mSpenNoteDoc = new SpenNoteDoc(mContext, rect.width(), rect.height());
        } catch (IOException e) {
            // TODO: [Low] 스트링 리소스화
            Toast.makeText(mContext, "Can't create SPenNoteDoc", Toast.LENGTH_LONG).show();
            e.printStackTrace();
            finish();
        }
    }

    private void createSpenPageDoc() {
        App.L.d("createSpenPageDoc()");
        // append first page
        mSpenPageDoc = mSpenNoteDoc.appendPage();
        // TODO: [Low] 색상 리소스화
        mSpenPageDoc.setBackgroundColor(0xffffff);
        mSpenPageDoc.clearHistory();

        mSpenSurfaceView.setPageDoc(mSpenPageDoc, true);

        // 아직까지는 Spen 모드만 지원한다
        if (!mIsSpenFeatureEnabled) {
            mSpenSurfaceView.setToolTypeAction(SpenSurfaceView.TOOL_FINGER,
                    SpenSurfaceView.ACTION_STROKE);
            // TODO: [Low] 스트링 리소스화
            Toast.makeText(mContext, "Device does not support Spen.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initializeSettingInfo() {
        App.L.d("initializeSettingInfo()");

        // pen setting
        final SpenSettingPenInfo penInfo = new SpenSettingPenInfo();
        penInfo.color = Color.BLACK;
        penInfo.size = 10;
        mSpenSurfaceView.setPenSettingInfo(penInfo);
        mPenSettingView.setInfo(penInfo);

        // erase setting
        final SpenSettingEraserInfo eraserInfo = new SpenSettingEraserInfo();
        eraserInfo.size = 30;
        mSpenSurfaceView.setEraserSettingInfo(eraserInfo);
        mEraserSettingView.setInfo(eraserInfo);

        // register listeners
        mSpenSurfaceView.setColorPickerListener(mColorPickerListener);
        mSpenSurfaceView.setPreTouchListener(mPreTouchSurfaceViewListener);
        mSpenPageDoc.setHistoryListener(mHistoryListener);
        mEraserSettingView.setEraserListener(mEraserListener);

        // set stroke action
        mSpenSurfaceView.setToolTypeAction(SpenSettingViewInterface.TOOL_SPEN,
                SpenSurfaceView.ACTION_STROKE);
    }

    private void initializeOtherViews() {
        App.L.d("initializeOtherViews()");
        mPenBtn = (ImageView) findViewById(R.id.pen_btn);
        mPenBtn.setOnClickListener(mPenBtnClickListener);

        mEraserBtn = (ImageView) findViewById(R.id.eraser_btn);
        mEraserBtn.setOnClickListener(mEraserBtnClickListener);

        mUndoBtn = (ImageView) findViewById(R.id.undo_btn);
        mUndoBtn.setOnClickListener(mUndoAndRedoBtnClickListener);
        mUndoBtn.setEnabled(mSpenPageDoc.isUndoable());

        mRedoBtn = (ImageView) findViewById(R.id.redo_btn);
        mRedoBtn.setOnClickListener(mUndoAndRedoBtnClickListener);
        mRedoBtn.setEnabled(mSpenPageDoc.isRedoable());

        selectButton(mPenBtn);
    }

    private void selectButton(final View view) {
        mPenBtn.setSelected(false);
        mEraserBtn.setSelected(false);

        view.setSelected(true);

        // 선택한 버튼 이외의 나머지 설정 뷰들은 다 닫는다
        closeOtherSettingView(view);
    }

    private void enableButton(final boolean isEnable) {
        mPenBtn.setEnabled(isEnable);
        mEraserBtn.setEnabled(isEnable);
        mUndoBtn.setEnabled(isEnable && mSpenPageDoc.isUndoable());
        mRedoBtn.setEnabled(isEnable && mSpenPageDoc.isRedoable());
    }

    private void closeOtherSettingView(final View view) {

        if (view.getId() != mPenBtn.getId()) {
            mPenSettingView.setVisibility(SpenSurfaceView.GONE);
        }

        if (view.getId() != mEraserBtn.getId()) {
            mEraserSettingView.setVisibility(SpenSurfaceView.GONE);
        }
    }
}
