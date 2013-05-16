package org.catacombae.simpletimer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MacUtils {
    public static boolean isMac() {
        return System.getProperty("os.name").toLowerCase().startsWith(
                "mac os x");
    }

    public static void requestUserAttention(boolean critical) {
        Class applicationClass;
        try {
            applicationClass = Class.forName("com.apple.eawt.Application");
        } catch(ClassNotFoundException e) {
            throw new RuntimeException("Unable to locate class " +
                    "com.apple.eawt.Application.", e);
        }

        Method getApplicationMethod;
        try {
            getApplicationMethod = applicationClass.getMethod("getApplication");
        } catch(NoSuchMethodException e) {
            throw new RuntimeException("Unable to locate method " +
                    "com.apple.eawt.Application.getApplication(void).", e);
        }

        Object applicationObject;
        try {
            applicationObject = getApplicationMethod.invoke(null);
        } catch(IllegalAccessException e) {
            throw new RuntimeException("Illegal access while attempting to " +
                    "invoke method " +
                    "com.apple.eawt.Application.getApplication(void).", e);
        } catch(IllegalArgumentException e) {
            throw new RuntimeException("Illegal argument while attempting to " +
                    "invoke method " +
                    "com.apple.eawt.Application.getApplication(void).", e);
        } catch(InvocationTargetException e) {
            throw new RuntimeException("Method " +
                    "com.apple.eawt.Application.getApplication(void) threw " +
                    "an exception.", e);
        }

        Method requestUserAttentionMethod;
        try {
            requestUserAttentionMethod = applicationClass.getMethod(
                    "requestUserAttention", boolean.class);
        } catch(NoSuchMethodException e) {
            throw new RuntimeException("Unable to locate method " +
                    "com.apple.eawt.Application.requestUserAttention(boolean).",
                    e);
        }

        try {
            requestUserAttentionMethod.invoke(applicationObject, critical);
        } catch(IllegalAccessException e) {
            throw new RuntimeException("Illegal access while attempting to " +
                    "invoke method " +
                    "com.apple.eawt.Application.requestUserAttention(boolean).",
                    e);
        } catch(IllegalArgumentException e) {
            throw new RuntimeException("Illegal argument while attempting to " +
                    "invoke method " +
                    "com.apple.eawt.Application.requestUserAttention(boolean).",
                    e);
        } catch(InvocationTargetException e) {
            throw new RuntimeException("Method " +
                    "com.apple.eawt.Application.requestUserAttention(boolean)" +
                    " threw an exception.", e);
        }
    }
}
