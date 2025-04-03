package com.yeild.restfulapi.annotation;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
@IntDef({ResourceLoadPolicy.NONE, ResourceLoadPolicy.LOCAL_ONLY, ResourceLoadPolicy.LOCAL_FIRST
        , ResourceLoadPolicy.MERGE, ResourceLoadPolicy.CUSTOM
})
public @interface ResourceLoadPolicy {
    /**
     * 使用默认策略（仅从远端接口加载）
     */
    int NONE = 0;
    /**
     * 仅加载本地数据
     */
    int LOCAL_ONLY = 1;
    /**
     * 优先加载本地数据，如果没有，则从远端接口加载
     */
    int LOCAL_FIRST = 2;
    /**
     * 先加载本地数据，再从接口加载，并保存远端接口加载的数据到本地
     */
    int MERGE = 3;
    /**
     * 自定义策略
     */
    int CUSTOM = 99;
}
