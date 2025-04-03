package com.yeild.restfulapi.http;

import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;

import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    private HashMap<String, String> headers = new HashMap<>();
    private String auth;

    public AuthInterceptor(Context context) {
        int verCode = -1;
        try {
            verCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("AuthInterceptor", e.getMessage(), e);
        }
        headers.put("APP-VERSION", String.valueOf(verCode));
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();

        Request.Builder requestBuilder = original.newBuilder();
        if(!TextUtils.isEmpty(auth))
            requestBuilder.header("Authorization", auth);
//        for(String key : headers.keySet()) {
//            requestBuilder.addHeader(key,headers.get(key));
//        }
        Request request = requestBuilder.build();
        Response response = chain.proceed(request);
        return response;
    }

    public void authNone() {
        auth = null;
    }

    public void authToken(String token) {
        auth = "Bearer " + token;
    }

    public void authBasic(String username, String password) {
        authBasic(username, password, null);
    }

    public void authBasic(String username, String password, Charset charset) {
        if(charset == null)
            auth = Credentials.basic(username, password);
        else
            auth = Credentials.basic(username, password, charset);
    }

}
