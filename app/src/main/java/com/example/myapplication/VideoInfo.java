package com.example.myapplication;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/3/22.
 */
public class VideoInfo implements Serializable{
    private int id;
    private String title;       //标题
    private String displayName; //文件名
    private String url;         //地址
    private int duration;    //时长
    private long size;      //大小
    private String type;    //类型
//    private Bitmap thumbBmp;    //缩略图

    public VideoInfo(){

    }

    public VideoInfo(int id, String title, String displayName, String url, int duration,
                     long size, String type){
        this.id = id;
        this.title = title;
        this.displayName = displayName;
        this.url = url;
        this.duration = duration;
        this.size = size;
        this.type = type;
//        this.thumbBmp = bmp;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

//    public Bitmap getThumbBmp() {
//        return thumbBmp;
//    }
//
//    public void setThumbBmp(Bitmap thumbBmp) {
//        this.thumbBmp = thumbBmp;
//    }


}
