/* *
   * Copyright (C) 2017 BaoliYota Tech. Co., Ltd, LLC - All Rights Reserved.
   *
   * Confidential and Proprietary.
   * Unauthorized copying of this file, via any medium is strictly prohibited.
   * */
package com.coolyota.analysis.tools;

import android.text.TextUtils;
import android.util.Log;

import com.coolyota.analysis.BuildConfig;
import com.coolyota.analysis.CYAnalysis;
import com.coolyota.analysis.CYAnalysis.LogLevel;

/**
 * des:
 *
 * @author liuwenrong
 * @version 1.0, 2017/6/22
 */
public class CYLog {
    CYLog() {
    }

    public static void v(String tag, Class<?> classobj, String msg) {
        if (CYConstants.DebugEnabled) {
            if (CYConstants.DebugLevel != CYAnalysis.LogLevel.Debug && CYConstants.DebugLevel != LogLevel.Info && CYConstants.DebugLevel != LogLevel.Warn && CYConstants.DebugLevel != LogLevel.Error) {
                Log.v(tag, classobj.getCanonicalName() + ": " + msg);
            }
        }
    }

    public static void d(String tag, Class<?> classobj, String msg) {
        if (CYConstants.DebugEnabled) {
            if (CYConstants.DebugLevel != LogLevel.Info && CYConstants.DebugLevel != LogLevel.Warn && CYConstants.DebugLevel != LogLevel.Error) {
                Log.d(tag, classobj.getCanonicalName() + ": " + msg);
            }
        }
    }

    public static void i(String tag, Class<?> classobj, String msg) {
        if (CYConstants.DebugEnabled) {
            if (CYConstants.DebugLevel != LogLevel.Warn && CYConstants.DebugLevel != LogLevel.Error) {
                Log.i(tag, classobj.getCanonicalName() + ": " + msg);
            }
        }
    }

    public static void w(String tag, Class<?> classobj, String msg) {
        if (CYConstants.DebugEnabled) {
            if (CYConstants.DebugLevel != LogLevel.Error) {
                Log.w(tag, classobj.getCanonicalName() + ": " + msg);
            }
        }
    }

    public static void e(String tag, Class<?> classobj, String msg) {
        if (CYConstants.DebugEnabled) {
            Log.e(tag, classobj.getCanonicalName() + ": " + msg);
        }
    }

    public static void e(String tag, Exception e) {
        if (CYConstants.DebugEnabled) {
            Log.e(tag, e.toString());
            e.printStackTrace();
        }
    }

    public static final boolean DEBUG = BuildConfig.LOG_DEBUG;
    private String module;
    private String tag;

    public CYLog(String module, String tag) {
        this.module = !TextUtils.isEmpty(module) ? module : "CoolYota";
        this.tag = !TextUtils.isEmpty(tag) ? tag : "CoolYota";
    }

    public CYLog(String tag) {
        this("CoolYota", tag);
    }

    public void e(String msg, Throwable tr) {
        Log.e(tag, msg, tr);
    }

    public void w(String msg) {
        Log.w(tag, msg);
    }

    public void info(String msg) {
        Log.i(tag, msg);
    }

    public void debug(String msg) {
        if (DEBUG || Log.isLoggable(module, Log.DEBUG)) Log.d(tag, msg);
    }

    public void verbose(String msg) {
        if (DEBUG || Log.isLoggable(module, Log.VERBOSE)) Log.v(tag, msg);
    }

}
