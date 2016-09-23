package com.samsung.hackathon.drawtogether.ui.model;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.samsung.hackathon.drawtogether.App;
import com.samsung.hackathon.drawtogether.R;
import com.samsung.hackathon.drawtogether.communication.ServerInterface;
import com.samsung.hackathon.drawtogether.ui.thumbnail.ImageLoader;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

/**
 * Created by 김시내 on 2016-09-17.
 */
public class ArtworkAdapter extends BaseAdapter {

    private ArrayList<ArtworkItem> mArtworkItemList = new ArrayList<>();
    private Context mContext;
    private ImageLoader mImageLoader;

    private ServerInterface.ServerApiEventListener<List<ArtworkItem>> mGetArtworkListEventListener
            = new ServerInterface.ServerApiEventListener<List<ArtworkItem>>() {
        @Override
        public void onResponse(Response<List<ArtworkItem>> response) {
            App.L.d("");
            List<ArtworkItem> artworkList = response.body();

            for (int i = 0; i < artworkList.size(); ++i) {
                final ArtworkItem item = artworkList.get(i);

                addItem(new ArtworkItem(item.name, item.strokeData,
                            ServerInterface.DOWNLOAD_URL + item.thumbnail));
                App.L.d("i=" + i + ", name=" + artworkList.get(i).name +
                        ", thumbnail=" + ServerInterface.DOWNLOAD_URL + item.thumbnail);
            }
        }

        @Override
        public void onFailure(Throwable t) {
            Activity activity = (Activity)mContext;
            if (mContext instanceof Activity) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, R.string.cant_get_artwork_list, Toast.LENGTH_LONG)
                        .show();
                    }
                });
            }

            App.L.e(t.getMessage());
        }
    };

    public ArtworkAdapter(final Context context) {
        mContext = context;
        mImageLoader = new ImageLoader(context);
        refresh();
    }

    private void refresh() {
        mArtworkItemList.clear();
        ServerInterface.getInstance().getArtworkList(mGetArtworkListEventListener);
    }

    // Adapter 에 사용되는 데이터의 개수를 리턴 : 필수구현
    @Override
    public int getCount() {
        if (mArtworkItemList != null) {
            return mArtworkItemList.size();
        }
        return 0;
    }

    // 지정한 위치(position)에 있는 데이터 리턴 : 필수구현
    @Override
    public Object getItem(int position) {
        if (mArtworkItemList != null) {
            return mArtworkItemList.get(position);
        }
        return null;
    }

    // 지정한 위치(position)에 있는 데이터와 관계된 아이템(row)의 ID 를 리턴 : 필수구현
    @Override
    public long getItemId(int position) {
        return position;
    }

    // position 에 위치한 데이터를 화면에 출력하는데 사용될 View 를 리턴 : 필수구현
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View columnView = convertView;
        ViewHolder viewHolder;

        // "artwork_item" Layout 을 inflate 하여 convertView 참조 획득
        if (columnView == null) {
            final LayoutInflater inflater = ((Activity)mContext).getLayoutInflater();
            columnView = inflater.inflate(R.layout.artwork_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.name = (TextView) columnView.findViewById(R.id.artwork_item_name);
            viewHolder.thumbnail =
                    (ImageButton) columnView.findViewById(R.id.artwork_item_thumbnail);
            columnView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)columnView.getTag();
        }

        final ArtworkItem item = mArtworkItemList.get(position);

        if (item != null) {
            viewHolder.name.setText(item.name);
            viewHolder.thumbnail.setContentDescription(item.name);
            viewHolder.thumbnail.setEnabled(false);
            viewHolder.thumbnail.setFocusable(false);
            viewHolder.thumbnail.setClickable(false);
            App.L.d(item.thumbnail);
            mImageLoader.DisplayImage(item.thumbnail, viewHolder.thumbnail);
        }

        return columnView;
    }

    // 아이템 데이터 추가를 위한 함수. 개발자가 원하는 대로 작성 가능.
    public void addItem(final ArtworkItem item) {
        if (mArtworkItemList != null) {
            mArtworkItemList.add(item);
            notifyDataSetChanged();
        }
    }

    class ViewHolder {
        public TextView name;
        public ImageButton thumbnail;
    }
}
