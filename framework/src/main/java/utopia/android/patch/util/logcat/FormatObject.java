package utopia.android.patch.util.logcat;

import android.util.Pair;

import java.lang.reflect.Member;
import java.lang.reflect.Type;

public abstract class FormatObject implements Format {
    @Override
    public final Pair<String, Object[]> format(Object object) {
        if (!belongTo(object)) {
            return null;
        }
        return new Pair<>(toString(object), null);
    }

    /**
     * Filter before calling toString.
     *
     * @param object any object
     * @return calling or not
     */
    public abstract boolean belongTo(Object object);

    /**
     * Make object to a readable string.
     *
     * @param object filtered object
     * @return readable string
     */
    public abstract String toString(Object object);

    /**
     * Support to format entry.
     */
    static final Format A = new FormatObject() {
        @Override
        public boolean belongTo(Object object) {
            return isObject(object);
        }

        @Override
        public String toString(Object object) {
            return objectToString(object);
        }
    };
    static final Format B = new FormatObject() {
        @Override
        public boolean belongTo(Object object) {
            return isFinalObject(object);
        }

        @Override
        public String toString(Object object) {
            return objectToString(object);
        }
    };

    static boolean isObject(Object object) {
        return true;
    }

    static String objectToString(Object object) {
        return String.valueOf(object);
    }

    static boolean isFinalObject(Object object) {
        if (object == null) {
            return true;
        }
        String className = object.getClass().getName();
        if ("boolean".equals(className)
                || "byte".equals(className)
                || "short".equals(className)
                || "int".equals(className)
                || "long".equals(className)
                || "float".equals(className)
                || "double".equals(className)
                || "char".equals(className)
        ) {
            return true;
        }
        if (object instanceof Boolean
                || object instanceof Number
                || object instanceof Character
                || object instanceof CharSequence
        ) {
            return true;
        }
        if (object instanceof Type
                || object instanceof Member
                || object instanceof Throwable
        ) {
            return true;
        }
        return false;
    }
}
