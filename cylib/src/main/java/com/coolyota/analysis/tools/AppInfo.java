/* *
   * Copyright (C) 2017 BaoliYota Tech. Co., Ltd, LLC - All Rights Reserved.
   *
   * Confidential and Proprietary.
   * Unauthorized copying of this file, via any medium is strictly prohibited.
   * */

package com.coolyota.analysis.tools;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * des: 获取App的信息的工具类
 *
 * @author  liuwenrong
 * @version 1.0,2017/6/22
 */
class AppInfo {
	private static String versionName = "";
    
    static String getAppKey(Context context) {
        	SharedPrefUtil sp = new SharedPrefUtil(context);
        	return sp.getValue("app_key", "");
    }

    static String getAppVersion(Context context) {
    	if(versionName.equals("")){
    		try {
                PackageManager pm = context.getPackageManager();
                PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
                if (pi != null) {
                    versionName = pi.versionName;
                }
            } catch (Exception e) {
            	AppInfo.class.getCanonicalName();
                CYLog.e(CYConstants.LOG_TAG, AppInfo.class,e.toString());
            }
    	}
        return versionName;
    }
}
