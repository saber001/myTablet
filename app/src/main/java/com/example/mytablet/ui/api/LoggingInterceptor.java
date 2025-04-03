package com.example.mytablet.ui.api;

import android.util.Log;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;

public class LoggingInterceptor implements Interceptor {
    private static final String TAG = "API_LOG";

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        // **获取请求信息**
        String url = request.url().toString();
        String method = request.method();
        String headers = request.headers().toString();
        String requestBody = bodyToString(request.body());

        // **手动获取 Authorization 头**
        String authorizationHeader = request.header("Authorization");
        if (authorizationHeader == null || authorizationHeader.isEmpty()) {
            authorizationHeader = "未设置";
        }
        Log.d(TAG, "========== 网络请求开始 ==========");
        Log.d(TAG, "请求 URL: " + url);
        Log.d(TAG, "请求方法: " + method);
        Log.d(TAG, "请求头: " + headers);
        Log.d(TAG, "Authorization 头部: " + authorizationHeader); // 这里强制打印
        Log.d(TAG, "请求体: " + requestBody);

        // **发送请求并获取响应**
        long startTime = System.nanoTime();
        Response response = chain.proceed(request);
        long endTime = System.nanoTime();

        // **读取响应体**
        ResponseBody responseBody = response.body();
        String responseBodyString = responseBody != null ? responseBody.string() : "null";
        MediaType contentType = responseBody != null ? responseBody.contentType() : null;

        Log.d(TAG, "响应状态码: " + response.code());
        Log.d(TAG, "响应时间: " + ((endTime - startTime) / 1e6) + " ms");
        Log.d(TAG, "响应体: " + responseBodyString);
        Log.d(TAG, "========== 网络请求结束 ==========");

        // **重新构建 Response 以确保 body 可重复读取**
        return response.newBuilder()
                .body(ResponseBody.create(contentType,responseBodyString))
                .build();
    }

    private String bodyToString(RequestBody requestBody) {
        try {
            if (requestBody == null) return "无请求体";
            Buffer buffer = new Buffer();
            requestBody.writeTo(buffer);
            return buffer.readUtf8();
        } catch (IOException e) {
            return "无法读取请求体";
        }
    }
}


