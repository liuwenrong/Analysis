/* *
   * Copyright (C) 2017 BaoliYota Tech. Co., Ltd, LLC - All Rights Reserved.
   *
   * Confidential and Proprietary.
   * Unauthorized copying of this file, via any medium is strictly prohibited.
   * */
package com.coolyota.analysis.tools;

import com.coolyota.analysis.BuildConfig;
import com.coolyota.analysis.CYAnalysis.LogLevel;
import com.coolyota.analysis.CYAnalysis.SendPolicy;

/**
 * des:
 *
 * @author liuwenrong
 * @version 1.0, 2017/6/22
 */
public class CYConstants {
    public static final String CLIENTDATA_URL = "/clientdata";
    public static final String ERROR_URL = "/errorlog";
    public static final String EVENT_URL = "/eventlog";
    public static final String TAG_URL = "/tag";
    public static final String PAGE_URL = "/page";
    public static final String UPDATE_URL = "/appupdate";
    public static final String CONFIG_URL = "/pushpolicyquery";
    public static final String PARAMETER_URL = "/getAllparameters";
    public static final String LOG_TAG = "CYAnalysis";


    public static final String CY = "/cy_";
    /**
     * 文件类型_上传所有类型
     */
    public static final String TYPE_ALL = "allInfo";
    /**
     * 文件类型_页面统计
     */
    public static final String TYPE_PAGE = "pageInfo";
    /**
     * 文件类型_自定义事件统计
     */
    public static final String TYPE_EVENT = "eventInfo";
    /**
     * 文件上传 加上后缀 新建一个文件如:cy_pageInfoUpload 防止上传文件时写入文件
     */
    public static final String UPLOAD = "Upload.zip";

    public static String BASE_URL = BuildConfig.BASE_URL;
    public static final String BASE_URL_TEST = "http://test.dcss.baoliyota.com";
    public static final String BASE_URL_PRO = "http://pro.dcss.baoliyota.com";
    public static boolean DebugEnabled = BuildConfig.LOG_DEBUG;
    public static LogLevel DebugLevel;
    public static long kContinueSessionMillis;
    public static boolean mProvideGPSData;
    public static boolean mUpdateOnlyWifi;
    public static SendPolicy mReportPolicy;
    public static long defaultFileSize;
    public static String fileSep;
    public static String newLine;
    public static String SDK_VERSION;
    public static String SDK_SECURITY_LEVEL;
    public static String SDK_POS_NAME;
    public static String SDK_CSR_ALIAS;
    public static String SDK_HTTPS_DN;
    public static String LIB_VERSION;
    public static boolean uploadEnabled;

    static {
        DebugLevel = LogLevel.Debug;
        kContinueSessionMillis = 30000L;
        mProvideGPSData = false;
        mUpdateOnlyWifi = true;
        mReportPolicy = SendPolicy.POST_ONSTART;
//        defaultFileSize = 1024L; //1K
        defaultFileSize = 1048576L; //1M
        fileSep = "@_@";
        newLine = "\r\n";
        SDK_VERSION = "${sdk.version}";
        SDK_SECURITY_LEVEL = "0";
        SDK_POS_NAME = "${sdk.pos.name}";
        SDK_CSR_ALIAS = "${sdk.csr.alias}";
        SDK_HTTPS_DN = "${sdk.https.dn}";
        LIB_VERSION = BuildConfig.VERSION_NAME;
        uploadEnabled = true;
    }

    public CYConstants() {
    }
}
