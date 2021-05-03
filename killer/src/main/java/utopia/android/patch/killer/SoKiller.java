package utopia.android.patch.killer;

import android.content.Context;

import java.io.File;

import utopia.android.patch.util.HiddenAPI;

public class SoKiller extends Killer {
    public SoKiller(final String filename) {
        addOnSharedObjectLoadedListener(new OnSharedObjectLoadedListener() {
            @Override
            public void onSharedObjectLoaded(String name) {
                if (!new File(name).getName().equals(filename)) {
                    return;
                }
                Context context = HiddenAPI.currentApplication();
                if (context != null) {
                    String outputPath = context.getDir("out", Context.MODE_PRIVATE).getPath();
                    File file = new File(outputPath, filename);
                    byte[] data = dumpSharedObject(filename);
                    writeByteArrayToFile(file, data);
                }
            }
        });
    }
}
