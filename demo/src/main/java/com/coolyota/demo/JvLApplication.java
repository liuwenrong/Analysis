/* *
   * Copyright (C) 2017 BaoliYota Tech. Co., Ltd, LLC - All Rights Reserved.
   *
   * Confidential and Proprietary.
   * Unauthorized copying of this file, via any medium is strictly prohibited.
   * */

package com.coolyota.demo;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.util.Log;

import com.coolyota.analysis.CYAnalysis;
import com.coolyota.analysis.tools.ApiConstants;
import com.cy.demo.BuildConfig;

import java.util.ArrayList;
import java.util.List;


/**
 * des:
 * @author liuwenrong@coolpad.com
 * @version 1.0, 2017/3/21
 */
public class JvLApplication extends Application {

    private static final String TAG = "JvLApplication";
    private static final boolean DEBUG = false;
    /**
     * BaoliYota begin, add
     * what(reason) 提供单例和Context
     * liuwenrong, 1.0, 2017/4/11
     */
    private JvLApplication sInstance;
    private static Context mContext;
    public JvLApplication getInstance(){
//        if (sInstance == null){
//            sInstance = new JvLApplication();
//      }
        return sInstance;
    }

    public static Context getContext(){
        return mContext;
    }

    // 创建服务用于捕获崩溃异常
    private Thread.UncaughtExceptionHandler restartHandler = new Thread.UncaughtExceptionHandler() {
        public void uncaughtException(Thread thread, Throwable ex) {
            ex.printStackTrace();
            Log.d("JvApp", "restartApp: 捕获到异常=");
            restartApp();//发生崩溃异常时,重启应用
        }
    };

    public void restartApp() {
        Intent intent = new Intent(sInstance, HomeAct.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        sInstance.startActivity(intent);
        android.os.Process.killProcess(android.os.Process.myPid());  //结束进程之前可以把你程序的注销或者退出代码放在这段代码之前
    }

    @Override
    public void onCreate() {

        super.onCreate();
        mContext = this;
        sInstance = this;

        CYAnalysis.setUploadEnabled(true); //设置是否上传到服务器,测试时防止频繁将数据传到数据库
        CYAnalysis.setDebugEnabled(BuildConfig.LOG_DEBUG); //是否打印jar包的log,方便测试,还有配置测试或正式服务器,call before init!
        CYAnalysis.init(this, ApiConstants.KEY_BReader, BuildConfig.APPLICATION_ID);

//        Thread.setDefaultUncaughtExceptionHandler(restartHandler); // 程序崩溃时触发线程  以下用来捕获程序崩溃异常
        //设置该CrashHandler为程序的默认处理器
        CyUncaughtExceptionHandler catchException = new CyUncaughtExceptionHandler(this);
        Thread.setDefaultUncaughtExceptionHandler(catchException);
    }
    List<Activity> list = new ArrayList<Activity>();
    /**
     * Activity关闭时，删除Activity列表中的Activity对象*/
    public void removeActivity(Activity a){
        list.remove(a);
    }

    /**
     * 向Activity列表中添加Activity对象*/
    public void addActivity(Activity a){
        list.add(a);
    }

    /**
     * 关闭Activity列表中的所有Activity*/
    public void finishActivity(){
        for (Activity activity : list) {
            if (null != activity) {
                activity.finish();
            }
        }
        //杀死该应用进程
        android.os.Process.killProcess(android.os.Process.myPid());
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

