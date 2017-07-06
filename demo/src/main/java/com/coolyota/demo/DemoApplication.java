/* *
   * Copyright (C) 2017 BaoliYota Tech. Co., Ltd, LLC - All Rights Reserved.
   *
   * Confidential and Proprietary.
   * Unauthorized copying of this file, via any medium is strictly prohibited.
   * */

package com.coolyota.demo;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;


/**
 * des:
 * @author liuwenrong@coolpad.com
 * @version 1.0, 2017/3/21
 */
public class DemoApplication extends Application {

    private static final String TAG = "DemoApplication";
    private static final boolean DEBUG = false;
    /**
     * BaoliYota begin, add
     * what(reason) 提供单例和Context
     * liuwenrong, 1.0, 2017/4/11
     */
    private DemoApplication sInstance;
    private static Context mContext;
    public DemoApplication getInstance(){
        if (sInstance == null){
            sInstance = new DemoApplication();
        }
        return sInstance;
    }

    public static Context getContext(){
        return mContext;
    }

    @Override
    public void onCreate() {

        super.onCreate();
        mContext = this;
    }

    boolean getIsEmpty(){
        String s = "..";
        return !s.isEmpty();

    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
