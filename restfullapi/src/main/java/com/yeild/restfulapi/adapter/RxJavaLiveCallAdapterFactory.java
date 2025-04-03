package com.yeild.restfulapi.adapter;

import androidx.annotation.Nullable;

import com.yeild.restfulapi.ResourceObservable;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import retrofit2.CallAdapter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.Result;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

/**
 * Expand from {@link retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory}
 */
public class RxJavaLiveCallAdapterFactory extends CallAdapter.Factory {
    private RxJava2CallAdapterFactory factory;
    /**
     * Returns an instance which creates synchronous observables that do not operate on any scheduler
     * by default.
     */
    public static RxJavaLiveCallAdapterFactory create() {
        return new RxJavaLiveCallAdapterFactory(null, false);
    }

    /**
     * Returns an instance which creates asynchronous observables. Applying
     * {@link Observable#subscribeOn} has no effect on stream types created by this factory.
     */
    public static RxJavaLiveCallAdapterFactory createAsync() {
        return new RxJavaLiveCallAdapterFactory(null, true);
    }

    /**
     * Returns an instance which creates synchronous observables that
     * {@linkplain Observable#subscribeOn(Scheduler) subscribe on} {@code scheduler} by default.
     */
    @SuppressWarnings("ConstantConditions") // Guarding public API nullability.
    public static RxJavaLiveCallAdapterFactory createWithScheduler(Scheduler scheduler) {
        if (scheduler == null) throw new NullPointerException("scheduler == null");
        return new RxJavaLiveCallAdapterFactory(scheduler, false);
    }

    private final @Nullable
    Scheduler scheduler;
    private final boolean isAsync;

    private RxJavaLiveCallAdapterFactory(@Nullable Scheduler scheduler, boolean isAsync) {
        this.scheduler = scheduler;
        this.isAsync = isAsync;
        if (scheduler != null)
            this.factory = RxJava2CallAdapterFactory.createWithScheduler(scheduler);
        else if (isAsync)
            this.factory = RxJava2CallAdapterFactory.createAsync();
        else
            this.factory = RxJava2CallAdapterFactory.create();
    }

    @Override public @Nullable CallAdapter<?, ?> get(
            Type returnType, Annotation[] annotations, Retrofit retrofit) {
        Class<?> rawType = getRawType(returnType);
        if (rawType != ResourceObservable.class) {
            return this.factory.get(returnType, annotations, retrofit);
        }

        boolean isFlowable = rawType == Flowable.class;
        boolean isSingle = rawType == Single.class;
        boolean isMaybe = rawType == Maybe.class;

        boolean isResult = false;
        boolean isBody = false;
        Type responseType;
        if (!(returnType instanceof ParameterizedType)) {
            String name = isFlowable ? "Flowable"
                    : isSingle ? "Single"
                    : isMaybe ? "Maybe" : "Observable";
            throw new IllegalStateException(name + " return type must be parameterized"
                    + " as " + name + "<Foo> or " + name + "<? extends Foo>");
        }

        Type observableType = getParameterUpperBound(0, (ParameterizedType) returnType);
        Class<?> rawObservableType = getRawType(observableType);
        if (rawObservableType == Response.class) {
            if (!(observableType instanceof ParameterizedType)) {
                throw new IllegalStateException("Response must be parameterized"
                        + " as Response<Foo> or Response<? extends Foo>");
            }
            responseType = getParameterUpperBound(0, (ParameterizedType) observableType);
        } else if (rawObservableType == Result.class) {
            if (!(observableType instanceof ParameterizedType)) {
                throw new IllegalStateException("Result must be parameterized"
                        + " as Result<Foo> or Result<? extends Foo>");
            }
            responseType = getParameterUpperBound(0, (ParameterizedType) observableType);
            isResult = true;
        } else {
            responseType = observableType;
            isBody = true;
        }

        return new RxJavaLiveCallAdapter(responseType, scheduler, isAsync, isResult, isBody, isFlowable,
                isSingle, isMaybe, false, annotations, retrofit);
    }
}
