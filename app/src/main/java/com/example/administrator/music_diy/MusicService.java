package com.example.administrator.music_diy;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import com.example.administrator.music_diy.util.MusicResource;

/**
 * Created by Administrator on 2016/10/4.
 */

public class MusicService extends Service {
    private int nextMusic;
    private int isPlay;
    private int progress;
    private int currentTime, totalTime;
    private int state = 0x11;//0x11:第一次播放歌曲，0x12:暂停，0x13：继续播放
    private MusicResource mMusicResource;
    private MediaPlayer mMediaPlayer = new MediaPlayer();


    @Override
    public void onCreate() {
        //注册广播
        MyBroadcastReceiver receiver = new MyBroadcastReceiver();
        IntentFilter filter = new IntentFilter("com.MusicService");
        registerReceiver(receiver ,filter);
        //监听播放完成事件
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Intent intent = new Intent("com.MainActivityService");
                intent.putExtra("over",true);
                sendBroadcast(intent);
                currentTime = 0;
                totalTime = 0;
            }
        });
        super.onCreate();
    }

    public class MyBroadcastReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
                //判断接受到的命令是否是播放新歌曲
                nextMusic = intent.getIntExtra("nextMusic",-1);
                if (nextMusic == 1 ){
                    mMusicResource = (MusicResource) intent.getSerializableExtra("musicResource");//获得歌曲对象
                    if (mMusicResource != null){
                            playMusic(mMusicResource);
                            state = 0x12;
                    }
                }
                //判断
                isPlay = intent.getIntExtra("isplay",-1);
            if (isPlay == 1){
                switch (state){
                    //第一次播放
                    case 0x11:
                        playMusic(mMusicResource);
                        state = 0x12;
                        break;
                    //暂停
                    case 0x12:
                        mMediaPlayer.pause();
                        state = 0x13;
                        break;
                    //继续播放
                    case 0x13:
                        mMediaPlayer.start();
                        state = 0x12;
                        break;
                    default:
                        state = 0x11;
                        break;
                }
            }

            progress = intent.getIntExtra("progress",-1);
            if(progress != -1){
                //获取当前时间进度
                currentTime = (int) (((progress*1.0)/100)*totalTime);
                mMediaPlayer.seekTo(currentTime);
            }


            //把当前状态发送给activity
            Intent intent1 = new Intent("com.MainActivityService");
            intent1.putExtra("state",state);
            sendBroadcast(intent1);
        }
    }
    //播放歌曲
    public void playMusic(MusicResource musicResource){
        //判断当前是否有音乐正在播放
        if (mMediaPlayer != null ){
            //停止播放
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            try {
                //获得播放路径
                mMediaPlayer.setDataSource(musicResource.getPath());
                //准备
                mMediaPlayer.prepare();
                //播放
                mMediaPlayer.start();
                totalTime = mMediaPlayer.getDuration();//获取当前歌曲时长
                new Thread(){
                    public void run(){
                        while(currentTime < totalTime){
                            try{
                                sleep(1000);
                                //获取当前时间

                                currentTime = mMediaPlayer.getCurrentPosition();
                                Intent intent2 = new Intent("com.MainActivityService");
                                intent2.putExtra("currentTime",currentTime);
                                intent2.putExtra("totalTime",totalTime);
                                intent2.putExtra("state", state);//---
                                sendBroadcast(intent2);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    };
                }.start();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void unregisterReceiver(BroadcastReceiver receiver) {
        super.unregisterReceiver(receiver);
        unregisterReceiver(receiver);
    }
}
