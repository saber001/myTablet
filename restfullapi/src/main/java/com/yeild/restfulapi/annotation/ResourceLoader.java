package com.yeild.restfulapi.annotation;

import com.yeild.restfulapi.IResourceLoader;
import com.yeild.restfulapi.utils.EmptyLoader;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ResourceLoader {
    /**
     * 本地资源加载策略 {@link com.yeild.restfulapi.annotation.ResourceLoadPolicy}
     */
    @ResourceLoadPolicy int policy() default ResourceLoadPolicy.NONE;

    /**
     * 指定本地资源加载器
     */
    Class<? extends IResourceLoader> loader() default EmptyLoader.class;
}
