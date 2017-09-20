/* *
   * Copyright (C) 2017 BaoliYota Tech. Co., Ltd, LLC - All Rights Reserved.
   *
   * Confidential and Proprietary.
   * Unauthorized copying of this file, via any medium is strictly prohibited.
   * */
package com.coolyota.analysis.tools;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;

import com.coolyota.analysis.CYAnalysis;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * des:
 *
 * @author liuwenrong
 * @version 1.0, 2017/6/22
 */
public class CommonUtil {
    public static final String TAG = "CYAnalysis";
    public static final String SESSION_ON_Pause_SAVE_TIME = "session_save_time";
    public static final String START_TIME = "start_time";
    public static final int ONE_DAY = 86400000;
    public static final int SIX_HOURS = 21600000;
    public static long mLastWifiConnectedTime;
    private static String USER_ID = "";
    private static String curVersion = "";
    private static ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

    CommonUtil() {
    }

    public static ReentrantReadWriteLock getRwl() {
        return rwl;
    }

    public static void saveInfoToFile(String type, JSONObject info, Context context) {
        JSONArray array = new JSONArray();
        array.put(info);
        saveInfoToFile(type, array, context);
    }

    public static void saveInfoToFile(String type, JSONArray info, Context context) {
        try {
            String filePath = context.getCacheDir().getAbsolutePath() + CYConstants.CY + type;
            SharedPrefUtil sp = new SharedPrefUtil(context);
            SaveInfo t = new SaveInfo(info, type, filePath, sp, context);
            t.run();
        } catch (Exception var6) {
            CYLog.e(TAG, var6);
        }

    }

    public static boolean checkPermissions(Context context, String permission) {
        if (context != null && permission != null && !permission.equals("")) {
            PackageManager pm = context.getPackageManager();
            return pm.checkPermission(permission, context.getPackageName()) == 0;
        } else if (context == null) {
            CYLog.e(TAG, CommonUtil.class, "context is null, please call CYAnalysis.init(context, appKey); ");
            return false;
        } else {
            CYLog.e(TAG, CommonUtil.class, "Incorrect parameters");
            return false;
        }
    }

    public static String getUserIdentifier(Context context) {
        if (context == null) {
            CYLog.e(TAG, CommonUtil.class, "context is null");
            return "";
        } else {
            if (USER_ID.equals("")) {
                SharedPrefUtil sp = new SharedPrefUtil(context);
                USER_ID = sp.getValue("identifier", "");
                if (USER_ID.equals("")) {
                    USER_ID = md5(DeviceInfo.getPhoneNum());
                    if ("".equals(DeviceInfo.getPhoneNum()) && Build.VERSION.SDK_INT >= 9) {
                        USER_ID = Build.SERIAL;
                    }

                    sp.setValue("identifier", USER_ID);
                }
            }

            return USER_ID;
        }
    }

    public static void setUserIdentifier(Context context, String identifier) {
        if (context == null) {
            CYLog.e(TAG, CommonUtil.class, "context is null");
        }

        SharedPrefUtil sp = new SharedPrefUtil(context);
        sp.setValue("identifier", identifier);
        USER_ID = identifier;
    }

    public static CYAnalysis.SendPolicy getReportPolicyMode(Context context) {
        int type = getLocalDefaultReportPolicy(context);
        switch (type) {
            case 0:
//                CYAnalysis.setDefaultReportPolicy(context, SendPolicy.POST_ONSTART);
                break;
            case 1:
//                CYAnalysis.setDefaultReportPolicy(context, SendPolicy.POST_NOW);
                break;
            case 2:
//                CYAnalysis.setDefaultReportPolicy(context, SendPolicy.POST_INTERVAL);
        }

        return CYConstants.mReportPolicy;
    }

    public static boolean isNetworkAvailable(Context context) {
        if (context == null) {
            CYLog.e(TAG, CommonUtil.class, "context is null");
            return false;
        } else if (checkPermissions(context, "android.permission.INTERNET")) {
            ConnectivityManager cManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cManager == null) {
                return false;
            } else {
                NetworkInfo info = cManager.getActiveNetworkInfo();
                if (info != null && info.isAvailable()) {
                    CYLog.i(TAG, CommonUtil.class, "Network is available.");
                    return true;
                } else {
                    CYLog.i(TAG, CommonUtil.class, "Network is not available.");
                    return false;
                }
            }
        } else {
            CYLog.e(TAG, CommonUtil.class, "android.permission.INTERNET permission should be added into AndroidManifest.xml.");
            return false;
        }
    }

    public static String getActivityName(Context context) {
        if (context == null) {
            CYLog.e(TAG, CommonUtil.class, "context is null");
            return "";
        } else if (context instanceof Activity) {
            String activityName= "";

            try {
                activityName = ((Activity) context).getComponentName().getShortClassName();
            } catch (Exception var4) {
                CYLog.e("can not get name", var4);
            }

            if (activityName.startsWith(".")) {
                activityName = activityName.replaceFirst(".", "");
            }

            return activityName;
        } else {
            return "";
        }
    }

    public static String getCurVersionCode(Context context) {
        if (context == null) {
            CYLog.e(TAG, CommonUtil.class, "context is null");
            return "";
        } else {
            if (curVersion.equals("")) {
                try {
                    PackageManager e = context.getPackageManager();
                    PackageInfo pi = e.getPackageInfo(context.getPackageName(), 0);
                    curVersion = String.valueOf(pi.versionCode);
                    if (curVersion.length() <= 0) {
                        return "";
                    }
                } catch (Exception var3) {
                    CYLog.e(TAG, var3);
                }
            }

            return curVersion;
        }
    }

    public static int getNetworkIntType(Context ctx) {

        if (ctx == null) {
            CYLog.e(TAG, CommonUtil.class, "context is null");
            return TelephonyManager.NETWORK_TYPE_UNKNOWN;
        } else {
            TelephonyManager manager = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
            int type = manager.getNetworkType();
            return type;
        }
    }


    public static String getNetworkType(Context context) {
        if (context == null) {
            CYLog.e(TAG, CommonUtil.class, "context is null");
            return "";
        } else {
            TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            int type = manager.getNetworkType();
            String typeString = "UNKNOWN";
            if (type == 4) {
                typeString = "CDMA";
            }

            if (type == 2) {
                typeString = "EDGE";
            }

            if (type == 5) {
                typeString = "EVDO_0";
            }

            if (type == 6) {
                typeString = "EVDO_A";
            }

            if (type == 1) {
                typeString = "GPRS";
            }

            if (type == 8) {
                typeString = "HSDPA";
            }

            if (type == 10) {
                typeString = "HSPA";
            }

            if (type == 9) {
                typeString = "HSUPA";
            }

            if (type == 3) {
                typeString = "UMTS";
            }

            if (type == 0) {
                typeString = "UNKNOWN";
            }

            if (type == 7) {
                typeString = "1xRTT";
            }

            if (type == 11) {
                typeString = "iDen";
            }

            if (type == 12) {
                typeString = "EVDO_B";
            }

            if (type == 13) {
                typeString = "LTE";
            }

            if (type == 14) {
                typeString = "eHRPD";
            }

            if (type == 15) {
                typeString = "HSPA+";
            }

            return typeString;
        }
    }

    static boolean isNewSession(Context context) {
        if (context == null) {
            CYLog.e(TAG, CommonUtil.class, "context is null");
            return false;
        } else {
            try {
                long e = System.currentTimeMillis();
                SharedPrefUtil sp = new SharedPrefUtil(context);
                long session_save_time = sp.getValue("session_save_time", 0L);
                CYLog.i(TAG, CommonUtil.class, "currenttime=" + e);
                CYLog.i(TAG, CommonUtil.class, "session_save_time=" + session_save_time);
                if (e - session_save_time > getSessionContinueMillis(context)) {
                    CYLog.i(TAG, CommonUtil.class, "return true,create new session.");
                    return true;
                } else {
                    CYLog.i(TAG, CommonUtil.class, "return false.At the same session.");
                    return false;
                }
            } catch (Exception var6) {
                CYLog.e(TAG, var6);
                return true;
            }
        }
    }

    public static boolean isNetworkTypeWifi(Context context) {
        if (context == null) {
            CYLog.e(TAG, CommonUtil.class, "context is null");
            return false;
        } else if (checkPermissions(context, "android.permission.INTERNET")) {
            ConnectivityManager cManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cManager == null) {
                return false;
            } else {
                NetworkInfo info = cManager.getActiveNetworkInfo();
                if (info != null && info.isAvailable() && info.getType() == ConnectivityManager.TYPE_WIFI) {
                    CYLog.i(TAG, CommonUtil.class, "Active Network type is wifi");
                    return true;
                } else {
                    CYLog.i(TAG, CommonUtil.class, "Active Network type is not wifi");
                    return false;
                }
            }
        } else {
            CYLog.e(TAG, CommonUtil.class, "android.permission.INTERNET permission should be added into AndroidManifest.xml.");
            return false;
        }
    }

    public static String md5(String str) {
        try {
            MessageDigest e = MessageDigest.getInstance("MD5");
            e.update(str.getBytes());
            byte[] arrayOfByte = e.digest();
            StringBuffer localStringBuffer = new StringBuffer();
            byte[] var7 = arrayOfByte;
            int var6 = arrayOfByte.length;

            for (int var5 = 0; var5 < var6; ++var5) {
                byte anArrayOfByte = var7[var5];
                int j = 255 & anArrayOfByte;
                if (j < 16) {
                    localStringBuffer.append("0");
                }

                localStringBuffer.append(Integer.toHexString(j));
            }

            return localStringBuffer.toString();
        } catch (Exception var9) {
            CYLog.e(TAG, var9);
            return "";
        }
    }

    /**
     * @param context
     * @return 根据context生成的sessionId
     */
    static String generateSession(Context context) {
        String sessionId = "";
        String str = DeviceInfo.getDeviceId();
        str = str + DeviceInfo.getDeviceTime();
        sessionId = md5(str);
        SharedPrefUtil sp = new SharedPrefUtil(context);
        sp.setValue("session_id", sessionId);
        saveSessionTime(context, System.currentTimeMillis());
        UploadActivityLog threadActivity = new UploadActivityLog(context);
        threadActivity.run();
        return sessionId;
    }

    public static String getSessionId(Context context) {
        SharedPrefUtil sp = new SharedPrefUtil(context);
        String session_id = sp.getValue("session_id", "");
        if (session_id.equals("")) {
            session_id = generateSession(context);
        }

        return session_id;
    }

    public static boolean isTodayUpdate(Context context) {

        SharedPrefUtil mSharedPreferences = new SharedPrefUtil(context);
        long lastUpdateTime = mSharedPreferences.getValue(SharedPrefUtil.LAST_UPDATE_TIME, 0);
        boolean isTodayUpdate = isToday(lastUpdateTime, null);
        return isTodayUpdate;

    }

    /**
     * @param time       time stamp
     * @param formatType time conversion format
     * @return is it the day
     */
    public static boolean isToday(long time, @Nullable String formatType) {
        if (time == 0) {
            long currentTime = new Date().getTime();
            time = currentTime - ONE_DAY;
        }
        if (formatType == null) {
            formatType = "yyyy-MM-dd";
        }
        Date dateOld = new Date(time);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(formatType, Locale.getDefault());
        if (simpleDateFormat.format(dateOld).equals(simpleDateFormat.format(new Date()))) {
            return true;
        } else {
            return false;
        }
    }

    public static void setLastUpdateTimeToSP(Context context) {
        SharedPrefUtil mSharedPreferences = new SharedPrefUtil(context);
        mSharedPreferences.setValue(SharedPrefUtil.LAST_UPDATE_TIME, new Date().getTime());

    }

    /**
     * 记录结束的时间 onPause
     *
     * @param context
     */
    static void saveSessionTime(Context context, long curTimeMillis) {
        SharedPrefUtil sp = new SharedPrefUtil(context);
        sp.setValue(SESSION_ON_Pause_SAVE_TIME, curTimeMillis);
    }

    /**
     * onResume保存时间
     *
     * @param context
     */
    static void saveResumeTime(SharedPrefUtil sp, Context context) {
        sp.setValue(START_TIME, System.currentTimeMillis());
    }

    static void savePageName(Context context, String pageName) {
        SharedPrefUtil sp = new SharedPrefUtil(context);
        sp.setValue("CurrentPage", pageName);
        saveResumeTime(sp, context);
    }

    static String getFormatTime(long timestamp) {
        try {
            Date e = new Date(timestamp);
            SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            String result = localSimpleDateFormat.format(e);
            return result;
        } catch (Exception var5) {
            return "";
        }
    }

    static void saveDefaultReportPolicy(Context context, int i) {
        SharedPrefUtil spu = new SharedPrefUtil(context);
        spu.setValue("DefaultReportPolicy", (long) i);
    }

    static int getLocalDefaultReportPolicy(Context context) {
        SharedPrefUtil spu = new SharedPrefUtil(context);
        return (int) spu.getValue("DefaultReportPolicy", 1L);
    }

    static long getSessionContinueMillis(Context context) {
        SharedPrefUtil spu = new SharedPrefUtil(context);
        return spu.getValue("SessionContinueMillis", CYConstants.kContinueSessionMillis);
    }

    static boolean isUpdateOnlyWIFI(Context context) {
        SharedPrefUtil spu = new SharedPrefUtil(context);
        return spu.getValue("updateOnlyWifiStatus", Boolean.valueOf(CYConstants.mUpdateOnlyWifi)).booleanValue();
    }

    static boolean isSupportlocation(Context context) {
        SharedPrefUtil spu = new SharedPrefUtil(context);
        return spu.getValue("locationStatus", Boolean.valueOf(CYConstants.mProvideGPSData)).booleanValue();
    }

    /**
     * 通过一行行读取文件,生成JsonArr
     *
     * @param srcFileName
     * @param key
     * @return
     */
    static File genUploadFile(String srcFileName, String key) {
        JSONArray jsonArr = new JSONArray();

        String tarFileName = srcFileName + CYConstants.UPLOAD;
        File srcFile = new File(srcFileName);
        File tarFile = null;
        if (!srcFile.exists()) {
            return tarFile;
        } else {
            tarFile = new File(tarFileName);
            if (!tarFile.exists()) {
                try {
                    tarFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            FileInputStream in = null;
//            DataInputStream dataIS = null; //readLine()已过期
            InputStreamReader inputStreamReader = null;
            BufferedReader bufferedReader = null;
            StringBuffer dataBuffer = new StringBuffer();

            FileWriter writer = null;

            ReentrantReadWriteLock rwl = getRwl();
            while (!rwl.writeLock().tryLock()) {
                ;
            }

            rwl.writeLock().lock();

            try {
                in = new FileInputStream(srcFile);
                inputStreamReader = new InputStreamReader(in);
                bufferedReader = new BufferedReader(inputStreamReader);
//                    dataIS = new DataInputStream(in);

                writer = new FileWriter(tarFile, true);

                String strLine;
                while ((strLine = bufferedReader.readLine()) != null) {

                    if (!strLine.equals("")) {

                        writer.write(strLine + CYConstants.newLine); //JsonObj+换行

                        try {
                            JSONObject e = new JSONObject(strLine);
                            jsonArr.put(e);
                        } catch (Exception var10) {
                            CYLog.e(TAG, var10);
                        }

                    }

                }
            } catch (Exception var15) {
                CYLog.e(TAG, var15);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException var14) {
                        CYLog.e(TAG, var14);
                    }
                }

                rwl.readLock().unlock();
                srcFile.delete();
            }
        }

        return tarFile;
    }

    static JSONArray getJSONData(String cacheFileData, String key) {
        JSONArray jsonarr = new JSONArray();
        String data = readDataFromFile(cacheFileData);
        if (data.length() > 0) {
            String[] dataArr = data.split(CYConstants.fileSep);
            String[] var8 = dataArr;
            int var7 = dataArr.length;

            for (int var6 = 0; var6 < var7; ++var6) {
                String dataStr = var8[var6];
                if (!dataStr.equals("")) {
                    try {
                        JSONObject e = (new JSONObject(dataStr)).getJSONObject(key);
                        jsonarr.put(e);
                    } catch (Exception var10) {
                        CYLog.e(TAG, var10);
                    }
                }
            }
        }

        return jsonarr;
    }

    static String readDataFromFile(String fileName) {
        File fileData = new File(fileName);
        if (!fileData.exists()) {
            return "";
        } else {
            FileInputStream in = null;
            StringBuffer dataBuffer = new StringBuffer();
            ReentrantReadWriteLock rwl = getRwl();
            if (rwl.readLock().tryLock()) {
                rwl.readLock().lock();

                try {
                    in = new FileInputStream(fileData);
                    byte[] e = new byte[2048];

                    int readByte;
                    while ((readByte = in.read(e)) != -1) {
                        dataBuffer.append(new String(e, 0, readByte));
                    }
                } catch (Exception var15) {
                    CYLog.e(TAG, var15);
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException var14) {
                            CYLog.e(TAG, var14);
                        }
                    }

                    rwl.readLock().unlock();
                    fileData.delete();
                }
            }

            return dataBuffer.toString();
        }
    }

    public static synchronized String getSALT(Context context) {
        String file_name = context.getPackageName().replace(".", "");
        String sdCardRoot = Environment.getExternalStorageDirectory().getAbsolutePath();
        int apiLevel = Integer.parseInt(Build.VERSION.SDK);
        File fileFromSDCard = new File(sdCardRoot + File.separator, "." + file_name);
        File fileFromDData = new File(context.getFilesDir(), file_name);
        if (apiLevel >= 19) {
            sdCardRoot = context.getExternalFilesDir((String) null).getAbsolutePath();
            fileFromSDCard = new File(sdCardRoot, file_name);
        }

        String saltString = "";
        if (Environment.getExternalStorageState().equals("mounted")) {
            if (!fileFromSDCard.exists()) {
                String e = getSaltOnDataData(fileFromDData, file_name);

                try {
                    writeToFile(fileFromSDCard, e);
                } catch (Exception var9) {
                    CYLog.e(TAG, var9);
                }

                return e;
            } else {
                saltString = getSaltOnSDCard(fileFromSDCard);

                try {
                    writeToFile(fileFromDData, saltString);
                } catch (IOException var10) {
                    var10.printStackTrace();
                }

                return saltString;
            }
        } else {
            return getSaltOnDataData(fileFromDData, file_name);
        }
    }

    private static String getSaltOnSDCard(File fileFromSDCard) {
        try {
            return readSaltFromFile(fileFromSDCard);
        } catch (IOException var2) {
            CYLog.e(TAG, var2);
            return null;
        }
    }

    private static String getSaltOnDataData(File fileFromDData, String file_name) {
        try {
            if (!fileFromDData.exists()) {
                String e = getUUID();
                writeToFile(fileFromDData, e);
                return e;
            } else {
                return readSaltFromFile(fileFromDData);
            }
        } catch (IOException var3) {
            CYLog.e(TAG, var3);
            return "";
        }
    }

    private static String getUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private static String readSaltFromFile(File file) throws IOException {
        RandomAccessFile accessFile = new RandomAccessFile(file, "r");
        byte[] bs = new byte[(int) accessFile.length()];
        accessFile.readFully(bs);
        accessFile.close();
        return new String(bs);
    }

    private static void writeToFile(File file, String uuid) throws IOException {
        file.createNewFile();
        FileOutputStream out = new FileOutputStream(file);
        out.write(uuid.getBytes());
        out.close();
    }
}
