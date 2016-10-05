package com.example.administrator.music_diy.util;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/10/2.
 */

public class MusicResource implements Serializable{

    private static final long SerialVersionUID = 1L;
    private String name;
    private String author;
    private String path;
    private long duration;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
    public static long getSerialVersionUID() {
        return SerialVersionUID;
    }

}
