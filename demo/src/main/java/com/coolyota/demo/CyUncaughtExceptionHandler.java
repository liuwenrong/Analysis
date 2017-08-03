package com.coolyota.demo;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

/**
 * des:
 *
 * @author liuwenrong
 * @version 1.0, 2017/7/18
 */
public class CyUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
    public static final String TAG = "CyUncaughtException";
    private JvLApplication myApplication;
    private Thread.UncaughtExceptionHandler mUncaughtExceptionHandler;

    public CyUncaughtExceptionHandler(JvLApplication myApplication) {
        this.myApplication = myApplication;
        mUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();// 获取系统默认的异常处理器
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        Log.d("JvApp", "28--uncaughtException: 捕获到异常=");
        ex.printStackTrace();
        if (!handleException(ex) && mUncaughtExceptionHandler != null) {
            //如果用户没有处理则让系统默认的异常处理器来处理
            mUncaughtExceptionHandler.uncaughtException(thread, ex);
        } else {
//            try {
//                Thread.sleep(2000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
            Toast.makeText(myApplication.getApplicationContext(), "很抱歉,程序出现异常,一秒钟后重启.", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(myApplication.getApplicationContext(), HomeAct.class);
            //重启应用，得使用PendingIntent
            PendingIntent restartIntent = PendingIntent.getActivity(
                    myApplication.getApplicationContext(), 0, intent,
                    PendingIntent.FLAG_CANCEL_CURRENT);
            //退出程序
            AlarmManager mAlarmManager = (AlarmManager) myApplication.getSystemService(Context.ALARM_SERVICE);
            mAlarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 1000,
                    restartIntent); // 1秒钟后重启应用
            myApplication.finishActivity();
        }
    }

    Handler mHandler = new Handler(Looper.getMainLooper());
    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     *
     * @param ex
     * @return true:如果处理了该异常信息;否则返回false.
     */
    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }
        Log.d(TAG, "65--run: 很抱歉,程序出现异常,一秒钟后重启.");
        try {
            final String command = "am force-stop com.cy.demo";
            Process process = Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //使用Toast来显示异常信息
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "70--run: 很抱歉,程序出现异常,一秒钟后重启.");
//                Toast.makeText(myApplication.getApplicationContext(), "很抱歉,程序出现异常,一秒钟后重启.", Toast.LENGTH_SHORT).show();
            }
        });

        return true;
    }
}
