package com.example.myapplication;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.provider.MediaStore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by Administrator on 2016/3/22.
 */
public class Utils {

    /**
     * 获取SD卡中的视频文件
     *
     * @param context
     * @return
     */
    public static List<VideoInfo> getVideoFiles(Context context) {
        List<VideoInfo> videoes = new ArrayList<VideoInfo>();

        //查询媒体数据库
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                null, null, null, MediaStore.Video.Media.DEFAULT_SORT_ORDER);
        //遍历媒体数据库
        if (cursor.moveToFirst()) {

            while (!cursor.isAfterLast()) {
                //视频编号
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
                //视频标题
                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE));
                //文件名称
                String displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME));
                //视频文件的路径 ：MediaStore.Video.Media.DATA
                String url = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                //视频的总播放时长 ：MediaStore.Video.Media.DURATION
                int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
                //视频文件的大小 ：MediaStore.Video.Media.SIZE
                long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));
                String type = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.MIME_TYPE));

//                Bitmap bmp = getVideoThumbnail(url);

                if (size > 1024 * 1024) {//大于1M
                    if (title.equals("<unknown>") || title.equals("")) {
                        title = "未知";
                    }

                    VideoInfo videoInfo = new VideoInfo(id, title, displayName,
                            url, duration, size, type);
                    videoes.add(videoInfo);
                }

                cursor.moveToNext();
            }
        }
        return videoes;
    }

    /**
     * 获取视频文件的缩略图
     *
     * @param filePath
     * @return
     */
    public static Bitmap getVideoThumbnail(String filePath) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);
            bitmap = retriever.getFrameAtTime(20000);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }


    /**
     * 将int值转换为分钟和秒的格式
     *
     * @param value
     * @return
     */
    public static String formatToString(int value) {
        SimpleDateFormat ft = new SimpleDateFormat("HH:mm:ss");
        //需要减去时区差，否则计算结果不正确(中国时区会相差8个小时)
        value -= TimeZone.getDefault().getRawOffset();
        String result = ft.format(value);
        return result;
    }

    /**
     * 获取音频焦点
     * @param context
     * @param listener 音频焦点监听器
     * @return
     */
    public static boolean getAudioFocus(Context context, AudioManager.OnAudioFocusChangeListener listener) {
        AudioManager manager = (AudioManager) context.getApplicationContext().
                getSystemService(Context.AUDIO_SERVICE);
        int result = manager.requestAudioFocus(listener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            return true;
        } else {
            return false;
        }
    }


    public static void showToast() {

    }
}
