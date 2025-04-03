package com.yeild.restfulapi;

import androidx.annotation.CallSuper;

import com.yeild.restfulapi.errors.ApiException;
import com.yeild.restfulapi.utils.ResponseHelper;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import io.reactivex.CompletableObserver;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 *
 * @param <T> 返回数据类型， Void 表示无返回值
 */
public abstract class BaseApiObserver<T> implements Observer<T>, CompletableObserver {
    Disposable disposable;
    /**
     * 成功时调用
     * @param response
     */
    public abstract void onSuccess(T response);

    /**
     * 发生错误时调用
     * @param error
     * @return 是否已处理
     */
    public boolean onFailure(ApiException error) {
        return false;
    }

    /**
     * 在所有请求处理之前调用
     */
    public void onRequestStart() {

    }

    /**
     * 在所有事件处理完成后调用，不管是成功还是失败，总是在最后调用
     */
    @CallSuper
    public void onRequestEnd() {
        if (disposable != null && !disposable.isDisposed()) disposable.dispose();
    }

    @Override
    public void onSubscribe(Disposable d) {
        disposable = d;
        onRequestStart();
    }

    @Override
    public void onNext(T response) {
        try {
            onSuccess(response);
        } catch (Exception e) {
            onFailure(new ApiException(-1999, "客户端处理错误", e));
        }
    }

    @Override
    public void onError(Throwable error) {
        onFailure(ResponseHelper.handleError(error));
        onRequestEnd();
    }

    @Override
    public void onComplete() {
        // 检查泛型T的实际类型
        Type type = getClass().getGenericSuperclass();
        if(type instanceof ParameterizedType) {
            Type dataType = ((ParameterizedType) type).getActualTypeArguments()[0];
            if(dataType != null && !(dataType instanceof ParameterizedType) && ((Class<T>) dataType).getName().equals(Void.class.getName())) {
                onSuccess(null);
            }
        }
        onRequestEnd();
    }
}
