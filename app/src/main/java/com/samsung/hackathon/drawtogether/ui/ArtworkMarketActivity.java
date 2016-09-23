package com.samsung.hackathon.drawtogether.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.samsung.hackathon.drawtogether.App;
import com.samsung.hackathon.drawtogether.R;
import com.samsung.hackathon.drawtogether.communication.ServerInterface;
import com.samsung.hackathon.drawtogether.ui.model.ArtworkAdapter;
import com.samsung.hackathon.drawtogether.ui.model.ArtworkItem;

import java.io.File;

import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * Created by 김시내 on 2016-09-17.
 */
public class ArtworkMarketActivity extends AppCompatActivity {

    private GridView mGridView;
    private ArtworkAdapter mArtworkAdapter;
    private Context mContext;

    private String mStrokeDataPath;

    private ArtworkItem mSelectedItem;
    private AlertDialog mDownloadConfirmDlg;

    // listeners
    private AdapterView.OnItemClickListener mGridViewItemClickListener =
            new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                    mSelectedItem = (ArtworkItem) adapterView.getItemAtPosition(pos);
                    if (mDownloadConfirmDlg != null) {
                        mDownloadConfirmDlg.show();
                    }
                }
            };

    private ServerInterface.ServerApiEventListener<ResponseBody> mDownloadEventListener =
            new ServerInterface.ServerApiEventListener<ResponseBody>() {
                @Override
                public void onResponse(Response<ResponseBody> response) {
                    App.L.d("download completed");
                    Toast.makeText(mContext, R.string.download_completed, Toast.LENGTH_LONG).show();
                    final Intent intent = new Intent(mContext, ImitatorActivity.class);
                    intent.putExtra(getString(R.string.stroke_data_path), mStrokeDataPath);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onFailure(Throwable t) {
                    App.L.e(t.getMessage());
                    Toast.makeText(mContext, R.string.cant_download_file, Toast.LENGTH_LONG).show();
                }
            };

    private DialogInterface.OnClickListener mDownloadAgreeClickListener =
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialogInterface, final int which) {
                    if (mSelectedItem != null) {
                        final String dir = mContext.getExternalCacheDir() + File.separator;
                        ServerInterface.getInstance().downloadFile(mSelectedItem.name + ".dat",
                                dir, mDownloadEventListener);
                        mStrokeDataPath = dir + mSelectedItem.name + ".dat";
                        App.L.d(mStrokeDataPath);
                        mSelectedItem = null;
                    }
                }
            };

    private DialogInterface.OnClickListener mDownloadDisagreeClickListener =
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialogInterface, final int which) {
                    mSelectedItem = null;
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artwork_market);
        mContext = this;
        mSelectedItem = null;

        mArtworkAdapter = new ArtworkAdapter(this);

        mGridView = (GridView) findViewById(R.id.artwork_grid_view);
        mGridView.setAdapter(mArtworkAdapter);
        mGridView.setOnItemClickListener(mGridViewItemClickListener);

        mDownloadConfirmDlg = new AlertDialog.Builder(ArtworkMarketActivity.this,
                R.style.DialogTheme)
                .setTitle(R.string.dlg_download)
                .setMessage(R.string.dlg_confirm_download)
                .setPositiveButton(R.string.dlg_yes, mDownloadAgreeClickListener)
                .setNegativeButton(R.string.dlg_no, mDownloadDisagreeClickListener)
                .create();
    }
}
