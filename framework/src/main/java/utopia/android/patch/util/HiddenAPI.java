package utopia.android.patch.util;

import android.app.ActivityThread;
import android.app.Application;
import android.app.LoadedApk;
import android.content.Context;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.os.UserHandle;

import java.lang.reflect.Field;

public final class HiddenAPI {
    private HiddenAPI() {
    }

    /**
     * Get the current package name.
     *
     * @return package name
     */
    public static String currentPackageName() {
        String packageName = ActivityThread.currentPackageName();
        if (packageName != null) {
            return packageName;
        }
        return "android";
    }

    /**
     * Get the current process name.
     *
     * @return process name
     */
    public static String currentProcessName() {
        String processName = ActivityThread.currentProcessName();
        if (processName != null) {
            return processName;
        }
        return "android";
    }

    /**
     * Get the current class loader.
     *
     * @return class loader
     */
    public static ClassLoader currentClassLoader() {
        ActivityThread activityThread = ActivityThread.currentActivityThread();
        if (activityThread != null) {
            String packageName = ActivityThread.currentPackageName();
            if (packageName != null) {
                LoadedApk loadedApk = activityThread.peekPackageInfo(packageName, true);
                if (loadedApk != null) {
                    return loadedApk.getClassLoader();
                }
            } else {
                Context mSystemContext = null;
                try {
                    Field field = ActivityThread.class.getDeclaredField("mSystemContext");
                    field.setAccessible(true);
                    mSystemContext = (Context) field.get(activityThread);
                } catch (ReflectiveOperationException ignore) {
                }
                if (mSystemContext != null) {
                    return mSystemContext.getClassLoader();
                }
            }
        }
        return ClassLoader.getSystemClassLoader();
    }

    /**
     * Get the current application.
     *
     * @return application
     */
    public static Application currentApplication() {
        return ActivityThread.currentApplication();
    }

    /**
     * Get package info without depending on context.
     *
     * @param packageName selected app
     * @return package info for the selected app
     */
    public static PackageInfo getPackageInfo(String packageName) {
        IPackageManager iPackageManager = ActivityThread.getPackageManager();
        if (iPackageManager != null) {
            return iPackageManager.getPackageInfo(packageName, 0, UserHandle.myUserId());
        }
        return null;
    }
}
