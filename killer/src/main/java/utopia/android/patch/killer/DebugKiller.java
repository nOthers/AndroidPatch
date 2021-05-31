package utopia.android.patch.killer;

import java.io.File;

import utopia.android.patch.util.HiddenAPI;

public class DebugKiller extends Killer {
    public final static String DEBUG_TYPE_GDB = "gdb";
    public final static String DEBUG_TYPE_IDA = "ida";

    public DebugKiller(String type) {
        this(type, "/data/local/tmp");
    }

    public DebugKiller(String type, String assets) {
        if (DEBUG_TYPE_GDB.equals(type)) {
            startGDBServer(assets);
        } else if (DEBUG_TYPE_IDA.equals(type)) {
            startIDAServer(assets);
        } else {
            throw new IllegalArgumentException("illegal argument type: " + type);
        }
    }

    static void executeCommand(String command, boolean waitFor) {
        try {
            Process proc = Runtime.getRuntime().exec(command);
            if (waitFor) {
                proc.waitFor();
            }
        } catch (Exception ignore) {
        }
    }

    static String copyExecutableFile(File sourceFile) {
        byte[] data = readFileToByteArray(sourceFile);
        if (data == null || data.length == 0) {
            return null;
        }
        String targetPath = "/data/data/" + HiddenAPI.currentPackageName() + "/debug/" + sourceFile.getName();
        writeByteArrayToFile(new File(targetPath), data);
        executeCommand("chomd 700 " + targetPath, true);
        return targetPath;
    }

    public static boolean startGDBServer(String assets) {
        String debugServer = copyExecutableFile(new File(assets, "gdbserver"));
        if (debugServer != null) {
            executeCommand(debugServer + " :23946 --attach " + android.os.Process.myPid(), false);
        }
        return debugServer != null;
    }

    public static boolean startIDAServer(String assets) {
        String debugServer = copyExecutableFile(new File(assets, "android_server"));
        if (debugServer != null) {
            executeCommand(debugServer, false);
        }
        return debugServer != null;
    }
}
