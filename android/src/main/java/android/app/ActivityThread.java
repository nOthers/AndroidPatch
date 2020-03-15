package android.app;

import android.content.pm.IPackageManager;

public class ActivityThread {
    public static ActivityThread currentActivityThread() {
        throw new RuntimeException("Stub!");
    }

    public static String currentPackageName() {
        throw new RuntimeException("Stub!");
    }

    public static String currentProcessName() {
        throw new RuntimeException("Stub!");
    }

    public static Application currentApplication() {
        throw new RuntimeException("Stub!");
    }

    public static IPackageManager getPackageManager() {
        throw new RuntimeException("Stub!");
    }

    public LoadedApk peekPackageInfo(String packageName, boolean includeCode) {
        throw new RuntimeException("Stub!");
    }
}
