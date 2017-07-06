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


/**
 * des: 生成设备信息,上传客户端数据 顺便生成Session时上传历史Page数据
 *
 * @author liuwenrong
 * @version 1.0, 2017/6/22
 */
public class ClientDataManager {
    private Context context;
    private final String PLATFORM = "android";

    public ClientDataManager(Context context) {
        this.context = context;
        DeviceInfo.init(context);
    }

    JSONObject prepareClientdataJSON() throws JSONException {
        JSONObject jsonClientdata = new JSONObject();

        jsonClientdata.put("deviceid", DeviceInfo.getDeviceId());
        jsonClientdata.put("os_version", DeviceInfo.getOsVersion());
        jsonClientdata.put("platform", PLATFORM);
        jsonClientdata.put("language", DeviceInfo.getLanguage());
        jsonClientdata.put("appkey", AppInfo.getAppKey(context));
        jsonClientdata.put("resolution", DeviceInfo.getResolution());
        jsonClientdata.put("isMobileDevice", true);
        jsonClientdata.put("phoneType", DeviceInfo.getPhoneType());
        jsonClientdata.put("imsi", DeviceInfo.getIMSI());


        jsonClientdata.put("mccmnc", DeviceInfo.getMCCMNC());
        jsonClientdata.put("cellId", DeviceInfo.getCellInfoofCID());
        jsonClientdata.put("lac", DeviceInfo.getCellInfoofLAC());


        jsonClientdata.put("network", DeviceInfo.getNetworkTypeWIFI2G3G());
        jsonClientdata.put("time", DeviceInfo.getDeviceTime());
        jsonClientdata.put("version", AppInfo.getAppVersion(context));
        jsonClientdata.put("userIdentifier",
                CommonUtil.getUserIdentifier(context));
        jsonClientdata.put("moduleName", DeviceInfo.getDeviceProduct());
        jsonClientdata.put("deviceName", DeviceInfo.getDeviceName());
        jsonClientdata.put("wifiMac", DeviceInfo.getWifiMac());
        jsonClientdata.put("haveBt", DeviceInfo.getBluetoothAvailable());
        jsonClientdata.put("haveWifi", DeviceInfo.getWiFiAvailable());
        jsonClientdata.put("haveGps", DeviceInfo.getGPSAvailable());
        jsonClientdata.put("haveGravity", DeviceInfo.getGravityAvailable());
        jsonClientdata.put("session_id", CommonUtil.getSessionId(context));
        jsonClientdata.put("salt", CommonUtil.getSALT(context));
        jsonClientdata.put("lib_version", CYConstants.LIB_VERSION);

        if (CommonUtil.isSupportlocation(context)) {
            jsonClientdata.put("latitude", DeviceInfo.getLatitude());
            jsonClientdata.put("longitude", DeviceInfo.getLongitude());
        }

        return jsonClientdata;
    }
    public void judgeSession(final Context context){
    	CYLog.i(CYConstants.LOG_TAG,SavePageManager.class, "judgeSession on clientdata");
        try {
            if (CommonUtil.isNewSession(context)) {
              String  session_id = CommonUtil.generateSession(context);
                CYLog.i(CYConstants.LOG_TAG, SavePageManager.class,"New SessionId is " + session_id);
            }
        } catch (Exception e) {
            CYLog.e(CYConstants.LOG_TAG, e);
        }
    }
    public void postClientData() {
        JSONObject clientData;
        judgeSession(context);
        //目前不用传客户端数据
//        try {
//            clientData = prepareClientdataJSON();
//        } catch (Exception e) {
//            CYLog.e(CYConstants.LOG_TAG, e);
//            return;
//        }
//        JSONObject postData = new JSONObject();
//        try {
//            postData.put("data", new JSONArray().put(clientData));
//        } catch (JSONException e) {
//            CYLog.e(CYConstants.LOG_TAG, e);
//        }
//
//        if (CommonUtil.isNetworkAvailable(context)) {
//            MyMessage message = NetworkUtil.Post(CYConstants.BASE_URL
//                    + CYConstants.CLIENTDATA_URL, postData.toString());
//
//            if (!message.isSuccess()) {
//                CYLog.e(CYConstants.LOG_TAG, ClientDataManager.class, "Error Code=" + message.getMsg());
//                CommonUtil.saveInfoToFile("clientData", clientData, context);
//            }
//        } else {
//            CommonUtil.saveInfoToFile("clientData", clientData, context);
//        }
    }
}
