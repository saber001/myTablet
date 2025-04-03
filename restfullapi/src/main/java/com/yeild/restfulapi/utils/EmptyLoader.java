package com.yeild.restfulapi.utils;

import com.yeild.restfulapi.IResourceLoader;

import java.lang.reflect.Type;

public class EmptyLoader implements IResourceLoader<Object> {

    @Override
    public Object load(Type responseType, Object[] args) throws Exception {
        return null;
    }

    @Override
    public boolean shouldFetchRemote(Object loaded) {
        return true;
    }

    @Override
    public void save(int code, Object t) throws Exception {

    }
}
