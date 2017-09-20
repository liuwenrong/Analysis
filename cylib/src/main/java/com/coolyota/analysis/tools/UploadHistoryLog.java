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

import java.io.File;

/**
 * des: 上传历史的数据包括Page信息,事件信息的文件
 *
 * @author  liuwenrong
 * @version 1.0,2017/6/22
 */
public class UploadHistoryLog extends Thread {
    public Context context;
    private static final String TAG = "UploadHistory";

    public UploadHistoryLog(Context context) {
        super();
        this.context = context;
    }
    private String type = CYConstants.TYPE_ALL;

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public void run() {

        CYConstants.uploadEnabled = UploadFileUtil.isExperienceOn(context);
        if ( !CYConstants.uploadEnabled ) {
            CYLog.w(CYConstants.LOG_TAG, UploadHistoryLog.class, "uploadEnabled is false, can't upload !");
            return;
        }

        String baseDir = context.getCacheDir().getAbsolutePath()
                + CYConstants.CY;
        if (type.equals(CYConstants.TYPE_ALL)) {

            String pagePath = baseDir + CYConstants.TYPE_PAGE;
            String eventPath = baseDir + CYConstants.TYPE_EVENT;

            postData(pagePath, pagePath+CYConstants.UPLOAD, CYConstants.TYPE_PAGE);
            postData(eventPath, eventPath+CYConstants.UPLOAD, CYConstants.TYPE_EVENT);

        } else {
            String infoPath = baseDir + type;
            postData(infoPath, infoPath+CYConstants.UPLOAD, type);
        }
    }

    /**
     * @param srcFilePath 数据保存的文件路径
     * @param uploadFilePath 即将上传的文件路径
     * @param type 文件类型
     */
    private void postData(String srcFilePath, String uploadFilePath, String type) {
        //首先判断是否能发送，如果不能发送就没必要读文件了
//        if (!CommonUtil.isNetworkAvailable(context)) {
//            return;
//        }

        //判断xxInfoUpload文件是否存在
//        final ReentrantReadWriteLock rwl = CommonUtil.getRwl();

        File srcFile;//将 xxInfo 重命名 成 xxInfoUpload
        srcFile = new File(srcFilePath);
        final File uploadFile = new File(uploadFilePath);
        if (!srcFile.exists() && !uploadFile.exists()) {
            return;
        }

        if (!uploadFile.exists()){
            genUploadFile(srcFilePath, uploadFile);
        }
        //当 file或者uploadFile大于10M,删除长时间不上传的文件,  比如用户长期不使用网络,或者关闭了 用户体验开关
        if ((srcFile.exists() && srcFile.length() >= CYConstants.needDeleteSize) || (uploadFile.exists() && uploadFile.length() >= CYConstants.needDeleteSize)) {

            if (uploadFile.exists() && !CommonUtil.isNetworkTypeWifi(context)) {//不是WiFi的情况下删除旧数据
                uploadFile.delete();
            }

        }

        if (uploadFile.length()!=0) {
            //增强判断，以免网络突然中断, 网络可用,且是WiFi
//            if (CommonUtil.isNetworkAvailable(context)) {
            if (CommonUtil.isNetworkAvailable(context) && CommonUtil.isNetworkTypeWifi(context)) {

                UploadFileUtil.uploadFile(uploadFile, new UploadFileUtil.CallbackMessage() {
                    @Override
                    public void callbackMsg(MyMessage message) {
                        if (!message.isSuccess()) {// 不成功,打印错误
                            CYLog.e(CYConstants.LOG_TAG, UploadHistoryLog.class,"95--Message=" + message.getMsg());
                            //
                        } else { //成功后删除该文件xxInfoUpload

                            //{"code":-1,"msg":"系统异常"}
                            String msg = message.getMsg();
                            CYLog.d(CYConstants.LOG_TAG, UploadHistoryLog.class, "100--msg = " + msg);
                            try {
                                JSONObject jsonObj = new JSONObject(msg);
                                int code = jsonObj.getInt("code");
                                String msgJson = jsonObj.getString("msg");
                                if (code == ApiConstants.Code.Success) {
                                    CommonUtil.setLastUpdateTimeToSP(context);
                                    uploadFile.delete();
                                }


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                    }
                });

            }
        }

    }

    private boolean genUploadFile(String srcFilePath, File uploadFile) {
        File srcFile;//将 xxInfo 重命名 成 xxInfoUpload
        srcFile = new File(srcFilePath);
        if (!srcFile.exists()){
            return false;
        } else { //重命名时 加上读写锁 拿不到锁,导致没法上传,文件重命名拿不到锁
            /*while (!rwl.writeLock().tryLock()) {

                CYLog.w(CYConstants.LOG_TAG, UploadHistoryLog.class, "94---等待上锁重命名");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ;
            }
            rwl.writeLock().lock();*/

            try {
                srcFile.renameTo(uploadFile);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
//                    rwl.writeLock().unlock();
            }

        }
        return true;
    }

}
