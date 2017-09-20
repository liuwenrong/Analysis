package com.coolyota.analysis.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Parcelable;

import com.coolyota.analysis.CYAnalysis;
import com.coolyota.analysis.tools.CYLog;
import com.coolyota.analysis.tools.CommonUtil;
import com.coolyota.analysis.tools.UploadFileUtil;
import com.coolyota.analysis.tools.UploadHistoryLog;

/**
 * des: 监听网络变化的广播接收器
 *
 * @author liuwenrong@yotamobile.com
 * @version 1.0, 2017/9/18
 */
public class NetBroadcastReceiver extends BroadcastReceiver {

//    long mLastWifiConnectedTime;//每次广播都会生成不同的对象,没法用在记录时间

    @Override
    public void onReceive(final Context context, Intent intent) {
        // 如果相等的话就说明网络状态发生了变化,包括WiFi和数据网络, 暂时不用,所以没注册监听
        if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            boolean isWifi = CommonUtil.isNetworkTypeWifi(context);
            CYLog.d(CYAnalysis.TAG, NetBroadcastReceiver.class, "isWifi = " + isWifi);

        }

        // 这个监听wifi的打开与关闭，与网络连通性无关,并不一定连接了某个WiFi android.net.wifi.WIFI_STATE_CHANGED
        if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {//关闭WiFi,执行了两遍
            int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
            CYLog.d(CYAnalysis.TAG, NetBroadcastReceiver.class, "onReceive: WiFi连接改变 wifiState = " + wifiState);
            switch (wifiState) {
                case WifiManager.WIFI_STATE_DISABLING: //关闭Wifi,执行了一遍
                    CYLog.d(CYAnalysis.TAG, NetBroadcastReceiver.class, "onReceive: WIFI_STATE_DISABLING");
                    break;
                case WifiManager.WIFI_STATE_DISABLED: //关闭Wifi,执行了一遍
                    CYLog.d(CYAnalysis.TAG, NetBroadcastReceiver.class, "onReceive: WIFI_STATE_DISABLED");
                    break;
            }
        }

            // 监听wifi的连接状态,即是否连上了一个有效无线路由,未认证的WiFi也认为是有效WiFi
            if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {//关闭WiFi,执行了3次,打开WiFi,执行9次

                Parcelable parcelableExtra = intent
                        .getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (null != parcelableExtra) {
                    NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
                    NetworkInfo.State state = networkInfo.getState();
                    boolean isConnected = state == NetworkInfo.State.CONNECTED;// 当然，这边可以更精确的确定状态
                    CYLog.d(CYAnalysis.TAG, NetBroadcastReceiver.class, "onReceive: 57----------isConnected = " + isConnected);
                    if (isConnected) { //打开WiFi后同一时间会走两次,加个时间限制 两秒内不重复执行
                        CYLog.d(CYAnalysis.TAG, NetBroadcastReceiver.class, "onReceive: 监听WiFi,连接了有效的路由器");
                        if (System.currentTimeMillis() - CommonUtil.mLastWifiConnectedTime > 2000) {
                            // 执行一些事件,如上传
                            CYLog.d(CYAnalysis.TAG, NetBroadcastReceiver.class, "onReceive: 监听WiFi,连接了有效的路由器,并上传打点数据");
                            if (UploadFileUtil.UploadIfNeed(context)) {

                                //刚连上WiFi时,networkInfo = null 需要加个延时,等待WiFi完全连上再上传
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {

                                        try {
                                            Thread.sleep(3000);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        Thread thread = new UploadHistoryLog(context);
                                        thread.start();
                                    }
                                }).start();

                            }

                            CommonUtil.mLastWifiConnectedTime = System.currentTimeMillis();
                        }

                    }

                }

            }

        }
    }
