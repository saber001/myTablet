package com.yeild.restfulapi.adapter;

import androidx.annotation.Nullable;

import com.yeild.restfulapi.ResourceObservable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.CompositeException;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.plugins.RxJavaPlugins;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.HttpException;
import retrofit2.adapter.rxjava2.Result;

final class RxJavaLiveCallAdapter<R> implements CallAdapter<R, Object> {
    private final Type responseType;
    private final @Nullable
    Scheduler scheduler;
    private final boolean isAsync;
    private final boolean isResult;
    private final boolean isBody;
    private final boolean isFlowable;
    private final boolean isSingle;
    private final boolean isMaybe;
    private final boolean isCompletable;
    private final Annotation[] annotations;
    private final Retrofit retrofit;

    RxJavaLiveCallAdapter(Type responseType, @Nullable Scheduler scheduler, boolean isAsync,
                          boolean isResult, boolean isBody, boolean isFlowable, boolean isSingle, boolean isMaybe,
                          boolean isCompletable, Annotation[] annotations, Retrofit retrofit) {
        this.responseType = responseType;
        this.scheduler = scheduler;
        this.isAsync = isAsync;
        this.isResult = isResult;
        this.isBody = isBody;
        this.isFlowable = isFlowable;
        this.isSingle = isSingle;
        this.isMaybe = isMaybe;
        this.isCompletable = isCompletable;
        this.annotations = annotations;
        this.retrofit = retrofit;
    }

    @Override public Type responseType() {
        return responseType;
    }

    @Override public Object adapt(Call<R> call) {
        Observable<Response<R>> responseObservable = new ResourceObservable<>(call, annotations
                , retrofit, responseType);
        return RxJavaPlugins.onAssembly(responseObservable);
    }
}
