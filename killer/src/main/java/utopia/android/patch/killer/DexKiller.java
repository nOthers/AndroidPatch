package utopia.android.patch.killer;

import android.app.Application;
import android.content.Context;

import java.io.File;

import utopia.android.patch.lang.Patch;
import utopia.android.patch.lang.Patchwork;
import utopia.android.patch.util.reflect.ClassWrapper;

public class DexKiller {
    public DexKiller() {
        Patchwork.patch(new ClassWrapper(Application.class)
                .getMethod("attach", Context.class)
                .wrapped, new Patch() {
            Application mFirstApplication;

            @Override
            protected void beforeExecuted(Registers registers) {
                if (mFirstApplication == null) {
                    mFirstApplication = (Application) registers.self;
                }
            }

            @Override
            protected void afterExecuted(Registers registers) {
                if (mFirstApplication == registers.self) {
                    ClassLoader classLoader = mFirstApplication.getClassLoader();
                    String outputPath = mFirstApplication.getDir("out", Context.MODE_PRIVATE).getPath();
                    dumpClassLoader_v23(classLoader, outputPath);
                }
            }
        });
    }

    static void dumpClassLoader_v23(ClassLoader classLoader, String outputPath) {
        int n = 0;
        Object pathList = new ClassWrapper("dalvik.system.BaseDexClassLoader")
                .getField("pathList")
                .get(classLoader);
        Object[] dexElements = new ClassWrapper("dalvik.system.DexPathList")
                .getField("dexElements")
                .get(pathList);
        for (Object dexElement : dexElements) {
            Object dexFile = new ClassWrapper("dalvik.system.DexPathList$Element")
                    .getField("dexFile")
                    .get(dexElement);
            long[] mCookie = new ClassWrapper("dalvik.system.DexFile")
                    .getField("mCookie")
                    .get(dexFile);
            for (long cookie : mCookie) {
                File file = new File(outputPath, "classes" + (n++) + ".dex");
                byte[] data = KillerAPI.dumpDexFile_v23(cookie);
                KillerAPI.writeByteArrayToFile(file, data);
            }
        }
    }
}
