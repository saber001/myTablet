package com.yeild.restfulapi;

import java.lang.reflect.Type;

public interface IResourceLoader<Response> {
    /**
     * 从本地加载数据
     * @param args 接口的参数列表
     * @return 加载的数据
     */
    Response load(Type responseType, Object[] args) throws Exception;

    /**
     * 指定加载策略为{@link com.yeild.restfulapi.annotation.ResourceLoadPolicy#CUSTOM}时调用
     * @param loaded 从本地加载的数据
     * @return true：继续从远端接口加载
     */
    boolean shouldFetchRemote(Response loaded);

    /**
     * 保存远端接口加载的数据
     */
    void save(int code, Response response) throws Exception;
}
