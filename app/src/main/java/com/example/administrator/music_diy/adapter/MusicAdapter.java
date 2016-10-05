package com.example.administrator.music_diy.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.administrator.music_diy.R;
import com.example.administrator.music_diy.util.MusicResource;

import java.text.SimpleDateFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2016/10/2.
 */

public class MusicAdapter extends BaseAdapter {
    private List<MusicResource> oList;
    private Context mContext;
    private LayoutInflater oInflater;
    private int selectedItem = -1;

    public MusicAdapter(List<MusicResource> oList, Context mContext){
        this.oList = oList;
        this.mContext = mContext;
        this.oInflater = LayoutInflater.from(mContext);
    }

    public void setSelectedItem(int selectedItem){
        this.selectedItem = selectedItem;

    }

    @Override
    public int getCount() {
        return oList.size();
    }

    @Override
    public Object getItem(int i) {
        return oList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup group) {
        ViewHolder mHolder;
        if (view == null){
            view = oInflater.inflate(R.layout.list_item ,null);
            mHolder = new ViewHolder(view);
            view.setTag(mHolder);
        }else{
            mHolder = (ViewHolder)view.getTag();
        }
        mHolder.img.setBackgroundResource(R.drawable.ic_launcher);
        mHolder.name.setText(oList.get(i).getName());
        mHolder.author.setText(oList.get(i).getAuthor());
        mHolder.duration.setText(getTime(oList.get(i).getDuration()));

        if (i == selectedItem){
            view.setBackgroundColor(Color.parseColor("#dbdbdb"));
            view.setSelected(true);
        }else{
            view.setBackgroundColor(Color.WHITE);
            view.setSelected(false);
        }

        return view;//栽在这里简直蠢到哭
    }

    private String getTime(long time){
        SimpleDateFormat formats = new SimpleDateFormat("mm:ss");
        String times = formats.format(time);
        return times;
    }

    class ViewHolder{
        @BindView(R.id.mu_img) ImageView img;
        @BindView(R.id.mu_name) TextView name;
        @BindView(R.id.mu_author) TextView author;
        @BindView(R.id.mu_time) TextView duration;

        public ViewHolder(View view){
            ButterKnife.bind(this,view);
        }
    }
}
