package com.example.administrator.music_diy.util;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.MediaStore;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.content.ContextWrapper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/10/2.
 */

public class MusicUtil{

    public static List<MusicResource> getMusicData(Context context){
        List<MusicResource> oList  = new ArrayList<MusicResource>();
        String TAG = "MusicDIY";
        Cursor cursor = null;
        Log.d(TAG,"cursor ing");


        try{
            cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,null,null,
                    MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
            Log.d(TAG,"finish cursor");
            while (cursor.moveToNext()) {
                MusicResource music = new MusicResource();
                //得到歌曲名
                String name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                Log.d(TAG,"name"+name);
                //得到演唱者
                String author = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                Log.d(TAG,"author"+author);
                //得到路径
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                Log.d(TAG,"path"+path);
                long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                //搜索歌曲的过程中会得到一些作者为空的情况，直接和unknown比较会报错，先从时间方面过滤掉这些无用的音频文件
                //歌曲时长大于20S
                if (duration > 60000) {
                    if (author.equals("<unknown>")) {
                        author = "未知艺术家";
                    }
                    music.setName(name);
                    music.setAuthor(author);
                    music.setPath(path);
                    music.setDuration(duration);
                    oList.add(music);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (cursor != null){
                cursor.close();
            }
        }

        return oList;
    }
}
