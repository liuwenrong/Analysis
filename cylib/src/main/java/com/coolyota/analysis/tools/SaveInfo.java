/* *
   * Copyright (C) 2017 BaoliYota Tech. Co., Ltd, LLC - All Rights Reserved.
   *
   * Confidential and Proprietary.
   * Unauthorized copying of this file, via any medium is strictly prohibited.
   * */
package com.coolyota.analysis.tools;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * des:
 *
 * @author liuwenrong
 * @version 1.0, 2017/6/22
 */
public class SaveInfo extends Thread {
    public static final String TAG = "SaveInfo";
    public JSONArray arrObj;
    private String filetype;
    private String filePath;
    private SharedPrefUtil prefUtil;
    private Context context;

    public SaveInfo(JSONArray objArray, String filetype, String cacheFilePath, SharedPrefUtil prefUtil, Context context) {
        this.arrObj = objArray;
        this.filePath = cacheFilePath;
        this.filetype = filetype;
        this.prefUtil = prefUtil;
        this.context = context;
    }

    private static Handler handler;

    static {
        HandlerThread localHandlerThread = new HandlerThread("CYAnalysis");
        localHandlerThread.start();
        handler = new Handler(localHandlerThread.getLooper());
    }

    public void run() {
        CYLog.d(TAG, SaveInfo.class, "-----Save cache file " + this.filePath);
        if (this.arrObj.length() != 0) {
            File file = new File(this.filePath);
            long filesize = this.prefUtil.getValue("file_size", CYConstants.defaultFileSize);
            JSONArray usefulData = null;
            CYLog.d(TAG, SaveInfo.class, "58--------file size:" + file.length());
            if (file.length() > filesize) { //当文件大于默认大小1M即1048576时,文件上传

                CYLog.d(TAG, SaveInfo.class, "61--------file size:" + file.length());
                UploadHistoryLog thread = new UploadHistoryLog(context);
                thread.setType(filetype);
                handler.post(thread);

            }

            if (usefulData != null) {
                JSONArray wholeJsonArray = this.mergeJsonArray(usefulData, this.arrObj);
                this.fileAppend(wholeJsonArray);
            } else {
                this.fileAppend(this.arrObj);
            }

        }
    }

    /**
     * 合并JsonArr,将文件中得到的最后一个JSONObj和传进来的arr合并
     * @param usefulData
     * @param arrObj
     * @return
     */
    private JSONArray mergeJsonArray(JSONArray usefulData, JSONArray arrObj) {
        JSONArray wholeArray = new JSONArray();

        int j;
        JSONObject jsonObject;
        for (j = 0; j < usefulData.length(); ++j) {
            jsonObject = new JSONObject();

            try {
                jsonObject = (JSONObject) usefulData.get(j);
            } catch (JSONException var8) {
                CYLog.e(TAG, var8);
            }

            wholeArray.put(jsonObject);
        }

        for (j = 0; j < arrObj.length(); ++j) {
            jsonObject = new JSONObject();

            try {
                jsonObject = (JSONObject) arrObj.get(j);
            } catch (JSONException var7) {
                CYLog.e(TAG, var7);
            }

            wholeArray.put(jsonObject);
        }

        return wholeArray;
    }

    /**
     * 从文件中读取最后一个的session_id ,如果新增的arr的第一个obj的session_id相同,则把最后一个加进去,得到usefulData
     * @param filePath
     * @return
     */
    private JSONArray retrieveDataFromFile(String filePath) {
        String data = CommonUtil.readDataFromFile(filePath);
        JSONArray usefulData = new JSONArray();
        if (data.length() > 0) {
            //TODO 通过@_@切割改成 读取行
//            data.
            String[] dataArr = data.split(CYConstants.fileSep);

            try {
                JSONObject e = (new JSONObject(dataArr[dataArr.length - 1])).getJSONObject(CYConstants.TYPE_PAGE); //文件中最后一个JSONObj
                if (this.arrObj.getJSONObject(0).getString("session_id").equals(e.getString("session_id"))) {
                    usefulData.put(e);
                }
            } catch (JSONException var6) {
                CYLog.e(TAG, var6);
            }
        }

        return usefulData;
    }

    /**
     * 文件追加的方式,写入arr中的每一个Obj
     * @param array
     */
    public void fileAppend(JSONArray array) {
        FileWriter writer = null;
        ReentrantReadWriteLock rwl = CommonUtil.getRwl();

        while (!rwl.writeLock().tryLock()) {
            CYLog.w(CYConstants.LOG_TAG, UploadHistoryLog.class, "151---等待上锁写入文件");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ;
        }
        rwl.writeLock().lock();

        try {
            writer = new FileWriter(this.filePath, true);

            CYLog.d(TAG, SaveInfo.class, "157==json data " + this.arrObj.toString());

            for (int e = 0; e < array.length(); ++e) {
                JSONObject jsonObject = new JSONObject();

                try {
                    jsonObject.put(this.filetype, array.get(e));
//                    writer.write(CYConstants.fileSep + jsonObject.toString()); //写入@_@+JsonObj
                    writer.write( jsonObject.toString() + CYConstants.newLine); //JsonObj+换行
                } catch (JSONException var16) {
                    CYLog.e(TAG, var16);
                }
            }
        } catch (IOException var17) {
            CYLog.e(TAG, var17);
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException var15) {
                CYLog.e(TAG, var15);
            }

            rwl.writeLock().unlock();
        }

    }
}
