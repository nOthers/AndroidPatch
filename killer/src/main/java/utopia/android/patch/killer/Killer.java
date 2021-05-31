package utopia.android.patch.killer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class Killer {
    static {
        System.loadLibrary("killer");
    }

    public static native byte[] dumpDexFile_v23(long cookie);

    private static native void setOnSharedObjectLoadedListener(OnSharedObjectLoadedListener listener);

    private static final List<OnSharedObjectLoadedListener> sOnSharedObjectLoadedListeners = new LinkedList<>();

    static {
        setOnSharedObjectLoadedListener(new OnSharedObjectLoadedListener() {
            @Override
            public void onSharedObjectLoaded(String name) {
                OnSharedObjectLoadedListener[] listeners;
                synchronized (sOnSharedObjectLoadedListeners) {
                    listeners = sOnSharedObjectLoadedListeners.toArray(new OnSharedObjectLoadedListener[0]);
                }
                for (OnSharedObjectLoadedListener listener : listeners) {
                    listener.onSharedObjectLoaded(name);
                }
            }
        });
    }

    public static void addOnSharedObjectLoadedListener(OnSharedObjectLoadedListener listener) {
        synchronized (sOnSharedObjectLoadedListeners) {
            sOnSharedObjectLoadedListeners.remove(listener);
            sOnSharedObjectLoadedListeners.add(0, listener);
        }
    }

    public static void removeOnSharedObjectLoadedListener(OnSharedObjectLoadedListener listener) {
        synchronized (sOnSharedObjectLoadedListeners) {
            sOnSharedObjectLoadedListeners.remove(listener);
        }
    }

    public static native byte[] dumpSharedObject(String filename);

    public static byte[] readFileToByteArray(File file) {
        byte[] data = new byte[(int) file.length()];
        FileInputStream input = null;
        try {
            input = new FileInputStream(file);
            input.read(data);
        } catch (IOException ignore) {
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ignore) {
                }
            }
        }
        return data;
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
}
