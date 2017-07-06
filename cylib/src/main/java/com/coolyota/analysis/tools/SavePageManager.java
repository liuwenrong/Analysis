/* *
   * Copyright (C) 2017 BaoliYota Tech. Co., Ltd, LLC - All Rights Reserved.
   *
   * Confidential and Proprietary.
   * Unauthorized copying of this file, via any medium is strictly prohibited.
   * */

package com.coolyota.analysis.tools;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

/**
 * des: 主要存储activity的页面数据Web页面
 *
 * @author  liuwenrong
 * @version 1.0,2017/6/22
 */
public class SavePageManager {
	private static WeakReference<Context> contextWR;
    private String session_id = "";

    public SavePageManager(Context context) {
    	 contextWR = new WeakReference<Context>(context);
        
    }

    /**
     * 每次界面结束时,生成一次JSONObj
     * @param start_millis
     * @param end_millis
     * @param duration
     * @param activities
     * @return
     * @throws JSONException
     */
    JSONObject prepareUsinglogJSON(String start_millis, String end_millis,
            String duration, String activities) throws JSONException {
        JSONObject jsonUsinglog = new JSONObject();
        if (session_id.equals("")) {
            session_id = CommonUtil.getSessionId(contextWR.get());
        }

        jsonUsinglog.put("session_id", session_id);
        jsonUsinglog.put("start_millis", start_millis);
        jsonUsinglog.put("end_millis", end_millis);
        jsonUsinglog.put("duration", duration);
        jsonUsinglog.put("version", AppInfo.getAppVersion(contextWR.get()));
        jsonUsinglog.put("activities", activities);
        jsonUsinglog.put("appkey", AppInfo.getAppKey(contextWR.get()));
        jsonUsinglog.put("useridentifier",
                CommonUtil.getUserIdentifier(contextWR.get()));
        jsonUsinglog.put("deviceid", DeviceInfo.getDeviceId());
        jsonUsinglog.put("lib_version", CYConstants.LIB_VERSION);

        return jsonUsinglog;
    }
    public void judgeSession(final Context context){
    	CYLog.i(CYConstants.LOG_TAG,SavePageManager.class, "Call onResume()");
        try {
            if (CommonUtil.isNewSession(context)) {
                session_id = CommonUtil.generateSession(context);
//                ClientdataManager cm = new ClientdataManager(context);
//                cm.postClientData();
                CYLog.i(CYConstants.LOG_TAG, SavePageManager.class,"New SessionId is " + session_id);
            }
        } catch (Exception e) {
            CYLog.e(CYConstants.LOG_TAG, e);
        }
    }
    /**
     * activity onResume
     * @param context
     */
    public void onResume(final Context context) {
    	judgeSession(context);
        CommonUtil.savePageName(context, CommonUtil.getActivityName(context));
    }
    /**
     * fragment onResume
     * @param context
     * @param pageName
     */
    public void onFragmentResume(final Context context,String pageName) {
    	judgeSession(context);
        CommonUtil.savePageName(context, pageName);
    }

    /**
     * call this method when activity or fragment onPause
     * @param context
     */
    public void onPause(final Context context) {
        CYLog.i(CYConstants.LOG_TAG,SavePageManager.class, "Call onPause()");

        SharedPrefUtil sp = new SharedPrefUtil(context);
        String pageName = sp.getValue("CurrentPage", CommonUtil.getActivityName(context));

        long start = sp.getValue("session_save_time",
                System.currentTimeMillis());
        String start_millis = CommonUtil.getFormatTime(start);

        long end = System.currentTimeMillis();
        String end_millis = CommonUtil.getFormatTime(end);

        String duration = end - start + "";
        CommonUtil.saveSessionTime(context);

        JSONObject info;
        try {
            info = prepareUsinglogJSON(start_millis, end_millis, duration,
                    pageName);
            CommonUtil.saveInfoToFile(CYConstants.TYPE_PAGE, info, context);
        } catch (JSONException e) {
            CYLog.e(CYConstants.LOG_TAG, e);
        }
    }

    public void onWebPage(String pageName,final Context context) {
        SharedPrefUtil sp = new SharedPrefUtil(context);
        String lastView = sp.getValue("CurrentWenPage", "");
        if (lastView.equals("")) {
            sp.setValue("CurrentWenPage", pageName);
            sp.setValue("session_save_time", System.currentTimeMillis());
        } else {
            long start = sp.getValue("session_save_time",
                    System.currentTimeMillis());
            String start_millis = CommonUtil.getFormatTime(start);

            long end = System.currentTimeMillis();
            String end_millis = CommonUtil.getFormatTime(end);

            String duration = end - start + "";

            sp.setValue("CurrentWenPage", pageName);
            sp.setValue("session_save_time", end);

            JSONObject obj;
            try {
                obj = prepareUsinglogJSON(start_millis, end_millis, duration,
                        lastView);
                CommonUtil.saveInfoToFile("activityInfo", obj, context);
            } catch (JSONException e) {
                CYLog.e(CYConstants.LOG_TAG, e);
            }
        }
    }
}
