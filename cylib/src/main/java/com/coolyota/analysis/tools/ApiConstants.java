/* *
   * Copyright (C) 2017 BaoliYota Tech. Co., Ltd, LLC - All Rights Reserved.
   *
   * Confidential and Proprietary.
   * Unauthorized copying of this file, via any medium is strictly prohibited.
   * */

package com.coolyota.analysis.tools;


/**
 * @author liuwenrong@coolpad.com
 * @version 1.0, 2017/6/16
 */
public class ApiConstants {


//    public static final String BASE_URL = "http://120.77.80.97";
//    public static final String BASE_URL = "http://test.dcss.baoliyota.com";
//    public static final String BASE_URL = "http://pro.dcss.baoliyota.com";
//    private static final String BASE_URL = Constants.BASE_URL;

    public class Code{
        /**
         * 上传成功 返回code 0
         */
        public static final int Success = 0;
    }
    /**
     * 上传路径
     */
    public static final String PATH_UPLOAD = CYConstants.BASE_URL + "/dcss-collector/log/upload";

    /**
     * 登录token
     */
    public static final String PARAM_TOKEN = "token";

    /**
     * 产品型号在服务器端对应的key
     */
    public static final String PARAM_KEY = "key";

    /**
     * 产品型号在服务器端对应的key = 7576E9DD910227F0D1B297FC05D90BE7(注：固定的，其实是’ YOTA3’的MD5编码的大写)
     * 墨知应用使用3001,B屏系统使用4001
     */
    public static String VALUE_KEY = "";
    /**
     * 墨知应用使用3001
     */
    public static String KEY_BReader = "3001";
    /**
     * B屏系统使用4001
     */
    public static String KEY_BLauncher = "4001";

//    public static final String VALUE_KEY = "7576E9DD910227F0D1B297FC05D90BE7";

    /**
     * 手机的唯一表示IMEI
     */
    public static final String PARAM_IMEI = "imei";

    /**
     * 时间戳参数
     */
    public static final String PARAM_TIME_STAMP = "timestamp";

    /**
     * logType 日志类型
     */
    public static final String PARAM_LOG_TYPE = "logType";

    /**
     * 日志分类:App分析统计打点 20015
     */
    public static final int LOG_TYPE_ANALYSIS =  20015;

    /**
     * account 账号 (非必须)
     */
    public static final String PARAM_ACCOUNT = "account";

    /**
     * phone 手机号 (非必须)
     */
    public static final String PARAM_PHONE = "phone";
    /**
     * phone 手机号 (非必须)
     */
    public static final String PARAM_FILE = "file";

    /**
     * proType产品型号 (Y3)
     */
    public static final String PARAM_PRO_TYPE = "proType";

    /**
     * sysVersion 系统版本
     */
    public static final String PARAM_SYS_VERSION = "sysVersion";

    /**
     * upDesc上报描述 (非必须)
     */
    public static final String PARAM_UP_DESC = "upDesc";

    /**
     * upType上报分类 (必须)
     */
    public static final String PARAM_UP_TYPE = "upType";

    public static class UpType{

        public static final int TYPE_SUGGESTIONS = 20001;
        public static final int TYPE_TALK_SMS = 20002;
        public static final int TYPE_CAMERA_GALLERY = 20003;
        public static final int TYPE_WIFI_NET = 20004;
        public static final int TYPE_SYSTEM_PLAYER = 20005;
        public static final int TYPE_SYSTEM_UPGRADE = 20006;
        public static final int TYPE_OTHER_APP = 20007;
        public static int[] upTypes = {TYPE_SUGGESTIONS, TYPE_TALK_SMS, TYPE_CAMERA_GALLERY, TYPE_WIFI_NET, TYPE_SYSTEM_PLAYER, TYPE_SYSTEM_UPGRADE, TYPE_OTHER_APP};
    }

    /**
     * post请求的头信息
     */
    public static final String HEADER_CONTENT_TYPE = "Content-Type";

    /**
     * post请求的头信息-要求body为json格式
     */
    public static final String HEADER_CONTENT_TYPE_JSON = "application/json";

}

