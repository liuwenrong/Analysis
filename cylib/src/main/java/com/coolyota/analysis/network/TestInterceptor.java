package com.coolyota.analysis.network;

import com.coolyota.analysis.CYAnalysis;
import com.coolyota.analysis.tools.CYLog;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * des: Ok拦截器,模拟返回测试成功或者失败的response
 *
 * @author liuwenrong@yotamobile.com
 * @version 1.0, 2017/9/18
 */
public class TestInterceptor implements Interceptor {

    public static String Test_Url = "https://www.baidu.com/";
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        String url = request.url().toString();
        CYLog.d(CYAnalysis.TAG, TestInterceptor.class, "intercept: url = " + url);
        Response response = null;
        if (url.equals(Test_Url)) {
            String responseString = "{\"message\":\"我是模拟的数据\"}";//模拟的错误的返回值
            int code = 400;
            String message = "请求失败";

            if (RetryInterceptor.retryNum == 2) { //重试第2次,模拟成功
                code = 200;
                message = "请求成功";
            }

            response = new Response.Builder()
                    .code(code)
                    .request(request)
                    .protocol(Protocol.HTTP_1_0)
                    .body(ResponseBody.create(MediaType.parse("application/json"), responseString.getBytes()))
                    .addHeader("content-type", "application/json")
                    .message(message)
                    .build();
        } else {
            response = chain.proceed(request);
        }
        return response;

    }
}
