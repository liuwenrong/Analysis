package com.coolyota.analysis.tools;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

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

import static java.lang.String.valueOf;

/**
 * des:
 *
 * @author liuwenrong
 * @version 1.0, 2017/6/16
 */
public class UploadFileUtil {

    private static final String TAG = "UploadFileUtil";
    private static final int TIME_OUT = 10 * 1000; //超时时间
    private static final String CHARSET = "utf-8"; //设置编码
    private static final String PREFIX = "--";
    private static final String LINE_END = "\n";
    static OkHttpClient mOkHttpClient = new OkHttpClient();
    private static Handler mHandler = new Handler(Looper.getMainLooper());

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

        if ( !CYConstants.uploadEnabled ) {
            CYLog.w(TAG, UploadFileUtil.class, "uploadEnabled is false, can't upload !");
            return;
        }

        if (TextUtils.isEmpty(DeviceInfo.getDeviceIMEI())) { //拿不到IMEI 直接不传,否则也会提示token
            CYLog.e(TAG, UploadFileUtil.class, "please call CYAnalysis.init(context, appKey) before uploadFile! ");
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
        CYLog.i(TAG, UploadFileUtil.class, "uploadFile: urlMD5 = " + urlMD5);
        Request request = new Request.Builder().url(urlMD5).post(requestBody.build()).build();
        // readTimeout("请求超时时间" , 时间单位);
        client.newBuilder().readTimeout(500, TimeUnit.SECONDS).build().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
//                uploadFailure(call, e);
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
//                    uploadSuccessToUiThread(call, response);

                } else {
//                    uploadNotSuccess(call, response);
                    message.setSuccess(false);
                    message.setMsg(str);
                    callbackMessage.callbackMsg(message);
                }
            }
        });


    }

    public interface CallbackMessage{
        void callbackMsg(MyMessage message);
    }

    private static boolean isJson(String strForValidating) {
        try {
            JSONObject jsonObject = new JSONObject(strForValidating);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static <T> void uploadSuccessToUiThread(final Call call, final Response response, final ReqProgressCallBack<T> callBackToService){
        // 发送到主线程
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    String str = response.body().string();
                    Log.i(TAG, response.message() + " , body " + str);
                    callBackToService.onSuccessInUiThread();
                } catch (IOException e) {

                }
            }
        });
    }

    public static <T> void uploadNotSuccess(final Call call, final Response response , final ReqProgressCallBack<T> callBackToService) {
        // 发送到主线程
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    callBackToService.onFail();
                    String string = response.body().string();
                    Log.i(TAG, response.message() + " error : body " + string);
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
                Log.i(TAG, "onFailure: call = " + call.toString() + ",e = " + e);

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

}
