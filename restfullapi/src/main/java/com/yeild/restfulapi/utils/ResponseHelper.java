package com.yeild.restfulapi.utils;

import android.text.TextUtils;
import android.util.Log;

import com.yeild.restfulapi.RestfullApiManager;
import com.yeild.restfulapi.errors.ApiException;
import com.yeild.restfulapi.models.ApiResponse;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.HttpException;

public class ResponseHelper {

    public static ApiException handleError(Throwable error) {
        ApiException exception = null;
        do {
            if (error instanceof ApiException) {
                exception = (ApiException) error;
                break;
            }
            if (error instanceof UnknownHostException) {
                exception = new ApiException(-99999, "找不到服务器，请检查网络", error);
                break;
            }
            if (error instanceof ConnectException) {
                String message = error.getMessage();
                if (message.contains("Failed to connect to")) {
                    message = "连接服务器失败";
                }
                exception = new ApiException(-99999, message, error);
                break;
            }
            if (error instanceof SocketTimeoutException) {
                exception = new ApiException(-99999, "服务器请求超时", error);
                break;
            }
            if (error instanceof IllegalStateException) {
                String message = error.getMessage();
                if (message.matches(".*Expected.*?but.*")) {
                    message = "响应数据与指定的类型不符";
                }
                exception = new ApiException(-999, message, error);
                break;
            }
            if (error instanceof HttpException) {
                exception = httpException((HttpException) error);
                break;
            }
            exception = new ApiException(-999, error.getMessage(), error);
        } while (false);
        int log_level = Log.ERROR;
        switch (exception.error) {
            case 400:
                exception.reason = (exception.response == null
                        || TextUtils.isEmpty(exception.response.message())) ? "请求数据不符合要求"
                        :exception.response.message();
                log_level = Log.INFO;
                break;
        }
        return exception;
    }

    protected static ApiException httpException(HttpException error) {
        String message = error.getMessage();
        if (error.code() == 404) {
            return new ApiException(404, "请求地址错误", error);
        } else if (error.code() >= 400) {
            if (error.response() == null) {
                return new ApiException(500, message, error);
            }
            ResponseBody errorBody = error.response().errorBody();
            if (errorBody == null) {
                return new ApiException(500, message, error);
            }
            String errorBodyContent = errorBody.source().getBuffer().clone().readString(Charset.forName("UTF-8"));
            try {
                String url = error.response().raw().request().url().toString();
                RestfullApiManager apiManager = RestfullApiManager.queryByUrl(url);
                if(apiManager == null) throw new Exception("未找到RestfullApiManager: "+url);
                ApiResponse errorResponse = null;
                if (apiManager.converter != null) {
                    ResponseBody clone_body = ResponseBody.create(
                            MediaType.get(error.response().headers().get("Content-Type"))
                            , errorBody.source().inputStream().available()
                            , errorBody.source().getBuffer().clone());
                    errorResponse = (ApiResponse) apiManager.converter
                            .responseBodyConverter(ApiResponse.class, null, null)
                            .convert(clone_body);
                } else {
                    throw new Exception("未找到解析器");
                }
                if (errorResponse == null) throw new Exception("错误响应数据解析结果为NULL");
                if(errorResponse.code() == 0) errorResponse.setCode(500);
                else if(errorResponse.code() == 429) errorResponse.setMessage("请求过于频繁，请稍后再试");
                return new ApiException(error, errorResponse);
            } catch (Exception e) {
//                Logger.log(Log.ERROR, ResponseHelper.class.getName(), "API错误响应数据解析失败", e);
                try {
                    message = errorBodyContent;
                } catch (Exception e1) {
                    return new ApiException(500, "错误响应数据读取失败", error);
                }
            }
        }
        return new ApiException(500, message, error);
    }
}
