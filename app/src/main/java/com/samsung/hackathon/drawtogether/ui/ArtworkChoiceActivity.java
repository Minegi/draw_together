package com.samsung.hackathon.drawtogether.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;

import com.samsung.hackathon.drawtogether.R;
import com.samsung.hackathon.drawtogether.ui.model.ArtworkAdapter;
import com.samsung.hackathon.drawtogether.ui.model.ArtworkItem;

/**
 * Created by 김시내 on 2016-09-17.
 */
public class ArtworkChoiceActivity extends AppCompatActivity {

    private GridView mGridView;
    private ArtworkAdapter mArtworkAdapter;
    private Context mContext;

    // listeners
    private AdapterView.OnItemClickListener mGridViewItemClickListener =
            new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                    final ArtworkItem item = (ArtworkItem) adapterView.getItemAtPosition(pos);
                    Toast.makeText(mContext, item.name, Toast.LENGTH_LONG).show();
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artwork_choice);
        mContext = this;

        mArtworkAdapter = new ArtworkAdapter(this);

        mGridView = (GridView) findViewById(R.id.artwork_grid_view);
        mGridView.setAdapter(mArtworkAdapter);
        mGridView.setOnItemClickListener(mGridViewItemClickListener);
    }
}
