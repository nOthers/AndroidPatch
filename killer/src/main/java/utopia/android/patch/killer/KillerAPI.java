package utopia.android.patch.killer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public final class KillerAPI {
    private KillerAPI() {
    }

    public static void writeByteArrayToFile(File file, byte[] data) {
        file.getParentFile().mkdirs();
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(file);
            output.write(data);
        } catch (IOException ignore) {
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException ignore) {
                }
            }
        }
    }

    static {
        System.loadLibrary("killer");
    }

    public static native byte[] dumpDexFile_v23(long cookie);
}
