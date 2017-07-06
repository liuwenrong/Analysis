/* *
   * Copyright (C) 2017 BaoliYota Tech. Co., Ltd, LLC - All Rights Reserved.
   *
   * Confidential and Proprietary.
   * Unauthorized copying of this file, via any medium is strictly prohibited.
   * */
package com.coolyota.analysis.tools;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * des:
 *
 * @author liuwenrong
 * @version 1.0, 2017/6/26
 */
public class DeviceInfo {

    public static final String TAG = "DeviceInfo";
    public static final String SP_KEY_ID = "uniqueuid";
    private static Context context;
    private static Location location;
    private static TelephonyManager telephonyManager;
    private static LocationManager locationManager;
    private static BluetoothAdapter bluetoothAdapter;
    private static SensorManager sensorManager;
    private static String DEVICE_ID = "";
    private static String DEVICE_NAME = "";
    private static SharedPrefUtil mSharedPrefUtil;

    DeviceInfo() {
    }

    public static void init(Context context) {
        DeviceInfo.context = context;

        try {
            mSharedPrefUtil = new SharedPrefUtil(context);
            telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        } catch (Exception var2) {
            CYLog.e(TAG, DeviceInfo.class, var2.toString());
        }

        getLocation();
    }

    public static String getKey() {
        String key = ApiConstants.VALUE_KEY;
        if (TextUtils.isEmpty(key)) {
            if (mSharedPrefUtil != null) {
                key = mSharedPrefUtil.getValue("key", "");
            }else {
//                CYLog.e(CYConstants.LOG_TAG, DeviceInfo.class, "");
            }
        }
        return key;
    }

    public static String getLanguage() {
        String language = Locale.getDefault().getLanguage();
        CYLog.i(TAG, DeviceInfo.class, "getLanguage()=" + language);
        return language == null ? "" : language;
    }

    public static String getCellInfoofLAC() {
        CellLocation cl = telephonyManager.getCellLocation();
        int lac;
        if (cl instanceof GsmCellLocation) {
            GsmCellLocation cdma1 = (GsmCellLocation) cl;
            lac = cdma1.getLac();
            return String.valueOf(lac);
        } else if (cl instanceof CdmaCellLocation) {
            CdmaCellLocation cdma = (CdmaCellLocation) cl;
            lac = cdma.getNetworkId();
            return String.valueOf(lac);
        } else {
            return "";
        }
    }

    public static String getCellInfoofCID() {
        CellLocation cl = telephonyManager.getCellLocation();
        int cid;
        if (cl instanceof GsmCellLocation) {
            GsmCellLocation cdma1 = (GsmCellLocation) cl;
            cid = cdma1.getCid();
            return String.valueOf(cid);
        } else if (cl instanceof CdmaCellLocation) {
            CdmaCellLocation cdma = (CdmaCellLocation) cl;
            cid = cdma.getBaseStationId();
            return String.valueOf(cid);
        } else {
            return "";
        }
    }

    public static String getResolution() {
        DisplayMetrics displaysMetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(displaysMetrics);
        CYLog.i(TAG, DeviceInfo.class, "getResolution()=" + displaysMetrics.widthPixels + "x" + displaysMetrics.heightPixels);
        return displaysMetrics.widthPixels + "x" + displaysMetrics.heightPixels;
    }

    public static String getDeviceProduct() {
        String result = Build.PRODUCT;
        CYLog.i(TAG, DeviceInfo.class, "getDeviceProduct()=" + result);
        return result == null ? "" : result;
    }

    public static boolean getBluetoothAvailable() {
        return bluetoothAdapter != null;
    }

    private static boolean isSimulator() {
        return getDeviceIMEI().equals("000000000000000");
    }

    public static boolean getGravityAvailable() {
        try {
            if (isSimulator()) {
                sensorManager = null;
            } else {
                sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            }

            CYLog.i(TAG, DeviceInfo.class, "getGravityAvailable()");
            return sensorManager != null;
        } catch (Exception var1) {
            return false;
        }
    }

    public static String getOsVersion() {
        String result = Build.VERSION.RELEASE;
        CYLog.i(TAG, DeviceInfo.class, "getOsVersion()=" + result);
        return result == null ? "" : result;
    }

    public static int getPhoneType() {
        if (telephonyManager == null) {
            return -1;
        } else {
            int result = telephonyManager.getPhoneType();
            CYLog.i(TAG, DeviceInfo.class, "getPhoneType()=" + result);
            return result;
        }
    }

    public static String getIMSI() {
        String result = "";

        try {
            if (!CommonUtil.checkPermissions(context, "android.permission.READ_PHONE_STATE")) {
                CYLog.e(TAG, DeviceInfo.class, "READ_PHONE_STATE permission should be added into AndroidManifest.xml.");
                return "";
            } else {
                result = telephonyManager.getSubscriberId();
                CYLog.i(TAG, DeviceInfo.class, "getIMSI()=" + result);
                return result == null ? "" : result;
            }
        } catch (Exception var2) {
            CYLog.e(TAG, var2);
            return result;
        }
    }

    public static String getWifiMac() {
        try {
            WifiManager e = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wi = e.getConnectionInfo();
            String result = wi.getMacAddress();
            if (result == null) {
                result = "";
            }

            CYLog.i(TAG, DeviceInfo.class, "getWifiMac()=" + result);
            return result;
        } catch (Exception var3) {
            CYLog.e(TAG, var3);
            return "";
        }
    }

    public static String getDeviceTime() {
        try {
            Date e = new Date();
            SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            return localSimpleDateFormat.format(e);
        } catch (Exception var2) {
            return "";
        }
    }

    public static String getDeviceName() {
        if (DEVICE_NAME.equals("")) {
            try {
                String e = Build.MANUFACTURER;
                if (e == null) {
                    e = "";
                }

                String model = Build.MODEL;
                if (model == null) {
                    model = "";
                }

                if (model.startsWith(e)) {
                    DEVICE_NAME = capitalize(model).trim();
                } else {
                    DEVICE_NAME = (capitalize(e) + " " + model).trim();
                }
            } catch (Exception var2) {
                CYLog.e(TAG, var2);
                return "";
            }
        }

        return DEVICE_NAME;
    }

    public static String getNetworkTypeWIFI2G3G() {
        try {
            ConnectivityManager e = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo ni = e.getActiveNetworkInfo();
            String type = "";
            if (ni != null && ni.getTypeName() != null) {
                type = ni.getTypeName().toLowerCase(Locale.US);
                if (!type.equals("wifi")) {
                    type = e.getNetworkInfo(0).getExtraInfo();
                }
            }

            return type;
        } catch (Exception var3) {
            return "";
        }
    }

    public static boolean getWiFiAvailable() {
        try {
            if (!CommonUtil.checkPermissions(context, "android.permission.ACCESS_WIFI_STATE")) {
                CYLog.e(TAG, DeviceInfo.class, "ACCESS_WIFI_STATE permission should be added into AndroidManifest.xml.");
                return false;
            } else {
                ConnectivityManager e = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                if (e != null) {
                    NetworkInfo[] info = e.getAllNetworkInfo();
                    if (info != null) {
                        NetworkInfo[] var5 = info;
                        int var4 = info.length;

                        for (int var3 = 0; var3 < var4; ++var3) {
                            NetworkInfo anInfo = var5[var3];
                            if (anInfo.getType() == 1 && anInfo.isConnected()) {
                                return true;
                            }
                        }
                    }
                }

                return false;
            }
        } catch (Exception var6) {
            return false;
        }
    }

    public static String getDeviceIMEI() {
        String result = "";

        try {
            if (!CommonUtil.checkPermissions(context, "android.permission.READ_PHONE_STATE")) {

                if (context != null) {
                    CYLog.e(TAG, DeviceInfo.class, "286--READ_PHONE_STATE permission should be added into AndroidManifest.xml.");
                }
                return "";
            }

            result = telephonyManager.getDeviceId();
            if (result == null) {
                result = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID); //获取安卓ID
//                result = "";
            }
        } catch (Exception var2) {
            CYLog.e(TAG, var2);
        }

        return result;
    }

    public static String getPhoneNum() {
        String result = "";

        try {
            if (!CommonUtil.checkPermissions(context, "android.permission.READ_PHONE_STATE")) {
                CYLog.e(TAG, DeviceInfo.class, "READ_PHONE_STATE permission should be added into AndroidManifest.xml.");
                return "";
            }

            result = telephonyManager.getLine1Number();
            if (result == null) {
                result = "";
            }
        } catch (Exception var2) {
            CYLog.e(TAG, var2);
        }

        return result;
    }

    private static String getSSN() {
        String result = "";

        try {
            if (!CommonUtil.checkPermissions(context, "android.permission.READ_PHONE_STATE")) {
                CYLog.e(TAG, DeviceInfo.class, "READ_PHONE_STATE permission should be added into AndroidManifest.xml.");
                return "";
            }

            result = telephonyManager.getSimSerialNumber();
            if (result == null) {
                result = "";
            }
        } catch (Exception var2) {
            CYLog.e(TAG, var2);
        }

        return result;
    }

    public static String getDeviceId() {
        if (DEVICE_ID.equals("")) {
            try {
                SharedPrefUtil e = new SharedPrefUtil(context);
                String uniqueid = e.getValue(SP_KEY_ID, "");
                if (!uniqueid.equals("")) {
                    DEVICE_ID = uniqueid;
                } else {
                    String imei = getDeviceIMEI();
                    String imsi = getIMSI();
                    String salt = CommonUtil.getSALT(context);
                    DEVICE_ID = CommonUtil.md5(imei + imsi + salt);
                    CYLog.d(TAG, DeviceInfo.class, "imei = " + imei + ", imsi = " + imsi + ", salt = " + salt + ", DeviceId = " + DEVICE_ID);
                    e.setValue(SP_KEY_ID, DEVICE_ID);
                }
            } catch (Exception e) {
                CYLog.e(TAG, e);
            }
        }

        return DEVICE_ID;
    }

    public static void setDeviceId(String did) {
        DEVICE_ID = did;
    }

    public static String getLatitude() {
        return location == null ? "" : String.valueOf(location.getLatitude());
    }

    public static String getLongitude() {
        return location == null ? "" : String.valueOf(location.getLongitude());
    }

    public static String getGPSAvailable() {
        return location == null ? "false" : "true";
    }

    private static void getLocation() {
        CYLog.i(TAG, DeviceInfo.class, "getLocation");

        try {
            List e = locationManager.getAllProviders();
            Iterator var2 = e.iterator();

            while (var2.hasNext()) {
                String prociderString = (String) var2.next();
                location = locationManager.getLastKnownLocation(prociderString);
                if (location != null) {
                    break;
                }
            }
        } catch (Exception var3) {
            CYLog.e(TAG, DeviceInfo.class, var3.toString());
        }

    }

    public static String getMCCMNC() {
        String result;
        try {
            String e = telephonyManager.getNetworkOperator();
            if (e == null) {
                result = "";
            } else {
                result = e;
            }
        } catch (Exception var2) {
            result = "";
            CYLog.e(TAG, DeviceInfo.class, var2.toString());
        }

        return result;
    }

    private static String capitalize(String s) {
        if (s != null && s.length() != 0) {
            char first = s.charAt(0);
            return Character.isUpperCase(first) ? s : Character.toUpperCase(first) + s.substring(1);
        } else {
            return "";
        }
    }

    /**
     * @return 产品型号 (Y3) ro.product.brand ro.product.model
     */
    public static String getProType(){
        return Build.BRAND + " " +Build.MODEL;
    }

    /**
     * @return 系统版本 Y3XSCN061000DPX1707031 Y3-userdebug 7.1.1 230 test-keys
     */
    public static String getSysVersion() {
        return SystemProperties.get("persist.sys.zs.last_build", "Y3XSCN061000DPX1707031 Y3-userdebug 7.1.1 230 test-keys");
    }

}
