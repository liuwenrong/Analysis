package com.coolyota.analysis.tools;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;

import com.coolyota.analysis.CYAnalysis;
import com.coolyota.analysis.network.RetryInterceptor;
import com.coolyota.analysis.network.TestInterceptor;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.coolyota.analysis.tools.SharedPrefUtil.LAST_UPDATE_TIME;
import static java.lang.String.valueOf;

/**
 * des:
 *
 * @author liuwenrong
 * @version 1.0, 2017/6/16
 */
public class UploadFileUtil {

    public static final int TIMEOUT_SECONDS = 50;//单位秒s
    private static final int TIME_OUT = 10 * 1000; //超时时间
    private static final String CHARSET = "utf-8"; //设置编码
    private static final String PREFIX = "--";
    private static final String LINE_END = "\n";
    static OkHttpClient mOkHttpClient = new OkHttpClient();
    //    检测用户体验开关状态的接口查找 EXPERIENCE_FILE_NAME 这个文件是否存在
    static String EXPERIENCE_FILE_NAME = "/data/junk-server/UserExperiencePlan";
    private static Handler mHandler = new Handler(Looper.getMainLooper());
    public static final String USER_PLAN_DB = "user_plan";//数据库读取用户体验开关的字段

    /**
     * 判断是否需要上传, WiFi情况下(刚连上WiFi时,没法NetWorkInfo = null),所以先不判断,与上一次时间相差6小时,且打开了用户体验开关
     */
    public static boolean UploadIfNeed(Context context) {

        return isMoreThan6Hours(context) && isExperienceOn(context);

    }

    /**
     * 距离上一次上传是否超过6小时
     * @param context
     * @return
     */
    public static boolean isMoreThan6Hours(Context context) {

        SharedPrefUtil mSharedPreferences = new SharedPrefUtil(context);
        long lastUpdateTime = mSharedPreferences.getValue(LAST_UPDATE_TIME, System.currentTimeMillis() - CommonUtil.ONE_DAY);

        boolean flag = (System.currentTimeMillis() - lastUpdateTime) > CommonUtil.SIX_HOURS;

        if (!flag) {
            CYLog.d(CYAnalysis.TAG, UploadFileUtil.class, "isMoreThan6Hours: The last upload interval is less than 6 hours, can't upload");
        }

        return flag;
    }

    /**
     * 用户体验开关是否打开
     * @param ctx
     * @return
     */
    public static boolean isExperienceOn(Context ctx) {

        boolean isExperienceOn;

        // 针对SystemUI 或者非系统应用 读取系统设置 TODO
        if (isSystemApp(ctx)) {
            File file = new File(EXPERIENCE_FILE_NAME); //只有系统应用即sharedUserId=system 才有权限读取该应用
            isExperienceOn = file.exists();
        } else {
            //读数据库
            int userPlan = Settings.System.getInt(ctx.getContentResolver(), USER_PLAN_DB, CYConstants.defaultUserPlan);
            isExperienceOn = (userPlan == CYConstants.UserPlanOn);
        }

        if (!isExperienceOn) {
            CYLog.d(CYAnalysis.TAG, UploadFileUtil.class, "isExperienceOn: User Experience Toggle is Off, can't upload");
        }

        return isExperienceOn;
    }

    public static boolean isSystemApp(Context ctx) {

//        通过uid来过滤系统系统的安装包,因为android系统中的uid从 1000 ～ 9999 都是给系统程序保留的，所以只要判断package的uid > 10000即可判定该程序是非系统程序。
        //其中SystemUI的uid测试是10026
        ApplicationInfo appInfo = ctx.getApplicationInfo();
        int uid = appInfo.uid;
        boolean flag = false;

        if (uid < 9999 && !"com.android.systemui".equals(appInfo.packageName)) {
            flag = true;
        }

        return flag;

    }


    public static void uploadFile(File uploadFile, final CallbackMessage callbackMessage) {
        final HashMap<String, Object> params = new HashMap<>();
        params.put(ApiConstants.PARAM_LOG_TYPE, ApiConstants.LOG_TYPE_ANALYSIS);
        params.put(ApiConstants.PARAM_PRO_TYPE, DeviceInfo.getProType());
        params.put(ApiConstants.PARAM_SYS_VERSION, DeviceInfo.getSysVersion());
        params.put(ApiConstants.PARAM_UP_TYPE, ApiConstants.UpType.TYPE_OTHER_APP);
        params.put(ApiConstants.PARAM_UP_DESC, "");
        params.put(ApiConstants.PARAM_PHONE, "");
        UploadFileUtil.uploadFile(ApiConstants.PATH_UPLOAD, params, uploadFile, callbackMessage);
    }


    public static void uploadFile(final String url, final Map<String, Object> params, File file, final CallbackMessage callbackMessage) {

        if (!CYConstants.uploadEnabled) {
            CYLog.w(CYAnalysis.TAG, UploadFileUtil.class, "uploadEnabled is false, can't upload !");
            return;
        }

        if (TextUtils.isEmpty(DeviceInfo.getDeviceIMEI())) { //拿不到IMEI 直接不传,否则也会提示token
            CYLog.e(CYAnalysis.TAG, UploadFileUtil.class, "please call CYAnalysis.init(context, appKey) before uploadFile! ");
            return;
        }

        final MyMessage message = new MyMessage();

        OkHttpClient client = new OkHttpClient();
        // form 表单形式上传
        MultipartBody.Builder requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM);
        if (file != null) {
            // MediaType.parse() 里面是上传的文件类型。
            RequestBody body = RequestBody.create(null, file);
            // 参数分别为， 请求key ，文件名称 ， RequestBody
            requestBody.addFormDataPart("file", file.getName(), body);
        }

        // 生成带token的url,并在params中加入3个参数
        String urlMD5 = UrlBuilder.decorateCommonParams(url, null, params);

        if (params != null) {
            // map 里面是请求中所需要的 key 和 value
            for (Map.Entry entry : params.entrySet()) {
                requestBody.addFormDataPart(valueOf(entry.getKey()), valueOf(entry.getValue())).setType(MultipartBody.MIXED);
            }
        }
        CYLog.i(CYAnalysis.TAG, UploadFileUtil.class, "uploadFile: urlMD5 = " + urlMD5);
        Request request = new Request.Builder().url(urlMD5).post(requestBody.build()).build();
        // readTimeout("请求超时时间" , 时间单位);
        client.newBuilder()
                .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .addInterceptor(new RetryInterceptor(3))
                .build()
                .newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        message.setSuccess(false);
                        message.setMsg(e.toString());
                        callbackMessage.callbackMsg(message);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String str = response.body().string();
                        if (response.isSuccessful()) {
                            message.setSuccess(isJson(str));
                            message.setMsg(str);
                            callbackMessage.callbackMsg(message);

                        } else {
                            message.setSuccess(false);
                            message.setMsg(str);
                            callbackMessage.callbackMsg(message);
                        }
                    }
                })
        ;
/*        try {
            testRequest(client, url);
        } catch (IOException e) {
            e.printStackTrace();
        }*/

    }

    /**
     * 测试网络请求
     * @param mClient
     * @param url
     * @throws IOException
     */
    public static void testRequest(OkHttpClient mClient, String url) throws IOException {
        Request request = new Request.Builder().url(url).build();

        mClient.newBuilder()
                .addInterceptor(new RetryInterceptor(3))
                .addInterceptor(new TestInterceptor())
                .build()
                .newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        CYLog.d(CYAnalysis.TAG, UploadFileUtil.class, "onFailure: -------205-----");
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        CYLog.d(CYAnalysis.TAG, UploadFileUtil.class, "onResponse: code = " + response.code() + ", message = " + response.message());
                        CYLog.d(CYAnalysis.TAG, UploadFileUtil.class, "onResponse: " + response.body().string());
                    }
                });
//            Response response = mClient.newCall(request).execute();
    }

    private static boolean isJson(String strForValidating) {
        try {
            JSONObject jsonObject = new JSONObject(strForValidating);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static <T> void uploadSuccessToUiThread(final Call call, final Response response, final ReqProgressCallBack<T> callBackToService) {
        // 发送到主线程
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    String str = response.body().string();
                    CYLog.d(CYAnalysis.TAG, UploadFileUtil.class, "run: " + response.message() + " , body " + str);
                    callBackToService.onSuccessInUiThread();
                } catch (IOException e) {

                }
            }
        });
    }

    public static <T> void uploadNotSuccess(final Call call, final Response response, final ReqProgressCallBack<T> callBackToService) {
        // 发送到主线程
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    callBackToService.onFail();
                    String string = response.body().string();
                    CYLog.d(CYAnalysis.TAG, UploadFileUtil.class, "run: " + response.message() + " error : body " + string);
                } catch (IOException e) {

                }
            }
        });
    }

    public static <T> void uploadFailure(final Call call, final IOException e, final ReqProgressCallBack<T> callBackToService) {
        // 发送到主线程
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                callBackToService.onFail();
                CYLog.d(CYAnalysis.TAG, UploadFileUtil.class, "run: " + "onFailure: call = " + call.toString() + ",e = " + e);

            }
        });
    }

    public static void progressCallBack(final long total, final long current, final long networkSpeed, final ReqProgressCallBack callBack) {
        // 发送到主线程
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                callBack.onProgressInUIThread(total, current, current * 1.0f / total, networkSpeed);

            }
        });
    }

    public static void uploadFileHttpClient(final String url, final Map<String, Object> params, File file) {

        // 生成带token的url,并在params中加入3个参数
        String urlMD5 = UrlBuilder.decorateCommonParams(url, null, params);

//        MultipartEntityBuilder entity = MultipartEntityBuilder.create();

        /*String imei = "emte-elts-e36f-rldf";
        entity.addPart("imei", new StringBody(imei, ContentType.TEXT_PLAIN));
        String key = "7576E9DD910227F0D1B297FC05D90BE7";
        entity.addPart("key", new StringBody(key, ContentType.TEXT_PLAIN));
        entity.addPart("timestamp", new StringBody(""+System.currentTimeMillis(), ContentType.TEXT_PLAIN));*/
/*        entity.addPart("logType", new StringBody("20013", ContentType.TEXT_PLAIN));
        entity.addPart("proType", new StringBody("YOTA Y3", ContentType.TEXT_PLAIN));
        entity.addPart("sysVersion", new StringBody("v2017.06.2_release", ContentType.TEXT_PLAIN));
        entity.addPart("upType", new StringBody("20007", ContentType.TEXT_PLAIN));*/

//        if (params != null) {
//            // map 里面是请求中所需要的 key 和 value
//            for (Map.Entry entry : params.entrySet()) {
//                entity.addPart(valueOf(entry.getKey()), new StringBody( String.valueOf(entry.getValue()), ContentType.TEXT_PLAIN));
//            }
//        }
//        entity.addPart("file", new FileBody(file));

        /*String token = MD5Util.md5(key+imei+System.currentTimeMillis());
        String url = "http://127.0.0.1:16001/dcss-collector/log/upload?token="+token;
        String url = "http://test.dcss.baoliyota.com/dcss-collector/log/upload?token="+token;
        String url = "http://172.16.7.29:16001/dcss-collector/log/upload?token="+token;*/

//        HttpPost request = new HttpPost(urlMD5);
//        request.setEntity(entity.build());
//
//        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
//        try {
//            httpClientBuilder.build().execute(request);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

    }


    public interface CallbackMessage {
        void callbackMsg(MyMessage message);
    }

    public interface ReqProgressCallBack<T> extends ReqCallBack<T> {
        /**
         * 响应进度更新 在UI线程
         */
        void onProgressInUIThread(long total, long current, float progress, long networkSpeed);
    }

    public interface ReqCallBack<T> {
        void onSuccessInUiThread();

        void onFail();
    }

    public interface FileUploadListener {
        public void onProgress(long pro, double percent);

        public void onFinish(int code, String res, Map<String, List<String>> headers);
    }

}
