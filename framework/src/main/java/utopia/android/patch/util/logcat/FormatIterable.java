package utopia.android.patch.util.logcat;

import android.util.Pair;

import java.util.LinkedList;
import java.util.List;

import utopia.android.patch.util.MissingAPI;

public abstract class FormatIterable implements Format {
    @Override
    public final Pair<String, Object[]> format(Object object) {
        if (!belongTo(object)) {
            return null;
        }
        Object[] array = toArray(object);
        StringBuilder sBuilder = new StringBuilder();
        String name = nameOf(object);
        if (name != null) {
            sBuilder.append(name);
        }
        sBuilder.append("[");
        if (array != null) {
            for (int i = 0; i < array.length; i++) {
                if (i != 0) {
                    sBuilder.append(", ");
                }
                sBuilder.append("%s");
            }
        }
        sBuilder.append("]");
        return new Pair<>(sBuilder.toString(), array);
    }

    /**
     * Filter before calling toArray.
     *
     * @param object any object
     * @return calling or not
     */
    public abstract boolean belongTo(Object object);

    /**
     * Make object to an array.
     *
     * @param object filtered object
     * @return array
     */
    public abstract Object[] toArray(Object object);

    /**
     * Get name of the object.
     *
     * @param object filtered object
     * @return name
     */
    public String nameOf(Object object) {
        if (object != null) {
            Class<?> objectClass = object.getClass();
            Class<?> componentType = objectClass.getComponentType();
            if (componentType != null) {
                return MissingAPI.getSimpleName(componentType);
            }
            return MissingAPI.getSimpleName(objectClass);
        }
        return null;
    }

    /**
     * Support to format entry.
     */
    static final Format A = new FormatIterable() {
        @Override
        public boolean belongTo(Object object) {
            return isIterable(object);
        }

        @Override
        public Object[] toArray(Object object) {
            return iterableToArray(object);
        }
    };
    static final Format B = new FormatIterable() {
        @Override
        public boolean belongTo(Object object) {
            return isFinalIterable(object);
        }

        @Override
        public Object[] toArray(Object object) {
            return iterableToArray(object);
        }
    };

    static boolean isIterable(Object object) {
        if (object != null) {
            String className = object.getClass().getName();
            if ("[Z".equals(className)
                    || "[B".equals(className)
                    || "[S".equals(className)
                    || "[I".equals(className)
                    || "[J".equals(className)
                    || "[F".equals(className)
                    || "[D".equals(className)
                    || "[C".equals(className)
            ) {
                return true;
            }
            if (object instanceof Object[]) {
                return true;
            }
            if (object instanceof Iterable) {
                return true;
            }
            if (object instanceof Pair) {
                return true;
            }
        }
        return false;
    }

    static Object[] iterableToArray(Object object) {
        if (object != null) {
            String className = object.getClass().getName();
            if ("[Z".equals(className)
                    || "[B".equals(className)
                    || "[S".equals(className)
                    || "[I".equals(className)
                    || "[J".equals(className)
                    || "[F".equals(className)
                    || "[D".equals(className)
                    || "[C".equals(className)
            ) {
                return MissingAPI.basicToArray(object);
            }
            if (object instanceof Object[]) {
                return (Object[]) object;
            }
            if (object instanceof Iterable) {
                List<Object> array = new LinkedList<>();
                for (Object item : (Iterable) object) {
                    array.add(item);
                }
                return array.toArray();
            }
            if (object instanceof Pair) {
                Pair pair = (Pair) object;
                return new Object[]{pair.first, pair.second};
            }
        }
        return null;
    }

    static boolean isFinalIterable(Object object) {
        if (object instanceof Iterable) {
            if (!MissingAPI.isSystemClass(object.getClass())) {
                return false;
            }
        }
        return isIterable(object);
    }
}
