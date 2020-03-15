package utopia.android.patch.util;

import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.util.SparseLongArray;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public final class MissingAPI {
    private MissingAPI() {
    }

    /**
     * Determine whether the class is defined by the system.
     *
     * @param clazz class
     * @return is system defined
     */
    public static Boolean isSystemClass(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        ClassLoader classLoader = clazz.getClassLoader();
        ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
        while (systemClassLoader != null) {
            if (classLoader == systemClassLoader) {
                return true;
            }
            systemClassLoader = systemClassLoader.getParent();
        }
        return false;
    }

    /**
     * Get simple name of the class.
     *
     * @param clazz class
     * @return simple name
     */
    public static String getSimpleName(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        String simpleName = clazz.getSimpleName();
        if (simpleName == null || simpleName.length() <= 0) {
            simpleName = clazz.getName();
            simpleName = simpleName.substring(simpleName.lastIndexOf(".") + 1);
        }
        return simpleName;
    }

    /**
     * Get classes and interfaces from inherit tree, include self.
     *
     * @param root class
     * @return inherit tree
     */
    public static Class<?>[] getInheritClasses(Class<?> root) {
        Queue<Class<?>> input = new LinkedList<>();
        List<Class<?>> output = new LinkedList<>();
        input.add(root);
        Class<?> node;
        while ((node = input.poll()) != null) {
            if (output.contains(node)) {
                continue;
            }
            output.add(node);
            Class<?> superclass = node.getSuperclass();
            if (superclass != null) {
                input.add(superclass);
            }
            Collections.addAll(input, node.getInterfaces());
        }
        return output.toArray(new Class[0]);
    }

    /**
     * Basic array to object array.
     *
     * @param object basic array
     * @return object array
     */
    public static Object[] basicToArray(Object object) {
        if (object != null) {
            String className = object.getClass().getName();
            if ("[Z".equals(className)) {
                boolean[] objects = (boolean[]) object;
                Object[] array = new Object[objects.length];
                for (int i = 0; i < array.length; i++) {
                    array[i] = objects[i];
                }
                return array;
            }
            if ("[B".equals(className)) {
                byte[] objects = (byte[]) object;
                Object[] array = new Object[objects.length];
                for (int i = 0; i < array.length; i++) {
                    array[i] = objects[i];
                }
                return array;
            }
            if ("[S".equals(className)) {
                short[] objects = (short[]) object;
                Object[] array = new Object[objects.length];
                for (int i = 0; i < array.length; i++) {
                    array[i] = objects[i];
                }
                return array;
            }
            if ("[I".equals(className)) {
                int[] objects = (int[]) object;
                Object[] array = new Object[objects.length];
                for (int i = 0; i < array.length; i++) {
                    array[i] = objects[i];
                }
                return array;
            }
            if ("[J".equals(className)) {
                long[] objects = (long[]) object;
                Object[] array = new Object[objects.length];
                for (int i = 0; i < array.length; i++) {
                    array[i] = objects[i];
                }
                return array;
            }
            if ("[F".equals(className)) {
                float[] objects = (float[]) object;
                Object[] array = new Object[objects.length];
                for (int i = 0; i < array.length; i++) {
                    array[i] = objects[i];
                }
                return array;
            }
            if ("[D".equals(className)) {
                double[] objects = (double[]) object;
                Object[] array = new Object[objects.length];
                for (int i = 0; i < array.length; i++) {
                    array[i] = objects[i];
                }
                return array;
            }
            if ("[C".equals(className)) {
                char[] objects = (char[]) object;
                Object[] array = new Object[objects.length];
                for (int i = 0; i < array.length; i++) {
                    array[i] = objects[i];
                }
                return array;
            }
        }
        return null;
    }

    /**
     * Convert sparse array to map.
     *
     * @param object sparse array
     * @return map
     */
    public static Map<Object, Object> sparseToMap(Object object) {
        if (object != null) {
            if (object instanceof SparseBooleanArray) {
                Map<Object, Object> map = new LinkedHashMap<>();
                SparseBooleanArray sparseArray = (SparseBooleanArray) object;
                for (int i = 0; i < sparseArray.size(); i++) {
                    int key = sparseArray.keyAt(i);
                    map.put(key, sparseArray.get(key));
                }
                return map;
            }
            if (object instanceof SparseIntArray) {
                Map<Object, Object> map = new LinkedHashMap<>();
                SparseIntArray sparseArray = (SparseIntArray) object;
                for (int i = 0; i < sparseArray.size(); i++) {
                    int key = sparseArray.keyAt(i);
                    map.put(key, sparseArray.get(key));
                }
                return map;
            }
            if (object instanceof SparseLongArray) {
                Map<Object, Object> map = new LinkedHashMap<>();
                SparseLongArray sparseArray = (SparseLongArray) object;
                for (int i = 0; i < sparseArray.size(); i++) {
                    int key = sparseArray.keyAt(i);
                    map.put(key, sparseArray.get(key));
                }
                return map;
            }
            if (object instanceof SparseArray) {
                Map<Object, Object> map = new LinkedHashMap<>();
                SparseArray sparseArray = (SparseArray) object;
                for (int i = 0; i < sparseArray.size(); i++) {
                    int key = sparseArray.keyAt(i);
                    map.put(key, sparseArray.get(key));
                }
                return map;
            }
        }
        return null;
    }
}
