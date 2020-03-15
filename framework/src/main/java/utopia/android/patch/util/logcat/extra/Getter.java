package utopia.android.patch.util.logcat.extra;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;

import utopia.android.patch.util.reflect.ClassWrapper;
import utopia.android.patch.util.reflect.WrappingError;

public abstract class Getter {
    public final String key; //key

    public Getter(String key) {
        this.key = key;
    }

    /**
     * Get value of the object with my key.
     *
     * @param object object
     * @return value
     * @throws Throwable from app or subclass
     */
    public abstract Object get(Object object) throws Throwable;

    /**
     * Auto find all getters of the class.
     *
     * @param clazz class
     * @return getters
     */
    public static Getter[] findGetters(Class<?> clazz) {
        Map<String, Getter> getters = new LinkedHashMap<>();
        if (clazz != null) { //Skip or not
            for (Field field : clazz.getFields()) { //Public field
                if (Modifier.isStatic(field.getModifiers())) {
                    continue; //Not static
                }
                if (field.getDeclaringClass().getClassLoader() != clazz.getClassLoader()) {
                    continue; //Same origin
                }
                Getter getter = makeGetter(field.getName());
                getters.put(getter.key, getter);
            }
            for (Method method : clazz.getMethods()) { //Public method
                if (Modifier.isStatic(method.getModifiers())) {
                    continue; //Not static
                }
                if (method.getDeclaringClass().getClassLoader() != clazz.getClassLoader()) {
                    continue; //Same origin
                }
                if (method.getReturnType() == void.class || method.getReturnType() == Void.class || method.getParameterTypes().length > 0) {
                    continue; //Has return and no parameter needed
                }
                String getterLike;
                String name = method.getName();
                if (name.startsWith("is")) {
                    getterLike = toFieldName(name, 2) + "=" + name;
                } else if (name.startsWith("get")) {
                    getterLike = toFieldName(name, 3) + "=" + name;
                } else {
                    continue; //Name not matched
                }
                Getter getter = makeGetter(getterLike);
                getters.put(getter.key, getter);
            }
        }
        return getters.values().toArray(new Getter[0]);
    }

    private static String toFieldName(String name, int begin) {
        name = name.substring(begin);
        if (name.length() > 0) {
            name = name.substring(0, 1).toLowerCase() + name.substring(1);
        }
        return name;
    }

    /**
     * Try make key to getter.
     *
     * @param getterLike key or getter
     * @return getter
     */
    public static Getter makeGetter(Object getterLike) {
        if (getterLike instanceof Getter) {
            return (Getter) getterLike;
        } else if (getterLike instanceof String) {
            String string = (String) getterLike;
            final String key, val;
            int index = string.indexOf('=');
            if (index < 0) {
                key = string;
                val = string;
            } else {
                key = string.substring(0, index);
                val = string.substring(index + 1);
            }
            return new Getter(key) {
                @Override
                public Object get(Object object) throws Throwable {
                    if (object != null) {
                        for (ClassWrapper wrapper : new ClassWrapper(object.getClass())) {
                            try {
                                return wrapper.getField(val).get(object);
                            } catch (WrappingError ignore) {
                            }
                            try {
                                return wrapper.getMethod(val).invokeWithException(object);
                            } catch (WrappingError ignore) {
                            }
                        }
                        throw new ReflectiveOperationException("no such field or method: " + val);
                    }
                    return null;
                }
            };
        }
        throw new IllegalArgumentException("illegal getter like object");
    }
}
