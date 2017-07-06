/* *
   * Copyright (C) 2017 BaoliYota Tech. Co., Ltd, LLC - All Rights Reserved.
   *
   * Confidential and Proprietary.
   * Unauthorized copying of this file, via any medium is strictly prohibited.
   * */
package com.coolyota.analysis;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;

import com.coolyota.analysis.tools.ApiConstants;
import com.coolyota.analysis.tools.CYConstants;
import com.coolyota.analysis.tools.CYLog;
import com.coolyota.analysis.tools.ClientDataManager;
import com.coolyota.analysis.tools.CommonUtil;
import com.coolyota.analysis.tools.SaveEventManager;
import com.coolyota.analysis.tools.SharedPrefUtil;
import com.coolyota.analysis.tools.UploadHistoryLog;
import com.coolyota.analysis.tools.SavePageManager;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

/**
 * des:
 *
 * @author liuwenrong
 * @version 1.0, 2017/6/22
 */
public final class CYAnalysis {

    public static final String TAG = "CYAnalysis";
    /**
     * 用于判断一些方法调用前是否调用了init方法,防止一些不必要的错误
     */
    private static boolean mIsInit = false;
    private static boolean isFirst = true;
    private static SavePageManager savePageManager;
    private static Timer timer = null;
    private static WeakReference<Context> contextWR;
    private static Handler handler;

    static {
        HandlerThread localHandlerThread = new HandlerThread("CYAnalysis");
        localHandlerThread.start();
        handler = new Handler(localHandlerThread.getLooper());
    }

    private static void init(Context context) {
        updateContent(context);
        postClientData();
        postHistoryLog();
//        onError();
        CYLog.i("CYAnalysis", CYAnalysis.class, "Call init();BaseURL = " + CYConstants.BASE_URL);
        SharedPrefUtil spu = new SharedPrefUtil((Context)contextWR.get());
        spu.setValue("system_start_time", System.currentTimeMillis());
    }

    /**
     * 初始化,推荐在Home页或者Application的onCreate中调用
     * @param context
     * @param key 如墨知应用使用3001,B屏系统使用4001
     * @param appkey Package name is recommended
     */
    public static void init(Context context, String key, String appkey) {
        if(appkey.length() != 0) {
            updateContent(context);
            mIsInit = true;
            ApiConstants.VALUE_KEY = key;
            SharedPrefUtil sp = new SharedPrefUtil((Context)contextWR.get());
            sp.setValue("key", key);
            sp.setValue("app_key", appkey);
            init(context);
        } else {
            CYLog.e(TAG, CYAnalysis.class, "appkey and baseUrl are required");
        }
    }

    private static void updateContent(Context context) {
        contextWR = new WeakReference(context);
        context = null;
    }


    /**
     * 针对使用Activity或者Fragment的应用，在对应的生命周期函数里调用
     *
     * @param context
     * @param PageName
     */
    public static void onResume(Context context, final String PageName) {
        if(!mIsInit) {
            CYLog.e(TAG, CYAnalysis.class, "init must be called before onResume! ");
        } else {
            updateContent(context);
            setSystemStartTime((Context)contextWR.get());
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    CYLog.i(TAG, CYAnalysis.class, "Call onFragmentResume()");
                    if(CYAnalysis.savePageManager == null) {
                        CYAnalysis.savePageManager = new SavePageManager((Context)CYAnalysis.contextWR.get());
                    }

                    CYAnalysis.savePageManager.onFragmentResume((Context)CYAnalysis.contextWR.get(), PageName);
                }
            });
            handler.post(thread);
        }
    }

    /**
     * 在Activity或者Fragment的生命周期函数中调用
     * @param context
     */
    public static void onPause(Context context) {
        if (!mIsInit) {
            CYLog.e(CYConstants.LOG_TAG, CYAnalysis.class, "init must be called before onPause! ");
            return;
        }
        updateContent(context);
        if (timer != null) {
            timer.cancel();
        }

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                CYLog.i(CYConstants.LOG_TAG, CYAnalysis.class, "Call onPause()");
                if (savePageManager == null)
                    savePageManager = new SavePageManager(contextWR.get());
                savePageManager.onPause(contextWR.get());
            }
        });
        handler.post(thread);
    }

    private static void setSystemStartTime(final Context context) {
        if(CommonUtil.getReportPolicyMode(context) == CYAnalysis.SendPolicy.POST_INTERVAL) {
            long app_start_time = (new SharedPrefUtil(context)).getValue("system_start_time", System.currentTimeMillis());
            long running_time = System.currentTimeMillis() - app_start_time;
            timer = new Timer();
            SharedPrefUtil sp = new SharedPrefUtil(context);
            int start_time = Integer.parseInt(String.valueOf(sp.getValue("interval_time", 60000L)));
            //定时发送
            timer.schedule(new TimerTask() {
                public void run() {
                    UploadHistoryLog thread = new UploadHistoryLog(context);
                    CYAnalysis.handler.post(thread);
                }
            }, (long)start_time - running_time % (long)start_time, (long)start_time);
        }

    }

    /**
     * Setting data transmission mode 设置数据发送模式 暂时不开放
     *
     * @param context
     * @param sendPolicy {@link SendPolicy}
     */
    private static void setDefaultReportPolicy(Context context,
                                              SendPolicy sendPolicy) {
        if (!mIsInit) {
            CYLog.e(CYConstants.LOG_TAG, CYAnalysis.class, "init must be called before setDefaultReportPolicy! ");
            return;
        }
        updateContent(context);
        CYConstants.mReportPolicy = sendPolicy;
        int type = 1;

        if (sendPolicy == SendPolicy.POST_ONSTART) {
            type = 0;
        }
        if (sendPolicy == SendPolicy.POST_INTERVAL) {
            type = 2;
        }
        SharedPrefUtil spu = new SharedPrefUtil(contextWR.get());
        spu.setValue("DefaultReportPolicy", type);

        CYLog.i(CYConstants.LOG_TAG, CYAnalysis.class,
                "setDefaultReportPolicy = " + String.valueOf(sendPolicy));

    }

    public static void setUploadEnabled(boolean uploadEnabled){
        CYConstants.uploadEnabled = uploadEnabled;
    }

    public static enum LogLevel {
        Info,
        Debug,
        Warn,
        Error,
        Verbose;

        private LogLevel() {
        }
    }

    public static enum SendPolicy {
        POST_ONSTART,
        POST_NOW,
        POST_INTERVAL; //间隔

        private SendPolicy() {
        }
    }

    /**
     * Call this function to send the uncatched crash exception stack
     * information to server
     */
    static void onError() {
        if (!mIsInit) {
            CYLog.e(CYConstants.LOG_TAG, CYAnalysis.class, "init must be called before onError! ");
            return;
        }

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                CYLog.i(CYConstants.LOG_TAG, CYAnalysis.class, "Call onError()");
//                MyCrashHandler crashHandler = MyCrashHandler.getInstance();
//                crashHandler.init(contextWR.get().getApplicationContext());
//                Thread.setDefaultUncaughtExceptionHandler(crashHandler);
            }
        });
        handler.post(thread);
    }

    /**
     * upload startup and device information 生成DeviceInfo, 暂时不用上传数据,
     */
    static void postClientData() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (isFirst) {
                    CYLog.i(CYConstants.LOG_TAG, CYAnalysis.class, "Start postClientdata thread");
                    ClientDataManager cm = new ClientDataManager(contextWR.get());
                    cm.postClientData();
                    isFirst = false;
                }
            }
        });
        handler.post(thread);
    }

    /**
     * 发送历史的Log到服务器
     */
    static void postHistoryLog() {
        CYLog.i(CYConstants.LOG_TAG, CYAnalysis.class, "postHistoryLog");
        if (CommonUtil.isNetworkAvailable(contextWR.get())) {
            Thread thread = new UploadHistoryLog(contextWR.get());
            handler.post(thread);
        }
    }

    /**
     * 发送event事件数据
     *
     * @param context
     */
    public static void onEvent(Context context, final String event_id) {
        if (!mIsInit) {
            CYLog.e(CYConstants.LOG_TAG, CYAnalysis.class, "init must be called before onEvent! ");
            return;
        }
        updateContent(context);
        Thread thread = new Thread(new Runnable() {
            public void run() {
                CYLog.i(CYConstants.LOG_TAG, CYAnalysis.class, "Call onEvent(context,event_id)");
                onEvent(contextWR.get(), event_id, 1);
            }
        });
        handler.post(thread);
    }

    /**
     * 发送event事件数据
     *
     * @param context
     * @param event_id
     * @param acc 计数 account
     */
    public static void onEvent(Context context, final String event_id,
                               final int acc) {
        if (!mIsInit) {
            CYLog.e(CYConstants.LOG_TAG, CYAnalysis.class, "init must be called before onEvent! ");
            return;
        }
        updateContent(context);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                CYLog.i(CYConstants.LOG_TAG, CYAnalysis.class, "Call onEvent(event_id,acc)");
                onEvent(contextWR.get(), event_id, "", acc);
            }
        });
        handler.post(thread);
    }

    /**
     * 发送event事件数据
     *
     * @param event_id
     * @param label
     * @param acc
     */
    public static void onEvent(Context context, final String event_id,
                               final String label, final int acc) {
        if (!mIsInit) {
            CYLog.e(CYConstants.LOG_TAG, CYAnalysis.class, "init must be called before onEvent! ");
            return;
        }
        updateContent(context);
        if (event_id == null || event_id.length() == 0) {
            CYLog.e(CYConstants.LOG_TAG, CYAnalysis.class, "Valid event_id is required");
        }
        if (acc < 1) {
            CYLog.e(CYConstants.LOG_TAG, CYAnalysis.class, "Event acc should be greater than zero");
        }

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                CYLog.i(CYConstants.LOG_TAG, CYAnalysis.class, "Call onEvent(event_id,label,acc)");
                SaveEventManager em = new SaveEventManager(contextWR.get(), event_id, label,
                        acc + "");
                em.saveEventInfo();
            }
        });
        handler.post(thread);
    }

    /**
     * 发送event事件数据k,注册事件,计算事件
     *
     * @param context
     * @param event_id
     * @param label 可以传空
     * @param json JsonObj.toString
     */
    public static void onEvent(Context context, final String event_id,
                               final String label, final String json) {
        if (!mIsInit) {
            CYLog.e(CYConstants.LOG_TAG, CYAnalysis.class, "init must be called before onEvent! ");
            return;
        }
        updateContent(context);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                CYLog.i(CYConstants.LOG_TAG, CYAnalysis.class, "Call onEvent(context,event_id,label,acc)");
                SaveEventManager em = new SaveEventManager(contextWR.get(), event_id, label,
                        "1", json);
                em.saveEventInfo();
            }
        });
        handler.post(thread);
    }




}
