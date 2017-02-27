package com.example.myapplication;

import android.support.multidex.MultiDexApplication;

/**
 * 作者:伍腾飞
 * on 16/8/3110:45.
 * 邮箱:15333864350@163.com
 */
public class App extends MultiDexApplication {
    private static final String TAG = "App";

    @Override
    public void onCreate() {
        super.onCreate();

     new InjectUtil(this).fix();
    }


}




