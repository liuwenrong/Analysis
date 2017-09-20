package com.coolyota.analysis.network;

import com.coolyota.analysis.CYAnalysis;
import com.coolyota.analysis.tools.CYLog;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * des: 自定义重试次数拦截器
 * @author liuwenrong@yotamobile.com
 * @version 1.0, 2017/9/18
 */
public class RetryInterceptor implements Interceptor{
    public int maxRetry;//最大重试次数
    public static int retryNum = 0;//假如设置为3次重试的话，则最大可能请求4次（默认1次+3次重试）

    public RetryInterceptor(int maxRetry) {
        this.maxRetry = maxRetry;
    }
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        CYLog.d(CYAnalysis.TAG, RetryInterceptor.class, "intercept: 27-----retryNum = " + retryNum);
        Response response = chain.proceed(request);
        while (!response.isSuccessful() && retryNum < maxRetry) {
            retryNum++;
            CYLog.d(CYAnalysis.TAG, RetryInterceptor.class, "intercept: 31-----retryNum = " + retryNum);
            response = chain.proceed(request);
        }
        return response;
    }
}
