package com.yeild.restfulapi.models;

import com.yeild.restfulapi.errors.ApiException;

import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;

public class Resource<ResponseType> {
    public enum Status {
        LOADING, SUCCEED, ERROR, COMPLETE
    }

    @NonNull
    public final Status status;
    @Nullable
    public final ResponseType data;
    @Nullable
    public final String message;
    @Nullable
    public final ApiException exception;
    public boolean isLocal = false;
    /** 资源请求参数列表 */
    public Object[] requestArgs = null;

    private Resource(@Nullable ApiException exception, Object[] requestArgs) {
        this(Status.ERROR, null, exception.reason, requestArgs, exception);
    }

    private Resource(@NonNull Status status, @Nullable ResponseType data, @Nullable String message
            , Object[] requestArgs) {
        this(status, data, message, requestArgs, null);
    }

    private Resource(@NonNull Status status, @Nullable ResponseType data
            , @Nullable String message, Object[] requestArgs, @Nullable ApiException exception) {
        this.status = status;
        this.data = data;
        this.message = message;
        this.requestArgs = requestArgs;
        this.exception = exception;
    }

    public Resource<ResponseType> local() {
        this.isLocal = true;
        return this;
    }

    public static <ResponseType> Resource<ResponseType> loading() {
        return loading(null);
    }

    public static <ResponseType> Resource<ResponseType> loading(@Nullable ResponseType data) {
        return loading(data, null);
    }

    public static <ResponseType> Resource<ResponseType> loading(@Nullable ResponseType data, String msg) {
        return new Resource<ResponseType>(Status.LOADING, data, msg, null);
    }

    public static <ResponseType> Resource<ResponseType> success(@NonNull ResponseType data, Object[] requestArgs) {
        return success(data, null, requestArgs);
    }

    public static <ResponseType> Resource<ResponseType> success(@NonNull ResponseType data, String msg, Object[] requestArgs) {
        return new Resource<>(Status.SUCCEED, data, msg, requestArgs);
    }

    public static <ResponseType> Resource<ResponseType> error(ApiException exception, Object[] requestArgs) {
        return new Resource<>(exception, requestArgs);
    }

    public static <ResponseType> Resource<ResponseType> errorLocal(ApiException exception, Object[] requestArgs) {
        return new Resource<ResponseType>(exception, requestArgs).local();
    }

    public static <ResponseType> Resource<ResponseType> error(String msg
            , @Nullable ResponseType data, Object[] requestArgs
            , @Nullable ApiException exception) {
        return new Resource<>(Status.ERROR, data, msg, requestArgs, exception);
    }

    public static <ResponseType> Resource<ResponseType> complete(Object[] requestArgs) {
        return new Resource<>(Status.COMPLETE, null, null, requestArgs);
    }

    public static <ResponseType> Resource<ResponseType> completeLocal(Object[] requestArgs) {
        return new Resource<ResponseType>(Status.COMPLETE, null, null, requestArgs).local();
    }

    public static <ResponseType> Resource<ResponseType> complete(@Nullable ResponseType data, String msg, Object[] requestArgs) {
        return new Resource<>(Status.COMPLETE, data, msg, requestArgs);
    }
}
