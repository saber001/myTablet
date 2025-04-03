package com.yeild.restfulapi;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;

import com.orhanobut.logger.Logger;
import com.yeild.restfulapi.annotation.ResourceLoadPolicy;
import com.yeild.restfulapi.annotation.ResourceLoader;
import com.yeild.restfulapi.models.Resource;
import com.yeild.restfulapi.models.LinearLiveData;
import com.yeild.restfulapi.utils.ContextHelper;
import com.yeild.restfulapi.utils.EmptyLoader;
import com.yeild.restfulapi.utils.ResponseHelper;

import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.functions.Action;
import io.reactivex.internal.disposables.DisposableHelper;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.Buffer;
import retrofit2.Call;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.HttpException;
import retrofit2.Retrofit;

public class ResourceObservable<ResponseType> extends Observable<Response<ResponseType>> {
    private final Type responseType;
    private final Call<ResponseType> originalCall;
    private WeakReference<MutableLiveData<Resource<ResponseType>>> result;
    private final Annotation[] annotations;
    private final Retrofit retrofit;
    private LinearLiveData<Resource<ResponseType>> inner;
    private SubscribeOnObserver<ResponseType> subscribeOnObserver;
    private Disposable disposable;

    public ResourceObservable(Call<ResponseType> originalCall, Annotation[] annotations
            , Retrofit retrofit, Type responseType) {
        this.originalCall = originalCall;
        this.annotations = annotations;
        this.retrofit = retrofit;
        this.responseType = responseType;
    }

    public ResourceObservable<ResponseType> observe(@NonNull LifecycleOwner owner, @NonNull androidx.lifecycle.Observer<Resource<ResponseType>> observer) {
        this.inner = new LinearLiveData<>();
        this.inner.observe(owner, observer);
        return observe(this.inner);
    }

    public ResourceObservable<ResponseType> observe(@NonNull LinearLiveData<Resource<ResponseType>> source) {
        this.result = new WeakReference<>(source);
        subscribeOnObserver = new SubscribeOnObserver<>();
        subscribeOnObserver.setDisposable(Schedulers.io().scheduleDirect(new SubscribeTask()));
        return this;
    }

    @Override
    protected void subscribeActual(Observer<? super Response<ResponseType>> observer) {
        Call<ResponseType> call = originalCall.clone();
        result(Resource.loading());

        IResourceLoader<ResponseType> resourceLoader = null;
        @ResourceLoadPolicy int loadPolicy = ResourceLoadPolicy.NONE;
        Object[] requestArgs = null;
        try{
            okhttp3.Request raw_request = call.request();
            ResourceLoader aResourceLoader = null;
            for (Annotation annotation : annotations) {
                if (annotation instanceof ResourceLoader) {
                    aResourceLoader = (ResourceLoader) annotation;
                    break;
                }
            }
            if (aResourceLoader != null) {
                loadPolicy = aResourceLoader.policy();
                Class<? extends IResourceLoader> loader_cls = aResourceLoader.loader();
                if (loader_cls != null && loader_cls != EmptyLoader.class)
                    resourceLoader = loader_cls.newInstance();
                if (resourceLoader == null) resourceLoader = new DefaultResourceLoader<>(
                        responseType, retrofit, annotations, raw_request
                );
            }
            if (loadPolicy != ResourceLoadPolicy.NONE) {
                ResponseType local_resp = null;
                try {
                    if (call.getClass().getSimpleName().equals("OkHttpCall")) {
                        Field field = call.getClass().getDeclaredField("args");
                        field.setAccessible(true);
                        requestArgs = (Object[]) field.get(call);
                    }
                    local_resp = resourceLoader.load(responseType, requestArgs);
                    if (local_resp != null) {
                        result(Resource.success(local_resp, requestArgs));
                    }
                } catch (Throwable t) {
                    Exceptions.throwIfFatal(t);
                    try {
                        result(Resource.errorLocal(ResponseHelper.handleError(t), requestArgs));
                    } catch (Throwable inner) {
                        Exceptions.throwIfFatal(inner);
                    }
                }
                boolean shouldFetchNet = false;
                switch (loadPolicy) {
                    case ResourceLoadPolicy.LOCAL_FIRST:
                        shouldFetchNet = local_resp == null;
                        break;
                    case ResourceLoadPolicy.MERGE:
                        shouldFetchNet = true;
                        break;
                    case ResourceLoadPolicy.CUSTOM:
                        shouldFetchNet = resourceLoader.shouldFetchRemote(local_resp);
                        break;
                }
                if (!shouldFetchNet) {
                    result(Resource.completeLocal(requestArgs));
                    return;
                }
            }
        } catch (Throwable t) {
            Exceptions.throwIfFatal(t);
            try {
                result(Resource.error(ResponseHelper.handleError(t), requestArgs));
            } catch (Throwable inner) {
                Exceptions.throwIfFatal(inner);
            }
        }

        ResponseType responseData = null;
        Response<ResponseType> response = null;
        try {
            response = call.execute();
            if (response.isSuccessful()) {
                responseData = response.body();
                result(Resource.success(responseData, requestArgs));
            } else {
                Throwable t = new HttpException(response);
                try {
                    result(Resource.error(ResponseHelper.handleError(t), requestArgs));
                } catch (Throwable inner) {
                    Exceptions.throwIfFatal(inner);
                }
            }
        } catch (Throwable t) {
            Exceptions.throwIfFatal(t);
            try {
                result(Resource.error(ResponseHelper.handleError(t), requestArgs));
            } catch (Throwable inner) {
                Exceptions.throwIfFatal(inner);
            }
        }
        try {
            result(Resource.complete(requestArgs));
            if (response != null && response.isSuccessful()
                    && loadPolicy != ResourceLoadPolicy.NONE) {
                try {
                    if (resourceLoader instanceof DefaultResourceLoader)
                        ((DefaultResourceLoader<ResponseType>) resourceLoader).response(response);
                    resourceLoader.save(response.code(), responseData);
                } catch (Exception e) {
                    Throwable t = new HttpException(response);
                    try {
                        result(Resource.error(ResponseHelper.handleError(t), requestArgs));
                    } catch (Throwable inner) {
                        Exceptions.throwIfFatal(inner);
                    }
                }
            }
        } catch (Throwable inner) {
            Exceptions.throwIfFatal(inner);
        }
        resourceLoader = null;
        observer.onComplete();
        this.inner = null;
        if (disposable != null && !disposable.isDisposed()) disposable.dispose();
        disposable = null;
        subscribeOnObserver.dispose();
    }

    private void result(Resource<ResponseType> resource) {
        if (this.inner != null) {
            this.inner.postValue(resource);
            return;
        }
        if (this.result == null || this.result.get() == null) return;
        try{ Thread.sleep(50); } catch (Exception ignored) {}
        if (this.result == null || this.result.get() == null) return;
        this.result.get().postValue(resource);
    }

    static final class SubscribeOnObserver<ResponseType> extends AtomicReference<Disposable> implements Observer<ResponseType>, Disposable {

        private static final long serialVersionUID = 8094547886072529208L;
        final AtomicReference<Disposable> upstream;

        SubscribeOnObserver() {
            this.upstream = new AtomicReference<Disposable>();
        }

        @Override
        public void onSubscribe(Disposable d) {
            DisposableHelper.setOnce(this.upstream, d);
        }

        @Override
        public void onNext(ResponseType t) {
        }

        @Override
        public void onError(Throwable t) {
        }

        @Override
        public void onComplete() {
        }

        @Override
        public void dispose() {
            DisposableHelper.dispose(upstream);
            DisposableHelper.dispose(this);
        }

        @Override
        public boolean isDisposed() {
            return DisposableHelper.isDisposed(get());
        }

        void setDisposable(Disposable d) {
            DisposableHelper.setOnce(this, d);
        }
    }

    final class SubscribeTask implements Runnable {

        SubscribeTask() {
        }

        @Override
        public void run() {
            try {
                Thread.sleep(100);
            } catch (Exception ignored) {}
            disposable = subscribe();
        }
    }

    static final class DefaultResourceLoader<ResponseType> implements IResourceLoader<ResponseType> {
        private static final Charset CHARSET = Charset.forName("UTF-8");
        private final Type responseType;
        private final Retrofit retrofit;
        private final Annotation[] annotations;
        private final okhttp3.Request request;
        private Response<ResponseType> raw_response;

        public DefaultResourceLoader(Type responseType, Retrofit retrofit, Annotation[] annotations
                , Request request) {
            this.responseType = responseType;
            this.retrofit = retrofit;
            this.annotations = annotations;
            this.request = request;
        }

        public DefaultResourceLoader<ResponseType> response(Response<ResponseType> raw_response) {
            this.raw_response = raw_response;
            return this;
        }

        @Override
        public ResponseType load(Type responseType, Object[] args) throws Exception {
            Context context = ContextHelper.getContext();
            if (context == null) throw new Exception("获取应用Context失败");
            SharedPreferences preferences = context.getSharedPreferences("retrofit", Context.MODE_PRIVATE);
            String key = urlKey(request.url().toString());
            String cached = preferences.getString(key, null);
            if (TextUtils.isEmpty(cached)) return null;
            Converter<ResponseBody, ResponseType> converter = retrofit
                    .responseBodyConverter(responseType, annotations);
            MediaType contentType = MediaType.parse(
                    preferences.getString(key+"_contenttype", "application/json"));
            contentType.charset(CHARSET);
            return converter.convert(
                    ResponseBody.create(contentType, cached)
            );
        }

        @Override
        public boolean shouldFetchRemote(ResponseType loaded) {
            return loaded == null;
        }

        @Override
        public void save(int code, ResponseType response) throws Exception {
            if (response == null) return;
            Context context = ContextHelper.getContext();
            if (context == null) throw new Exception("获取应用Context失败");
            String url = raw_response.raw().request().url().toString();
            RequestBody requestBody = retrofit.requestBodyConverter(responseType
                    , new Annotation[1], annotations).convert(response);
            Buffer buffer = new Buffer();
            requestBody.writeTo(buffer);
            MediaType contentType = raw_response.raw().body() == null ? null:raw_response.raw().body().contentType();
            if (contentType == null) contentType = MediaType.parse("application/json");
            String raw_body = buffer.readString(CHARSET);
            String key = urlKey(url);
            SharedPreferences preferences = context.getSharedPreferences("retrofit", Context.MODE_PRIVATE);
            SharedPreferences.Editor sp_edit = preferences.edit();
            sp_edit.putString(key, raw_body);
            sp_edit.putString(key+"_contenttype", contentType.toString());
            sp_edit.apply();
        }

        private String urlKey(String url) {
            try {
                return URLEncoder.encode(url, CHARSET.name());
            } catch (UnsupportedEncodingException e) {
                return url;
            }
        }
    }
}
