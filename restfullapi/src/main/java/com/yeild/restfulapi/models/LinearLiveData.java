package com.yeild.restfulapi.models;

import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import java.lang.ref.WeakReference;

import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * {@link LinearLiveData#postValue(Object value)}线性同步到主线程，保证不会丢失提交的<code>value</code>
 */
public class LinearLiveData<T> extends MediatorLiveData<T> {
    private static final class PostRunnable<T> implements Runnable {
        WeakReference<MutableLiveData<T>> source;
        T newValue;

        public PostRunnable(MutableLiveData<T> source, T newValue) {
            this.source = new WeakReference<>(source);
            this.newValue = newValue;
        }

        @Override
        public void run() {
            if (this.source.get() != null) this.source.get().setValue(newValue);
        }
    };

    @Override
    public void postValue(T value) {
        AndroidSchedulers.mainThread().scheduleDirect(new PostRunnable<>(this, value));
    }
}
