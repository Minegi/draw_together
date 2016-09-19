package com.samsung.hackathon.drawtogether.ui.model;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.samsung.hackathon.drawtogether.R;

import java.util.ArrayList;

/**
 * Created by 김시내 on 2016-09-17.
 */
public class ArtworkAdapter extends BaseAdapter {

    private ArrayList<ArtworkItem> listViewItemList = new ArrayList<ArtworkItem>();

    public ArtworkAdapter() {

    }

    // Adapter 에 사용되는 데이터의 개수를 리턴 : 필수구현
    @Override
    public int getCount() {
        return listViewItemList.size();
    }

    // 지정한 위치(position)에 있는 데이터 리턴 : 필수구현
    @Override
    public Object getItem(int position) {
        return listViewItemList.get(position);
    }

    // 지정한 위치(position)에 있는 데이터와 관계된 아이템(row)의 ID 를 리턴 : 필수구현
    @Override
    public long getItemId(int position) {
        return position;
    }

    // position 에 위치한 데이터를 화면에 출력하는데 사용될 View 를 리턴 : 필수구현
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();

        // "artwork_item" Layout 을 inflate 하여 convertView 참조 획득
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.artwork_item, parent, false);
        }

        // 화면에 표시될 View(Layout 이 inflate 된) 으로부터 위젯에 대한 참조 획득
        ImageView iconImageView = (ImageView) convertView.findViewById(R.id.imageView1);
        TextView titleTextView = (TextView) convertView.findViewById(R.id.textView1);

        // Data Set(listViewItemList) 에서 position 에 위치한 데이터 참조 획득
        ArtworkItem artworkItem = listViewItemList.get(pos);

        // 아이템 내 각 위젯에 데이터 반영
        iconImageView.setImageDrawable(artworkItem.getIcon());
        titleTextView.setText(artworkItem.getTitle());

        return convertView;
    }

    // 아이템 데이터 추가를 위한 함수. 개발자가 원하는 대로 작성 가능.
    public void addItem(Drawable icon, String title) {
        ArtworkItem item = new ArtworkItem();

        item.setIcon(icon);
        item.setTitle(title);

        listViewItemList.add(item);
    }

    // 삭제
    public void deleteItem(int position) {
        listViewItemList.remove(position);
    }
}
