/* *
   * Copyright (C) 2017 BaoliYota Tech. Co., Ltd, LLC - All Rights Reserved.
   *
   * Confidential and Proprietary.
   * Unauthorized copying of this file, via any medium is strictly prohibited.
   * */

package com.coolyota.analysis.tools;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
/**
 * des: 上传activity的页面信息 json方式上传,暂时不用
 *
 * @author  liuwenrong
 * @version 1.0,2017/6/22
 */
public class UploadActivityLog extends Thread {
    public Context context;

    public UploadActivityLog(Context context) {
        super();
        this.context = context;
    }

    @Override
    public void run() {
        String cachfileactivity = context.getCacheDir().getAbsolutePath()
                + "/cy_" + CYConstants.TYPE_PAGE;
//        postActivityInfo(cachfileactivity); //暂时不传json
    }

    private void postActivityInfo(String cachfileactivity) {
        // 首先判断是否能发送，如果不能发送就没必要读文件了
        if (!CommonUtil.isNetworkAvailable(context)) {
            return;
        }
        //TODO 可以发送，读文件传 改成直接传文件
        String data = CommonUtil.readDataFromFile(cachfileactivity);

        try {
            String[] strArr = data.split(CYConstants.fileSep);
            JSONArray jsonArr = new JSONArray();
            JSONObject postData = new JSONObject();
            for (int i = 1; i < strArr.length; i++) {
                try {
                    JSONObject obj = new JSONObject(strArr[i])
                            .getJSONObject("activityInfo");
                    int jsonarrlength = jsonArr.length();
                    jsonArr.put(jsonarrlength, obj);
                } catch (Exception e) {
                    CYLog.e(CYConstants.LOG_TAG, UploadActivityLog.class, "Message=" + e.getMessage());
                    continue;
                }
            }
            if (jsonArr.length() == 0) {
                return;
            }
            postData.put("data", jsonArr);

            // 发送之前再度判断
            if (CommonUtil.isNetworkAvailable(context)) {
                CYLog.i(CYConstants.LOG_TAG, UploadActivityLog.class, "data post activity info");

                //TODO 发送到服务器
                MyMessage message = NetworkUtil.Post(CYConstants.BASE_URL + CYConstants.PAGE_URL, postData.toString());

                if (!message.isSuccess()) {
                    //不成功
                    CYLog.e(CYConstants.LOG_TAG, UploadActivityLog.class, message.getMsg());
                    for (int i = 0; i < jsonArr.length(); i++) {
                        CommonUtil.saveInfoToFile(CYConstants.TYPE_PAGE, jsonArr.getJSONObject(i), context);
                    }
                }


            } else {
                for (int i = 0; i < jsonArr.length(); i++) {
                    CommonUtil.saveInfoToFile(CYConstants.TYPE_PAGE, jsonArr.getJSONObject(i), context);
                }

            }

        } catch (JSONException e) {
            CYLog.e(CYConstants.LOG_TAG, e);
        }

    }
}
