package com.yeild.restfulapi.utils;

import androidx.lifecycle.LifecycleOwner;

import com.rxjava.rxlife.ObservableLife;
import com.rxjava.rxlife.RxLife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import rxhttp.wrapper.entity.Progress;
import rxhttp.wrapper.param.RxHttp;

public class RxHelper {
    public static ObservableLife<String> download(String url, String path, Consumer<Progress> progress, LifecycleOwner owner) {
        return RxHttp.get(url).addHeader("Accept-Encoding", "*")
                .asDownload(path, AndroidSchedulers.mainThread(), progress)
                .as(RxLife.as(owner));
    }

    public static <T> ObservableLife<T> get(String url, Class<T> cls, LifecycleOwner owner) {
        return RxHttp.get(url).asClass(cls).observeOn(AndroidSchedulers.mainThread()).as(RxLife.as(owner));
    }
}
