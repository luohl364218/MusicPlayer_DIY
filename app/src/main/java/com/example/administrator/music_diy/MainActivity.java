package com.example.administrator.music_diy;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.KeyEvent;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.music_diy.adapter.MusicAdapter;
import com.example.administrator.music_diy.util.MusicResource;
import com.example.administrator.music_diy.util.MusicUtil;

import java.util.ArrayList;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends Activity {
    @BindView(R.id.list) ListView listview;
    @BindView(R.id.btn_top) ImageButton btn_top;
    @BindView(R.id.btn_play) ImageButton btn_play;
    @BindView(R.id.btn_next) ImageButton btn_next;
    @BindView(R.id.seekBar) SeekBar SBar;
    @BindView(R.id.time) TextView time;
    @BindView(R.id.xuanze_img) ImageButton mImageButton;

    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    private MusicAdapter mAdapter;
    private List<MusicResource> oList;
    private Context mContext;
    private MusicResource mMusicResource;
    private int index = 0;
    private int state;
    private int flag = 0;// 歌曲播放模式标记
    String TAG = "MusicDIY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mContext = MainActivity.this;
        init();

    }

    protected void init(){
        //android 6.0 APK23 之后获取权限都要这样子
        checkPermission();
        //获取音乐列表
        getMusicList();
        //注册广播
        registMainActivityBroadcastReceiver();
        //注册点击事件
        listview.setOnItemClickListener(mOnItemClickListener);
        btn_top.setOnClickListener(mOnClickListener);
        btn_next.setOnClickListener(mOnClickListener);
        btn_play.setOnClickListener(mOnClickListener);
        mImageButton.setOnClickListener(mOnClickListener);
        seekBarChange();
    }

    protected void getMusicList(){

        Log.d(TAG,"start to MusicUtil");
        oList = MusicUtil.getMusicData(mContext);
        mAdapter = new MusicAdapter(oList, mContext);
        listview.setAdapter(mAdapter);
        listview.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);


    }
    @TargetApi(23)
    protected void checkPermission(){
        int hasReadContentPermission = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        if(hasReadContentPermission != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},REQUEST_CODE_ASK_PERMISSIONS);
            return ;
        }
    }
    //主方法注册广播
    protected void registMainActivityBroadcastReceiver(){
        MainActivityBroadcastReceiver mainActivityBroadcastReceiver = new MainActivityBroadcastReceiver();
        IntentFilter mfilter = new IntentFilter("com.MainActivityService");
        registerReceiver(mainActivityBroadcastReceiver,mfilter);
        //启动服务
        Intent intent = new Intent(mContext, MusicService.class);
        startService(intent);
    }
    //主方法接收广播
    public class MainActivityBroadcastReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
                state = intent.getIntExtra("state",-1);
                switch (state){
                    case 0x12:
                        btn_play.setImageResource(R.drawable.pause_button_default);
                        break;
                    case 0x11:
                    case 0x13:
                        btn_play.setImageResource(R.drawable.play_button_default);
                        break;
                    default:break;
                }

                int totalTime = intent.getIntExtra("totalTime",-1);
                int currentTime = intent.getIntExtra("currentTime", -1);
                if (currentTime != -1){
                    SBar.setProgress((int)((currentTime*1.0)/totalTime*100));
                    time.setText(initTime(currentTime,totalTime));
                }

                boolean isOver = intent.getBooleanExtra("over", false);
                if (isOver == true){
                    Intent intent2 = new Intent("com.MusicService");
                    if (flag == 0){
                        //列表循环
                        if(index == (oList.size() -1))index = 0;//如果为最后一首歌，则点击下一曲时播放第一曲，实现循环
                        else index++;

                    } else if(flag == 2){
                        //随机播放
                        int tmp = index;
                        while(index == tmp){
                            index = (int)(Math.random()*oList.size());
                        }
                    }
                    mAdapter.setSelectedItem(index);
                    mAdapter.notifyDataSetInvalidated();
                    mMusicResource = oList.get(index);
                    intent2.putExtra("nextMusic",1);
                    intent2.putExtra("musicResource",mMusicResource);
                    sendBroadcast(intent2);
                }
        }
    }

    //列表点击事件
    private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            index = position;//获取当前下标
            mAdapter.setSelectedItem(position);
            mAdapter.notifyDataSetInvalidated();
            mMusicResource = oList.get(position);//获取选中位置
            Intent intent = new Intent("com.MusicService");
            intent.putExtra("musicResource",mMusicResource);
            intent.putExtra("nextMusic",1);//播放新歌曲
            sendBroadcast(intent);//发送广播给MusicService
        }
    };
    //button点击事件
    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent("com.MusicService");
            switch (v.getId()){
                //上一曲
                case R.id.btn_top:
                    if(flag == 2){
                        //随机播放
                        int tmp = index;
                        while(index == tmp){
                            index = (int)(Math.random()*oList.size());
                        }
                    }else if(index == 0)index = oList.size() -1;//如果为第一首歌，则点击上一曲时播放最后一曲，实现循环
                    else index--;
                    mAdapter.setSelectedItem(index);
                    mAdapter.notifyDataSetInvalidated();
                    mMusicResource = oList.get(index);
                    intent.putExtra("nextMusic",1);
                    intent.putExtra("musicResource",mMusicResource);
                    break;
                //暂停、播放按钮
                case R.id.btn_play:
                    if (mMusicResource == null){
                        mMusicResource = oList.get(index);//播放第一首歌曲
                        intent.putExtra("musicResource",mMusicResource);
                        intent.putExtra("nextMusic",1);//播放新歌曲
                    }
                    intent.putExtra("isplay",1);
                    break;
                //下一曲按钮
                case R.id.btn_next:
                    if(flag == 2){
                        //如果模式为随机播放，下一曲也是随机的
                        int tmp = index;
                        while(index == tmp){
                            index = (int)(Math.random()*oList.size());
                        }
                    }else if(index == (oList.size() -1))index = 0;//如果为最后一首歌，则点击下一曲时播放第一曲，实现循环
                    else index++;
                    mAdapter.setSelectedItem(index);
                    mAdapter.notifyDataSetInvalidated();
                    mMusicResource = oList.get(index);
                    intent.putExtra("nextMusic",1);
                    intent.putExtra("musicResource",mMusicResource);
                    break;
                //播放模式 0:列表循环 1：单曲循环 2：随机播放
                case R.id.xuanze_img:
                    flag++;
                    if (flag>2)flag=0;
                    if (flag == 0) {
                        mImageButton.setBackgroundResource(R.drawable.liebiao1);
                        Toast.makeText(mContext,"列表循环",Toast.LENGTH_SHORT).show();
                    }
                    else if (flag == 1){
                        mImageButton.setBackgroundResource(R.drawable.danqu1);
                        Toast.makeText(mContext,"单曲循环",Toast.LENGTH_SHORT).show();
                    }
                    else {
                        mImageButton.setBackgroundResource(R.drawable.suiji1);
                        Toast.makeText(mContext,"随机播放",Toast.LENGTH_SHORT).show();
                    }
                    break;
                default:break;
            }
            sendBroadcast(intent);
        }
    };
    //进度条改变
    private void seekBarChange(){
        SBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                    Intent intent = new Intent("com.MusicService");
                    //获取当前拖动条位置
                    intent.putExtra("progress",seekBar.getProgress());
                    sendBroadcast(intent);
            }
        });
    }
    //把毫秒转换成分钟和秒钟
    private String initTime(int current, int total){
        int cur_minus = current / 1000 / 60;
        int cur_secnd = current /1000 % 60;
        int total_minus = total / 1000 /60;
        int total_secnd = total / 1000 % 60;
        // 00:00/00:00
        return  getTime(cur_minus)+":"+getTime(cur_secnd)+"/"+getTime(total_minus)+":"+getTime(total_secnd);
    }
    private String getTime(int time){
        if(time < 10){
            return "0"+ time;
        }
        return time + "";
    }
    //按下返回键时的处理方法
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if(keyCode == KeyEvent.KEYCODE_BACK){

            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle("提示");
            builder.setMessage("您确定要退出吗？");
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(mContext , MusicService.class);
                    stopService(intent);
                    System.exit(0);
                }
            });
            builder.setNegativeButton("取消",new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            builder.show();

            return true;
        }
        return super.onKeyDown(keyCode,event);
    }

    //注销广播
    @Override
    public void unregisterReceiver(BroadcastReceiver receiver) {

        super.unregisterReceiver(receiver);
        unregisterReceiver(receiver);
    }
}
