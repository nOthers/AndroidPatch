package utopia.android.patch.util.logcat;

import android.util.Pair;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.util.SparseLongArray;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import utopia.android.patch.util.MissingAPI;

public abstract class FormatMap implements Format {
    @Override
    public final Pair<String, Object[]> format(Object object) {
        if (!belongTo(object)) {
            return null;
        }
        Object[] array2 = toArray2(object);
        StringBuilder sBuilder = new StringBuilder();
        String name = nameOf(object);
        if (name != null) {
            sBuilder.append(name);
        }
        sBuilder.append("{");
        if (array2 != null) {
            if (array2.length % 2 != 0) { //Fix mistake
                List<Object> list = new LinkedList<>();
                Collections.addAll(list, array2);
                list.add(null);
                array2 = list.toArray();
            }
            for (int i = 0; i < array2.length / 2; i++) {
                if (i != 0) {
                    sBuilder.append(", ");
                }
                sBuilder.append("%s").append("=").append("%s");
            }
        }
        sBuilder.append("}");
        return new Pair<>(sBuilder.toString(), array2);
    }

    /**
     * Filter before calling toArray2.
     *
     * @param object any object
     * @return calling or not
     */
    public abstract boolean belongTo(Object object);

    /**
     * Make object to an array2.
     *
     * @param object filtered object
     * @return array2
     */
    public abstract Object[] toArray2(Object object);

    /**
     * Get name of the object.
     *
     * @param object filtered object
     * @return name
     */
    public String nameOf(Object object) {
        if (object != null) {
            return MissingAPI.getSimpleName(object.getClass());
        }
        return null;
    }

    /**
     * Support to format entry.
     */
    static final Format A = new FormatMap() {
        @Override
        public boolean belongTo(Object object) {
            return isMap(object);
        }

        @Override
        public Object[] toArray2(Object object) {
            return mapToArray2(object);
        }
    };
    static final Format B = new FormatMap() {
        @Override
        public boolean belongTo(Object object) {
            return isFinalMap(object);
        }

        @Override
        public Object[] toArray2(Object object) {
            return mapToArray2(object);
        }
    };

    static boolean isMap(Object object) {
        if (object != null) {
            if (object instanceof SparseBooleanArray
                    || object instanceof SparseIntArray
                    || object instanceof SparseLongArray
                    || object instanceof SparseArray
            ) {
                return true;
            }
            if (object instanceof Map) {
                return true;
            }
        }
        return false;
    }

    static Object[] mapToArray2(Object object) {
        if (object != null) {
            if (object instanceof SparseBooleanArray
                    || object instanceof SparseIntArray
                    || object instanceof SparseLongArray
                    || object instanceof SparseArray
            ) {
                object = MissingAPI.sparseToMap(object); //Convert to map
            }
            if (object instanceof Map) {
                List<Object> array = new LinkedList<>();
                Map map = (Map) object;
                for (Object key : map.keySet()) {
                    array.add(key);
                    array.add(map.get(key));
                }
                return array.toArray();
            }
        }
        return null;
    }

    static boolean isFinalMap(Object object) {
        if (object instanceof Map) {
            if (!MissingAPI.isSystemClass(object.getClass())) {
                return false;
            }
        }
        return isMap(object);
    }
}
