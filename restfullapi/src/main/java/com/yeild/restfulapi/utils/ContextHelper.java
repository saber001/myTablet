package com.yeild.restfulapi.utils;

import android.content.ContextWrapper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ContextHelper {
    public static ContextWrapper getContext() {
        try {
            Class<?> c = Class.forName("android.app.AppGlobals");
            Method method = c.getDeclaredMethod("getInitialApplication");
            return (ContextWrapper)method.invoke(c);
        } catch (ClassNotFoundException e) {
            //e.printStackTrace();
        } catch (NoSuchMethodException e) {
            //e.printStackTrace();
        } catch (IllegalAccessException e) {
            //e.printStackTrace();
        } catch (InvocationTargetException e) {
            //e.printStackTrace();
        }
        return null;
    }
}
