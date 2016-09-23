package com.samsung.hackathon.drawtogether.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.pen.Spen;
import com.samsung.android.sdk.pen.SpenSettingEraserInfo;
import com.samsung.android.sdk.pen.SpenSettingPenInfo;
import com.samsung.android.sdk.pen.SpenSettingViewInterface;
import com.samsung.android.sdk.pen.document.SpenNoteDoc;
import com.samsung.android.sdk.pen.document.SpenObjectBase;
import com.samsung.android.sdk.pen.document.SpenObjectStroke;
import com.samsung.android.sdk.pen.document.SpenPageDoc;
import com.samsung.android.sdk.pen.engine.SpenColorPickerListener;
import com.samsung.android.sdk.pen.engine.SpenPenChangeListener;
import com.samsung.android.sdk.pen.engine.SpenSurfaceView;
import com.samsung.android.sdk.pen.engine.SpenTouchListener;
import com.samsung.android.sdk.pen.settingui.SpenPenPresetPreviewManager;
import com.samsung.android.sdk.pen.settingui.SpenSettingEraserLayout;
import com.samsung.android.sdk.pen.settingui.SpenSettingPenLayout;
import com.samsung.hackathon.drawtogether.App;
import com.samsung.hackathon.drawtogether.R;
import com.samsung.hackathon.drawtogether.communication.ServerInterface;
import com.samsung.hackathon.drawtogether.communication.ServerInterface.ServerApiEventListener;
import com.samsung.hackathon.drawtogether.model.StepModel;
import com.samsung.hackathon.drawtogether.model.StrokeModel;
import com.samsung.hackathon.drawtogether.util.BitmapUtils;
import com.samsung.hackathon.drawtogether.util.FileHelper;
import com.samsung.hackathon.drawtogether.util.SPenSdkUtils;
import com.samsung.hackathon.drawtogether.util.StrokeModelConvertUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Response;

public class CreatorActivity extends AppCompatActivity {

    private Context mContext;

    private SpenSurfaceView mSpenSurfaceView;
    private SpenNoteDoc mSpenNoteDoc;
    private SpenPageDoc mSpenPageDoc;

    private boolean mIsSpenFeatureEnabled;
    private int mPresetViewMode;
    private List<SpenSettingPenInfo> mFavoritePenList;

    private SpenPenPresetPreviewManager mPenPresetPreviewManager;

    private ArrayList<StrokeModel> mStrokeList;
    private ArrayList<StepModel> mStepModelList;
    private String mTempFileName;

    // constants
    private final int SPEN = SpenSettingViewInterface.TOOL_SPEN;

    private final float SCREEN_UNIT = 360.0f;
    private static final int PRESET_MODE_VIEW = 1;
    private static final int PRESET_MODE_EDIT = 2;
    private static final int MAX_NUM_OF_PRESET = 9;

    // layouts
    private SpenSettingPenLayout mPenSettingView;
    private SpenSettingEraserLayout mEraserSettingView;
    private FrameLayout mSpenViewContainer;
    private RelativeLayout mSpenViewLayout;
    private LinearLayout mPresetLayout;

    // buttons
    private ImageButton mPenBtn;
    private ImageButton mEraserBtn;
    private ImageButton mUndoBtn;
    private ImageButton mRedoBtn;
    private ImageButton mSaveBtn;
    private ImageButton mNextStepBtn;
    private ImageButton mShowPresetBtn;
    private ImageButton mAddPresetBtn;
    private Button mEditPresetBtn;

    private EditText mArtworkTitle;

    // dialogs
    private AlertDialog mUploadConfirmDlg;
    private AlertDialog mDeleteAllDlg;
    private AlertDialog mNextStepDlg;

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

    private final View.OnClickListener mSaveBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
            if (isStrokeDataExist()) {
                if (mUploadConfirmDlg != null) {
                    mUploadConfirmDlg.show();
                }
            } else {
                Toast.makeText(CreatorActivity.this, R.string.draw_data_isnt_exist,
                        Toast.LENGTH_LONG).show();
            }
        }
    };

    private final SpenSettingPenLayout.PresetListener mPresetListener
            = new SpenSettingPenLayout.PresetListener() {
        @Override
        public void onAdded(final SpenSettingPenInfo info) {
            if (info != null) {
                // 최대 갯수 체크
                if (mFavoritePenList.size() == MAX_NUM_OF_PRESET) {
                    Toast.makeText(mContext, getString(R.string.preset_is_full,
                            Integer.toString(MAX_NUM_OF_PRESET)), Toast.LENGTH_LONG).show();
                    return;
                }

                // 이미 같은 설정의 펜이 있으면 추가하지 않는다
                for (int i = 0; i < mFavoritePenList.size(); ++i) {
                    if (isEqualsPenInfo(mFavoritePenList.get(i), info)) {
                        Toast.makeText(mContext, getString(R.string.is_already_exist_same_pen),
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                }

                App.L.d("[PenPresetAdded] name=" + info.name + ",color="
                        + Integer.toHexString(info.color) + ",size=" + info.size
                        + ",advSetting=" + info.advancedSetting);
                final SpenSettingPenInfo tmpInfo = new SpenSettingPenInfo();
                tmpInfo.name = info.name;
                tmpInfo.color = info.color;
                tmpInfo.size = info.size;
                tmpInfo.advancedSetting = info.advancedSetting;
                mPenSettingView.setInfo(tmpInfo);
                mFavoritePenList.add(tmpInfo);
            }
            mPenSettingView.setVisibility(View.GONE);
            mEraserSettingView.setVisibility(View.GONE);
            initializePresetLayout();
        }

        @Override
        public void onDeleted(final int idx) {
            if (idx < mPresetLayout.getChildCount() - 1) {
                mPresetLayout.removeViewAt(idx);
            }
        }

        @Override
        public void onSelected(final int idx) {

        }

        @Override
        public void onChanged(final int idx) {

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
            if (mDeleteAllDlg != null) {
                mDeleteAllDlg.show();
            }
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

    private final SpenPageDoc.ObjectListener mSpenObjectEventListener =
            new SpenPageDoc.ObjectListener() {
                @Override
                public void onObjectAdded(final SpenPageDoc spenPageDoc,
                                          final ArrayList<SpenObjectBase> arrayList,
                                          final int type) {
                    if (mStrokeList == null) {
                        return;
                    }

                    final int objCnt = mSpenPageDoc.getObjectCount(SpenPageDoc.FIND_TYPE_ALL,
                            false);
                    App.L.d("[ObjectAdded] objCnt=" + objCnt);
                    final SpenObjectStroke spenStrokeObj =
                            (SpenObjectStroke) arrayList.get(arrayList.size() - 1);

                    if (spenStrokeObj instanceof SpenObjectStroke) {
                        final StrokeModel strokeModel =
                                StrokeModelConvertUtils.convertToStrokeModel(spenStrokeObj);
                        App.L.d(strokeModel.toString());
                        mStrokeList.add(strokeModel);
                        enableNextStepBtn(!mStrokeList.isEmpty());

                        App.L.d("[ObjectAdded] added!");
                    }
                }

                @Override
                public void onObjectRemoved(final SpenPageDoc spenPageDoc,
                                            final ArrayList<SpenObjectBase> arrayList,
                                            final int type) {
                    if (mStrokeList == null) {
                        return;
                    }

                    final int objCnt = mSpenPageDoc.getObjectCount(SpenPageDoc.FIND_TYPE_ALL,
                            false);
                    App.L.d("[ObjectRemoved] objCnt=" + objCnt);
                    mStrokeList.remove(mStrokeList.size() - 1);

                    enableNextStepBtn(!mStrokeList.isEmpty());
                    App.L.d("[ObjectRemoved] removed!");
                }

                @Override
                public void onObjectChanged(final SpenPageDoc spenPageDoc,
                                            final SpenObjectBase spenObjectBase,
                                            final int type) {
                    if (mStrokeList == null) {
                        return;
                    }

                    App.L.d("[ObjectChanged] start");
                    final int objCnt = mSpenPageDoc.getObjectCount(SpenPageDoc.FIND_TYPE_ALL,
                            false);
                    App.L.d("[ObjCnt] " + objCnt);
                }
            };

    private final View.OnClickListener mShowPresetListener = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
            mShowPresetBtn.setVisibility(View.GONE);
            mEditPresetBtn.setVisibility(View.VISIBLE);
            mPresetLayout.setVisibility(View.VISIBLE);
            setPresetViewMode(PRESET_MODE_VIEW);
        }
    };

    private final View.OnClickListener mNextStepBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
            if (mNextStepDlg != null) {
                mNextStepDlg.show();
            }
        }
    };

    private final View.OnClickListener mAddPresetListener = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
            mPenSettingView.setViewMode(SpenSettingPenLayout.VIEW_MODE_FAVORITE);
            mPenSettingView.setVisibility(View.VISIBLE);
        }
    };

    private final View.OnClickListener mEditPresetListener = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
            if (mPresetViewMode == PRESET_MODE_EDIT) {
                setPresetViewMode(PRESET_MODE_VIEW);
            } else {
                setPresetViewMode(PRESET_MODE_EDIT);
            }
        }
    };

    private final View.OnClickListener mSelectPresetListener = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
            if (mPresetViewMode == PRESET_MODE_EDIT) {
                return;
            }

            final String[] presetInfo = ((String)view.getTag()).split("\\s+");
            if (presetInfo.length >= 3) {
                for (int i = 0; i < mFavoritePenList.size(); ++i) {
                    final SpenSettingPenInfo curPenInfo = mFavoritePenList.get(i);
                    if (curPenInfo.name.equalsIgnoreCase(presetInfo[0])
                            && mFavoritePenList.get(i).color == Integer.parseInt(presetInfo[1])
                            && mFavoritePenList.get(i).size == Float.parseFloat(presetInfo[2])) {
                        final SpenSettingPenInfo settingPenInfo = new SpenSettingPenInfo();
                        settingPenInfo.name = presetInfo[0];
                        settingPenInfo.color = Integer.parseInt(presetInfo[1]);
                        settingPenInfo.size = Float.parseFloat(presetInfo[2]);
                        if (presetInfo.length >= 4) {
                            settingPenInfo.advancedSetting = curPenInfo.advancedSetting;
                        }
                        mSpenSurfaceView.setPenSettingInfo(settingPenInfo);
                        mPenSettingView.setInfo(settingPenInfo);
                        selectPresetByIndex(i);
                        return;
                    }
                }
            }
        }
    };

    private final View.OnLongClickListener mPresetLongClickListener =
            new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(final View view) {
                    if (mPresetViewMode == PRESET_MODE_EDIT) {
                        setPresetViewMode(PRESET_MODE_VIEW);
                    } else {
                        setPresetViewMode(PRESET_MODE_EDIT);
                    }
                    return true;
                }
            };

    private final View.OnClickListener mDeletePresetListener = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
            final String[] presetInfo = ((String)view.getTag()).split("\\s+");
            if (presetInfo.length >= 3) {
                final SpenSettingPenInfo info = new SpenSettingPenInfo();
                info.name = presetInfo[0];
                info.color = Integer.parseInt(presetInfo[1]);
                info.size = Float.parseFloat(presetInfo[2]);
                if (presetInfo.length >= 4) {
                    info.advancedSetting = presetInfo[3];
                }

                for (int i = 0; i < mFavoritePenList.size(); ++i) {
                    if (!isEqualsPenInfoAndNotBeautify(mFavoritePenList.get(i), info)) {
                        continue;
                    }

                    // 모든 조건이 일치하므로 삭제하고 loop를 나간다.
                    mFavoritePenList.remove(i);
                    mPresetLayout.removeViewAt(i);
                    break;
                }
            }
        }
    };

    private final SpenPenChangeListener mPenChangeListener = new SpenPenChangeListener() {
        @Override
        public void onChanged(final SpenSettingPenInfo info) {
            int selectedIdx = -1;
            for (int i = 0; i < mFavoritePenList.size(); ++i) {
                final SpenSettingPenInfo presetInfo = mFavoritePenList.get(i);
                if (!isEqualsPenInfo(presetInfo, info)) {
                    continue;
                }

                selectedIdx = i;
                break;
            }
            selectPresetByIndex(selectedIdx);
        }
    };

    private final View.OnTouchListener mTouchSurfaceViewListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(final View view, final MotionEvent event) {
            if (mSpenSurfaceView == null || mSpenPageDoc == null) {
                return false;
            }

            final int pointerIdx = 0;

            // 펜 데이터만 처리
            if (event.getToolType(pointerIdx) != MotionEvent.TOOL_TYPE_STYLUS) {
                closePresetLayout();
                return false;
            }

            final int action = event.getAction();
            final int penAction = mSpenSurfaceView.getToolTypeAction(SPEN);

            if (action == MotionEvent.ACTION_DOWN) {
                if (penAction == SpenSurfaceView.ACTION_STROKE
                        || penAction == SpenSurfaceView.ACTION_ERASER) {
                    // Stroke or Erase 중에는 preset layout은 GONE 상태로 설정한다.
                    closePresetLayout();
                }
            }

            return false;
        }
    };

    private ServerApiEventListener<ResponseBody> mFileUploadEventListener =
            new ServerApiEventListener<ResponseBody>() {
                @Override
                public void onResponse(final Response<ResponseBody> response) {
                    App.L.d("response.code()=" + response.code());
                    Toast.makeText(mContext, R.string.upload_complete, Toast.LENGTH_LONG).show();
                    finish();
                }

                @Override
                public void onFailure(final Throwable t) {
                    App.L.e(t.getMessage());
                    showCantUploadFileToast();
                }
            };

    private void selectPresetByIndex(final int selectedIdx) {
        final float density = getResources().getDisplayMetrics().density;
        for (int i = 0; i < mPresetLayout.getChildCount() - 1; ++i) {
            final SpenSettingPenInfo curPenInfo = mFavoritePenList.get(i);
            if (i < mFavoritePenList.size()) {
                final FrameLayout buttonPresetLayout = (FrameLayout) mPresetLayout.getChildAt(i);
                final ImageView penImg = (ImageView) buttonPresetLayout.findViewById(R.id.pen_img);
                final ViewGroup.LayoutParams params = penImg.getLayoutParams();
                if (i == selectedIdx) {
                    params.height = (int)(62f * density);
                    penImg.setLayoutParams(params);
                    penImg.setImageResource(getPenImgID(curPenInfo.name));
                } else {
                    if (params.height == (int) (62f * density)) {
                        params.height = (int) (47f * density);
                        penImg.setLayoutParams(params);
                        penImg.setImageBitmap(BitmapUtils.cropBitmap(
                                BitmapFactory.decodeResource(getResources(),
                                        getPenImgID(curPenInfo.name)), 32, 47, density));
                    }
                }
            }
        }
    }

    private void selectPresetByInfo(final SpenSettingPenInfo penInfo) {
        for (int i = 0; i < mFavoritePenList.size(); ++i) {
            if (isEqualsPenInfoAndNotBeautify(mFavoritePenList.get(i), penInfo)) {
                continue;
            }

            selectPresetByIndex(i);
            break;
        }
    }

    private boolean isEqualsPenInfo(final SpenSettingPenInfo lhs, final SpenSettingPenInfo rhs) {
        if (!lhs.name.equalsIgnoreCase(rhs.name)) {
            return false;
        }
        if (lhs.size != rhs.size) {
            return false;
        }
        if (!lhs.advancedSetting.equalsIgnoreCase(rhs.advancedSetting)) {
            return false;
        }
        if (lhs.color != rhs.color) {
            return false;
        }
        return true;
    }

    private boolean isEqualsPenInfoAndNotBeautify(final SpenSettingPenInfo lhs,
                                                  final SpenSettingPenInfo rhs) {
        if (!lhs.name.equalsIgnoreCase(rhs.name)) {
            return false;
        }
        if (lhs.size != rhs.size) {
            return false;
        }
        if (!lhs.advancedSetting.equalsIgnoreCase(rhs.advancedSetting)
                && getString(R.string.beautify_path).equals(lhs.name)) {
            return false;
        }
        if (lhs.color != rhs.color) {
            return false;
        }

        return true;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        App.L.d("");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creator);
        mContext = getApplicationContext();

        setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary));

        mSpenViewContainer = (FrameLayout) findViewById(R.id.spen_view_container);
        mSpenViewLayout = (RelativeLayout) findViewById(R.id.spen_view_layout);

        initializeSpenSdk();
        createSpenSettingView();
        createSpenSurfaceView();
        createSpenNoteDoc();
        createSpenPageDoc();
        initializeSettingInfo();

        initializeOtherViews();
        createDialogs();
        initializeStrokeDataModels();
        loadFavoritePenList();

        mPenPresetPreviewManager = new SpenPenPresetPreviewManager(mContext);

        initializePresetLayout();
        setPresetViewMode(PRESET_MODE_VIEW);
    }

    @Override
    protected void onPause() {
        App.L.d("");
        super.onPause();
        savePreset();
    }

    @Override
    protected void onDestroy() {
        App.L.d("");
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
                App.L.e("IOException occurred");
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
        App.L.d("");

        mIsSpenFeatureEnabled = false;
        Spen sPenPackage = new Spen();
        try {
            sPenPackage.initialize(this);
            mIsSpenFeatureEnabled = sPenPackage.isFeatureEnabled(Spen.DEVICE_PEN);
            App.L.d("isSpenFeatureEnabled=" + mIsSpenFeatureEnabled);
        } catch (SsdkUnsupportedException e) {
            App.L.e("SsdkUnsupportedException occurred");
            if (SPenSdkUtils.processUnsupportedException(this, e)) {
                mSpenSurfaceView.setToolTypeAction(SpenSurfaceView.TOOL_FINGER,
                        SpenSurfaceView.ACTION_STROKE);
                Toast.makeText(mContext, R.string.cant_support_spen_sdk, Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            App.L.e("Exception occurred");
            Toast.makeText(mContext, R.string.cant_initialize_spen_sdk, Toast.LENGTH_LONG).show();
            e.printStackTrace();
            finish();
        }
    }

    private void createSpenSettingView() {
        App.L.d("");

        mPenSettingView = new SpenSettingPenLayout(mContext, "", mSpenViewLayout);
        mEraserSettingView = new SpenSettingEraserLayout(mContext, "", mSpenViewLayout);

        mSpenViewContainer.addView(mPenSettingView);
        mSpenViewContainer.addView(mEraserSettingView);
    }

    private void createSpenSurfaceView() {
        App.L.d("");

        mSpenSurfaceView = new SpenSurfaceView(mContext);

        if (mSpenSurfaceView == null) {
            Toast.makeText(mContext, R.string.cant_create_surface_view, Toast.LENGTH_LONG).show();
            finish();
        }

        mSpenSurfaceView.setToolTipEnabled(true);
        mSpenViewLayout.addView(mSpenSurfaceView);

        mPenSettingView.setCanvasView(mSpenSurfaceView);
        mEraserSettingView.setCanvasView(mSpenSurfaceView);
    }

    private void createSpenNoteDoc() {
        App.L.d("");
        Display display = getWindowManager().getDefaultDisplay();
        Rect rect = new Rect();
        display.getRectSize(rect);

        try {
            mSpenNoteDoc = new SpenNoteDoc(mContext, rect.width(), rect.height());
        } catch (IOException e) {
            App.L.e("IOException occurred");
            Toast.makeText(mContext, R.string.cant_create_note_doc, Toast.LENGTH_LONG).show();
            e.printStackTrace();
            finish();
        }
    }

    private void createSpenPageDoc() {
        App.L.d("");
        // append first page
        mSpenPageDoc = mSpenNoteDoc.appendPage();
        mSpenPageDoc.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
        mSpenPageDoc.clearHistory();

        mSpenSurfaceView.setPageDoc(mSpenPageDoc, true);

        // 아직까지는 Spen 모드만 지원한다
        if (!mIsSpenFeatureEnabled) {
            Toast.makeText(mContext, R.string.device_doesnt_support_spen, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initializeSettingInfo() {
        App.L.d("");

        // pen setting
        final SpenSettingPenInfo penInfo = new SpenSettingPenInfo();
        penInfo.name = getString(R.string.pencil_path);
        penInfo.color = Color.BLACK;
        penInfo.size = 50;
        mSpenSurfaceView.setPenSettingInfo(penInfo);
        mPenSettingView.setInfo(penInfo);

        // erase setting
        final SpenSettingEraserInfo eraserInfo = new SpenSettingEraserInfo();
        eraserInfo.size = 30;
        mSpenSurfaceView.setEraserSettingInfo(eraserInfo);
        mEraserSettingView.setInfo(eraserInfo);

        // register layout listeners
        mPenSettingView.setPresetListener(mPresetListener);
        mSpenSurfaceView.setColorPickerListener(mColorPickerListener);
        mSpenSurfaceView.setPreTouchListener(mPreTouchSurfaceViewListener);
        mSpenSurfaceView.setPenChangeListener(mPenChangeListener);
        mSpenSurfaceView.setOnTouchListener(mTouchSurfaceViewListener);
        mSpenPageDoc.setHistoryListener(mHistoryListener);
        mSpenPageDoc.setObjectListener(mSpenObjectEventListener);
        mEraserSettingView.setEraserListener(mEraserListener);

        // set stroke action
        mSpenSurfaceView.setToolTypeAction(SpenSettingViewInterface.TOOL_SPEN,
                SpenSurfaceView.ACTION_STROKE);
        mSpenSurfaceView.setToolTypeAction(SpenSettingViewInterface.TOOL_FINGER,
                SpenSurfaceView.ACTION_GESTURE);

        // disable zoom action
        mSpenSurfaceView.setZoomable(true);
    }

    private void initializeOtherViews() {
        App.L.d("");
        // bind views and listeners
        mPenBtn = (ImageButton) findViewById(R.id.pen_btn);
        mPenBtn.setOnClickListener(mPenBtnClickListener);

        mShowPresetBtn = (ImageButton) findViewById(R.id.show_preset_btn);
        mShowPresetBtn.setOnClickListener(mShowPresetListener);

        mNextStepBtn = (ImageButton) findViewById(R.id.next_step_btn);
        mNextStepBtn.setOnClickListener(mNextStepBtnClickListener);
        enableNextStepBtn(false);

        mAddPresetBtn = (ImageButton) findViewById(R.id.add_preset_btn);
        mAddPresetBtn.setOnClickListener(mAddPresetListener);
        setRippleBackground(mAddPresetBtn);

        mEditPresetBtn = (Button) findViewById(R.id.edit_preset_btn);
        mEditPresetBtn.setOnClickListener(mEditPresetListener);
        setRippleBackground(mEditPresetBtn);

        mEraserBtn = (ImageButton) findViewById(R.id.eraser_btn);
        mEraserBtn.setOnClickListener(mEraserBtnClickListener);

        mUndoBtn = (ImageButton) findViewById(R.id.undo_btn);
        mUndoBtn.setOnClickListener(mUndoAndRedoBtnClickListener);
        mUndoBtn.setEnabled(mSpenPageDoc.isUndoable());

        mRedoBtn = (ImageButton) findViewById(R.id.redo_btn);
        mRedoBtn.setOnClickListener(mUndoAndRedoBtnClickListener);
        mRedoBtn.setEnabled(mSpenPageDoc.isRedoable());

        mSaveBtn = (ImageButton) findViewById(R.id.save_btn);
        mSaveBtn.setOnClickListener(mSaveBtnClickListener);

        mPresetLayout = (LinearLayout) findViewById(R.id.preset_layout);

        mArtworkTitle = new EditText(this);

        selectButton(mPenBtn);
    }

    private void createDialogs() {
        App.L.d("");

        mDeleteAllDlg = new AlertDialog.Builder(CreatorActivity.this,
                R.style.DialogTheme)
                .setTitle(R.string.dlg_delete_all)
                .setMessage(R.string.dlg_delete_all_description)
                .setPositiveButton(R.string.dlg_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dlg, int which) {
                        mSpenPageDoc.removeAllObject();
                        mSpenSurfaceView.update();
                        mSpenPageDoc.clearHistory();
                        mStrokeList.clear();
                        mStepModelList.clear();
                        enableNextStepBtn(false);
                    }
                })
                .setNegativeButton(R.string.dlg_no, null)
                .create();

        mNextStepDlg = new AlertDialog.Builder(CreatorActivity.this,
                R.style.DialogTheme)
                .setTitle(R.string.dlg_next_step)
                .setMessage(R.string.dlg_next_step_description)
                .setPositiveButton(R.string.dlg_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dlg, final int which) {
                        addStrokeModelToStepModel();
                        moveToNextStep();
                    }
                })
                .setNegativeButton(R.string.dlg_no, null)
                .create();

        final int horizontalMargin = (int)(20 * getResources().getDisplayMetrics().density);

        mUploadConfirmDlg = new AlertDialog.Builder(CreatorActivity.this,
                R.style.DialogTheme)
                .setTitle(R.string.cd_save)
                .setMessage(R.string.dlg_save_and_upload_description)
                .setView(mArtworkTitle, horizontalMargin, 0, horizontalMargin, 0)
                .setPositiveButton(R.string.dlg_yes, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(final DialogInterface dlg, final int which) {
                        if (mArtworkTitle == null) {
                            showCantUploadFileToast();
                            return;
                        }

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                final String artworkTitle = mArtworkTitle.getText().toString();
                                if (artworkTitle == null || artworkTitle.isEmpty()) {
                                    CreatorActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(CreatorActivity.this,
                                                    R.string.input_title, Toast.LENGTH_LONG).show();
                                        }
                                    });
                                    return;
                                }
                                App.L.d("artworkTitle=" + artworkTitle);

                                // 중간저장하지 않은 판서 데이터가 있다면 stepModel에 저장
                                if (!mStrokeList.isEmpty()) {
                                    addStrokeModelToStepModel();
                                    CreatorActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            moveToNextStep();
                                        }
                                    });
                                }

                                final byte[] strokeData = serializeStrokeData();

                                if (strokeData == null) {
                                    App.L.e("strokeData is null");
                                    CreatorActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            showCantUploadFileToast();
                                        }
                                    });
                                    return;
                                }

                                String directoryPath = new StringBuilder(getExternalCacheDir()
                                        .getAbsolutePath()).toString();
                                String fileName = new String(mTempFileName + ".png");

                                final boolean resultOfCreateThumbnail = createThumbnail(
                                        directoryPath, fileName);

                                if (!resultOfCreateThumbnail) {
                                    App.L.e("create thumbnail is failed");
                                    CreatorActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            showCantUploadFileToast();
                                        }
                                    });
                                    return;
                                }

                                // 판서 데이터와 썸네일 서버로 전송
                                ServerInterface.getInstance().uploadFile(new File(directoryPath
                                + File.separator + fileName), mTempFileName + ".png",
                                        strokeData, mTempFileName + ".dat", artworkTitle,
                                        mFileUploadEventListener);

                                App.L.d("strokeDataSize=" + strokeData.length);
                            }
                        }).start();
                    }
                })
                .setNegativeButton(R.string.dlg_no, null)
                .create();
    }

    private void initializeStrokeDataModels() {
        App.L.d("");
        mStrokeList = new ArrayList<StrokeModel>();
        mStepModelList = new ArrayList<StepModel>();
        mTempFileName = UUID.randomUUID().toString();
    }

    private void loadFavoritePenList() {
        App.L.d("");
        mFavoritePenList = new ArrayList<SpenSettingPenInfo>();
        final SharedPreferences favoritePenPref =
                getSharedPreferences(getString(R.string.prefname_favorite_pen), MODE_PRIVATE);
        final int size = favoritePenPref.getInt(getString(R.string.prefattr_favorite_pen_size), 0);
        for (int i = 0; i < size; ++i) {
            String savedPenInfo =
                    favoritePenPref.getString(getString(R.string.pen).toLowerCase() + i, "");
            if (savedPenInfo != null) {
                String[] splitedPenInfo = savedPenInfo.split("\\s+");
                if (splitedPenInfo.length >= 3) {
                    final SpenSettingPenInfo penInfo = new SpenSettingPenInfo();
                    penInfo.name = splitedPenInfo[0];
                    penInfo.color = Integer.parseInt(splitedPenInfo[1]);
                    penInfo.size = Float.parseFloat(splitedPenInfo[2]);

                    if (splitedPenInfo.length >= 4 && splitedPenInfo[3] != null) {
                        penInfo.advancedSetting = splitedPenInfo[3];
                    } else {
                        penInfo.advancedSetting = "";
                    }
                    mFavoritePenList.add(penInfo);
                }
            }
        }
    }

    private void initializePresetLayout() {
        App.L.d("");

        mPresetLayout.removeAllViews();
        LayoutInflater inflater =
                (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final float density = getResources().getDisplayMetrics().density;

        for (int i = 0; i < mFavoritePenList.size(); ++i) {
            final FrameLayout buttonPresetLayout = (FrameLayout)
                    inflater.inflate(R.layout.button_preset, null);
            final ImageView previewImg =
                    (ImageView) buttonPresetLayout.findViewById(R.id.preview_img);
            final SpenSettingPenInfo curPenInfo = mFavoritePenList.get(i);

            if (curPenInfo.name.equals(getString(R.string.magic_pen_path))) {
                float penAlpha = (mFavoritePenList.get(i).color >> 24) & 0xFF;
                previewImg.setAlpha((float)(Math.round(penAlpha / 255.0F * 99) / 100.0));

                int progress = Math.round(((mFavoritePenList.get(i).size
                        * SCREEN_UNIT) / 1440 - 2.4f) * 10);
                float alphaWidth = (float)(progress * 6.3 / 140);
                previewImg.setImageBitmap(BitmapUtils.getResizedBitmap(
                        BitmapFactory.decodeResource(getResources(),
                                R.drawable.favorite_pen_preview_correctpen),
                        (int)(32 * density), (int)(alphaWidth * density)));
            } else {

                previewImg.setImageBitmap(mPenPresetPreviewManager.getPresetPreview(
                        curPenInfo.name, curPenInfo.color,
                        curPenInfo.size, curPenInfo.advancedSetting));
            }

            final ImageView penImg = (ImageView) buttonPresetLayout.findViewById(R.id.pen_img);
            penImg.setImageBitmap(BitmapUtils.cropBitmap(
                    BitmapFactory.decodeResource(getResources(),
                            getPenImgID(curPenInfo.name)), 32, 47, density));
            setRippleBackground(penImg);

            final String presetInfo = new StringBuilder(curPenInfo.name).append(' ')
                    .append(curPenInfo.color).append(' ').append(curPenInfo.size).append(' ')
                    .append(curPenInfo.advancedSetting).toString();
            penImg.setTag(presetInfo);
            penImg.setOnClickListener(mSelectPresetListener);
            penImg.setOnLongClickListener(mPresetLongClickListener);
            final ImageButton deleteImg =
                    (ImageButton) buttonPresetLayout.findViewById(R.id.delete_preset_btn);
            deleteImg.setTag(presetInfo);
            deleteImg.setOnClickListener(mDeletePresetListener);
            mPresetLayout.addView(buttonPresetLayout);
        }
        mPresetLayout.addView(mAddPresetBtn);
        selectPresetByInfo(mSpenSurfaceView.getPenSettingInfo());
    }

    private void selectButton(final View view) {
        mPenBtn.setSelected(false);
        mEraserBtn.setSelected(false);
        mSaveBtn.setSelected(false);

        view.setSelected(true);

        // 선택한 버튼 이외의 나머지 설정 뷰들은 다 닫는다
        closeOtherSettingView(view);
    }

    private void enableButton(final boolean isEnable) {
        mPenBtn.setEnabled(isEnable);
        mEraserBtn.setEnabled(isEnable);
        mUndoBtn.setEnabled(isEnable && mSpenPageDoc.isUndoable());
        mRedoBtn.setEnabled(isEnable && mSpenPageDoc.isRedoable());
        mSaveBtn.setEnabled(isEnable);
    }

    private void closeOtherSettingView(final View view) {
        if (view.getId() != mPenBtn.getId()) {
            mPenSettingView.setVisibility(SpenSurfaceView.GONE);
        }

        if (view.getId() != mEraserBtn.getId()) {
            mEraserSettingView.setVisibility(SpenSurfaceView.GONE);
        }
    }

    private void setPresetViewMode(final int viewMode) {
        mPresetViewMode = viewMode;
        // preset 추가/편집 버튼 표시 설정
        if (mPresetViewMode == PRESET_MODE_VIEW) {
            mEditPresetBtn.setText(R.string.edit);
            mAddPresetBtn.setVisibility(View.VISIBLE);
        } else if (mPresetViewMode == PRESET_MODE_EDIT) {
            mEditPresetBtn.setText(R.string.done);
            mAddPresetBtn.setVisibility(View.GONE);
        }

        // delete 버튼 표시 설정
        for (int i = 0; i < mPresetLayout.getChildCount() - 1; ++i) {
            final FrameLayout buttonPresetLayout = (FrameLayout) mPresetLayout.getChildAt(i);
            final ImageButton deleteBtn =
                    (ImageButton) buttonPresetLayout.findViewById(R.id.delete_preset_btn);
            if (mPresetViewMode == PRESET_MODE_VIEW) {
                deleteBtn.setVisibility(View.GONE);
            } else if (mPresetViewMode == PRESET_MODE_EDIT) {
                deleteBtn.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setRippleBackground(final View view) {
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            view.setBackground(new RippleDrawable(
                    ColorStateList.valueOf(Color.LTGRAY), null, null));
        }
    }

    private int getPenImgID(final String name) {
        int penImgID = R.drawable.favorite_pen_pencil;

        if (name.equalsIgnoreCase(getString(R.string.pencil_path))) {
            penImgID = R.drawable.favorite_pen_pencil;
        } else if (name.equalsIgnoreCase(getString(R.string.ink_pen_path))) {
            penImgID = R.drawable.favorite_pen_pen;
        } else if (name.equalsIgnoreCase(getString(R.string.marker_path))) {
            penImgID = R.drawable.favorite_pen_marker;
        } else if (name.equalsIgnoreCase(getString(R.string.chinese_brush_path))
                || name.equalsIgnoreCase(getString(R.string.beautify_path))
                || name.equalsIgnoreCase(getString(R.string.brush_path))) {
            penImgID = R.drawable.favorite_pen_chinabrush;
        } else if (name.equalsIgnoreCase(getString(R.string.magic_pen_path))) {
            penImgID = R.drawable.favorite_pen_correctpen;
        } else if (name.equalsIgnoreCase(getString(R.string.fountain_pen_path))) {
            penImgID = R.drawable.favorite_pen_fountainpen;
        } else if (name.equalsIgnoreCase(getString(R.string.marker_path))) {
            penImgID = R.drawable.favorite_pen_calligraphypen;
        }

        return penImgID;
    }

    private void savePreset() {
        App.L.d("");
        final SharedPreferences favoritePenPref =
                getSharedPreferences(getString(R.string.prefname_favorite_pen), MODE_PRIVATE);
        final SharedPreferences.Editor editor = favoritePenPref.edit();
        editor.putInt(getString(R.string.prefattr_favorite_pen_size), mFavoritePenList.size());

        for (int i = 0; i < mFavoritePenList.size(); ++i) {
            final SpenSettingPenInfo curPenInfo = mFavoritePenList.get(i);
            final String presetInfo = new StringBuilder(curPenInfo.name).append(' ')
                    .append(curPenInfo.color).append(' ').append(curPenInfo.size).append(' ')
                    .append(curPenInfo.advancedSetting).toString();
            editor.putString(getString(R.string.pen).toLowerCase() + i, presetInfo);
        }
        editor.commit();
    }

    private void closePresetLayout() {
        mPenSettingView.setVisibility(View.GONE);
        mEraserSettingView.setVisibility(View.GONE);
        mPresetLayout.setVisibility(View.GONE);
        mEditPresetBtn.setVisibility(View.GONE);
        mShowPresetBtn.setVisibility(View.VISIBLE);
    }

    private boolean createThumbnail(final String directoryPath, final String fileName) {
        boolean result = false;
        if (mSpenSurfaceView != null) {
            File dir = null;
            FileOutputStream out = null;
            Bitmap scaledBitmap = null;
            try {
                dir = new File(directoryPath);
                App.L.d(dir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                App.L.d(dir.getAbsolutePath() + File.separator + fileName);
                // TODO: 썸네일의 크기 변경 필요
                final float width = getResources().getDimension(R.dimen.thumbnail_width);
                final float height = getResources().getDimension(R.dimen.thumbnail_height);
                App.L.d("width=" + width + ",height=" + height);
                out = new FileOutputStream(dir.getAbsolutePath() + File.separator + fileName);
                scaledBitmap = Bitmap.createScaledBitmap(
                        mSpenSurfaceView.capturePage(1.0f), (int)width, (int)height, true);
                scaledBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                result = true;
            } catch (FileNotFoundException e) {
                App.L.e("FileNotFoundException occurred");
                e.printStackTrace();
            } catch (IOException e) {
                App.L.e("IOException occurred");
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                    App.L.d("scaledBitmapSize=" + scaledBitmap.getByteCount());
                    scaledBitmap.recycle();
                } catch (Exception e) {
                    App.L.e("Exception occurred");
                    e.printStackTrace();
                }
            }
        }

        App.L.d("result=" + result);
        return result;
    }

    private byte[] serializeStrokeData() {
        if (mStepModelList == null) {
            App.L.warning("mStepModelList is null");
            return null;
        }

        ObjectOutputStream oos = null;
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);

            oos.writeObject(mStepModelList);
            App.L.d("baos.toByteArray().length=" + baos.toByteArray().length);
            return baos.toByteArray();

        } catch (FileNotFoundException e) {
            App.L.e("FileNotFoundException occurred");
            e.printStackTrace();
        } catch (IOException e) {
            App.L.e("IOException occurred");
            e.printStackTrace();
        } finally {
            try {
                oos.close();
                baos.close();
            } catch (IOException e) {
                App.L.e("IOException occurred");
                e.printStackTrace();
            }
        }

        return null;
    }

    private boolean isStrokeDataExist() {
        if (mStrokeList == null || mStepModelList == null) {
            return false;
        }

        return !mStrokeList.isEmpty() || !mStepModelList.isEmpty();
    }

    private void addStrokeModelToStepModel() {
        // 현재까지의 단계를 저장
        final StepModel step = new StepModel();
        step.setStrokes(new ArrayList<StrokeModel>());
        step.getStrokes().addAll(mStrokeList);
        mStrokeList.clear();
        mStepModelList.add(step);
        App.L.d("step.getStrokes().size()=" + step.getStrokes().size()
                + ", mStepModelList.size()=" + mStepModelList.size());
    }

    private void moveToNextStep() {
        // 이전 step으로 undo가 되지 못하도록 설정하고 undo / redo 버튼의 상태를 갱신
        mSpenPageDoc.clearHistory();

        mUndoBtn.setEnabled(mSpenPageDoc.isUndoable());
        mRedoBtn.setEnabled(mSpenPageDoc.isRedoable());
        enableNextStepBtn(false);
    }

    private void enableNextStepBtn(final boolean isEnable) {
        if (isEnable) {
            mNextStepBtn.setAlpha(1.0f);
        } else {
            mNextStepBtn.setAlpha(0.3f);
        }
        mNextStepBtn.setEnabled(isEnable);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(CreatorActivity.this,
                R.style.DialogTheme)
                .setTitle(R.string.dlg_end)
                .setMessage(R.string.dlg_confirm_move_back)
                .setPositiveButton(R.string.dlg_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dlg, final int which) {
                        finish();
                    }
                })
                .setNegativeButton(R.string.dlg_no, null)
                .create().show();
    }

    private void setStatusBarColor(final int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(color);
        }
    }

    private void showCantUploadFileToast() {
        Toast.makeText(CreatorActivity.this, R.string.cant_upload_file, Toast.LENGTH_LONG).show();
    }
}