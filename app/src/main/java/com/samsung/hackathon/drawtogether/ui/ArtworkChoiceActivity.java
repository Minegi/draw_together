package com.samsung.hackathon.drawtogether.ui;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.samsung.hackathon.drawtogether.R;
import com.samsung.hackathon.drawtogether.ui.model.ArtworkAdapter;
import com.samsung.hackathon.drawtogether.ui.model.ArtworkItem;

/**
 * Created by 김시내 on 2016-09-17.
 */
public class ArtworkChoiceActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artwork_choice);

        ListView listview;
        ArtworkAdapter adapter;

        adapter = new ArtworkAdapter();

        listview = (ListView) findViewById(R.id.listview1);
        listview.setAdapter(adapter);

        // 서버로부터 데이터 받아서 item 추가
        adapter.addItem(ContextCompat.getDrawable(this, R.drawable.sketch1), "sketch 1");
        adapter.addItem(ContextCompat.getDrawable(this, R.drawable.sketch2), "sketch 2");
        adapter.addItem(ContextCompat.getDrawable(this, R.drawable.sketch3), "sketch 3");

        // 위에서 생성한 listView 에 클릭 이벤트 핸들러 정의
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // get item
                ArtworkItem item = (ArtworkItem) parent.getItemAtPosition(position);

                String titleStr = item.getTitle();
                Drawable iconDrawable = item.getIcon();

                Toast.makeText(parent.getContext(), "Click " + titleStr, Toast.LENGTH_SHORT).show();

                //TODO : use item data.
                // 해당 그림

            }
        });
    }
}
