package com.example.mytablet.ui.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.OkHttpClient;
import okhttp3.Interceptor;
import okhttp3.Request;

public class ApiClient {
    private static final String BASE_URL = "http://ccms.sczhiming.cn/prod-api/";
    private static Retrofit retrofit = null;
    private static String deviceSerialNumber = ""; // 设备序列号

    // 设置设备序列号（在应用启动时设置）
    public static void setDeviceSerialNumber(String serialNumber) {
        deviceSerialNumber = serialNumber;
        // **设备号变更时，重新创建 Retrofit 实例**
        retrofit = null;
    }

    public static Retrofit getClient() {
        if (retrofit == null) {
            // **请求头拦截器，自动添加 Authorization**
            Interceptor headerInterceptor = chain -> {
                Request original = chain.request();
                Request.Builder requestBuilder = original.newBuilder();

                // **全局添加 Authorization 头**
                if (deviceSerialNumber != null && !deviceSerialNumber.isEmpty()) {
                    requestBuilder.header("Authorization", deviceSerialNumber);
                }

                Request request = requestBuilder.build();
                return chain.proceed(request);
            };
            // **创建 OkHttpClient**
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(headerInterceptor) // 添加全局请求头
                    .addInterceptor(new LoggingInterceptor()) // **日志拦截器放在最后**
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit;
    }
}





