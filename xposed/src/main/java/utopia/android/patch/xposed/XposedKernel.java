package utopia.android.patch.xposed;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.util.LinkedList;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dalvik.system.PathClassLoader;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import utopia.android.patch.lang.ExternalKernel;
import utopia.android.patch.util.HiddenAPI;

public class XposedKernel extends ExternalKernel implements IXposedHookLoadPackage {
    public XposedKernel() {
        super("Xposed");
        initPatchLoader(XposedKernel.class.getClassLoader());
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.isFirstApplication) {
            return;
        }
        if (Debugger.isDebug() && Debugger.reloadIfNeeded(lpparam)) {
            return;
        }
        initPatch();
    }

    @Override
    protected MemberStruct hookMember(Member member) throws Throwable {
        final MemberStruct struct = new MemberStruct(member);
        XposedBridge.hookMethod(member, new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                return replaceHookedMember(struct, param.thisObject, param.args);
            }
        });
        return struct;
    }

    @Override
    protected Object invokeOriginalMember(MemberStruct struct, Object object, Object[] objects) throws Throwable {
        try {
            return XposedBridge.invokeOriginalMethod(struct.getMember(), object, objects);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    @Override
    protected void wtf(Throwable t) {
        XposedBridge.log(t);
        Looper looper = Looper.getMainLooper();
        if (looper == null) {
            return;
        }
        final String text = String.valueOf(t);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Context context = HiddenAPI.currentApplication();
                if (context == null) {
                    return;
                }
                showToast(context, text);
            }
        };
        if (Thread.currentThread() != looper.getThread() || HiddenAPI.currentApplication() == null) {
            new Handler(looper).post(runnable);
        } else {
            runnable.run();
        }
    }

    static void showToast(Context context, String text) {
        Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
        Queue<View> views = new LinkedList<>();
        views.add(toast.getView());
        while (views.size() > 0) {
            View view = views.remove();
            if (view instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) view;
                for (int i = 0; i < viewGroup.getChildCount(); i++) {
                    views.add(viewGroup.getChildAt(i));
                }
            }
            Drawable background = view.getBackground();
            if (background != null) {
                background.setColorFilter(new ColorMatrixColorFilter(new float[]{
                        0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0,
                        0, 0, 0, 1f, 0
                }));
            }
            if (view instanceof TextView) {
                TextView textView = (TextView) view;
                textView.setTextColor(Color.RED);
                textView.setGravity(Gravity.CENTER);
            }
        }
        toast.show();
    }

    static class Debugger extends PathClassLoader {
        static final ClassLoader sClassLoader = Debugger.class.getClassLoader();
        static final String sPackageName;

        static {
            Matcher matcher = Pattern.compile("([\\w.]+)-\\d+/base\\.apk").matcher(sClassLoader.toString());
            if (matcher.find()) {
                sPackageName = matcher.group(1);
            } else {
                sPackageName = null;
            }
        }

        static boolean isDebug() {
            try {
                Class<?> buildConfig = Class.forName(sPackageName + ".BuildConfig");
                return Boolean.TRUE.equals(buildConfig.getField("DEBUG").get(null));
            } catch (Throwable t) {
                return false;
            }
        }

        static boolean reloadIfNeeded(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
            if (Debugger.class.getName().equals(sClassLoader.getClass().getName())) {
                return false;
            }
            PackageInfo packageInfo = HiddenAPI.getPackageInfo(sPackageName);
            if (packageInfo == null) { //Like the system_server
                return false;
            }
            IXposedHookLoadPackage xposedInit = (IXposedHookLoadPackage) new Debugger(packageInfo).loadClass("utopia.android.patch.xposed.XposedKernel").newInstance();
            xposedInit.handleLoadPackage(lpparam);
            return true;
        }

        Debugger(PackageInfo packageInfo) {
            super(packageInfo.applicationInfo.sourceDir, packageInfo.applicationInfo.nativeLibraryDir, ClassLoader.getSystemClassLoader());
        }
    }
}
