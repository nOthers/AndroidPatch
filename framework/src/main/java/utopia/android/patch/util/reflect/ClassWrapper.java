package utopia.android.patch.util.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import utopia.android.patch.util.HiddenAPI;

public class ClassWrapper extends Wrapper<Class<?>> implements Iterable<ClassWrapper> {
    public ClassWrapper(String className) {
        this(newWrapped(className, null));
    }

    public ClassWrapper(String className, ClassLoader classLoader) {
        this(newWrapped(className, classLoader));
    }

    private static Class<?> newWrapped(String className, ClassLoader classLoader) {
        if (className == null) {
            className = "";
        }
        if (classLoader == null) {
            classLoader = HiddenAPI.currentClassLoader();
        }
        try {
            return Class.forName(className, false, classLoader);
        } catch (ClassNotFoundException e) {
            throw new WrappingError(e); //Ignore reflect exception
        }
    }

    public ClassWrapper(Class<?> clazz) {
        super(clazz);
    }

    /**
     * Get name of wrapped class.
     *
     * @return class name
     */
    public String getName() {
        return wrapped.getName();
    }

    /**
     * Get loader of wrapped class.
     *
     * @return class loader
     */
    public ClassLoader getClassLoader() {
        return wrapped.getClassLoader();
    }

    /**
     * Search superclasses, include self. (default iterator)
     *
     * @return superclasses
     */
    public ClassWrapper[] getSuperclasses() {
        List<ClassWrapper> superclasses = new LinkedList<>();
        superclasses.add(this);
        Class<?> superclass = wrapped;
        while (true) {
            superclass = superclass.getSuperclass();
            if (superclass == null) {
                break; //Interface or basic class
            }
            superclasses.add(new ClassWrapper(superclass));
        }
        return superclasses.toArray(new ClassWrapper[0]);
    }

    /**
     * Search superclasses same origin, include self.
     *
     * @return superclasses
     */
    public ClassWrapper[] getSuperclassesSameOrigin() {
        List<ClassWrapper> superclasses = new LinkedList<>();
        for (ClassWrapper wrapper : getSuperclasses()) {
            if (wrapper.wrapped.getClassLoader() == wrapped.getClassLoader()) {
                superclasses.add(wrapper);
            }
        }
        return superclasses.toArray(new ClassWrapper[0]);
    }

    /**
     * Search inner classes, include self.
     *
     * @return inner classes
     */
    public ClassWrapper[] getInnerClasses() {
        Queue<Class<?>> input = new LinkedList<>();
        List<Class<?>> output = new LinkedList<>();
        input.add(wrapped);
        Class<?> node;
        while ((node = input.poll()) != null) {
            if (output.contains(node)) {
                continue;
            }
            output.add(node);
            Collections.addAll(input, node.getDeclaredClasses());
            int n = 0;
            while (true) {
                Class<?> anonymousClass;
                try {
                    anonymousClass = Class.forName(node.getName() + "$" + (++n), false, node.getClassLoader());
                } catch (ClassNotFoundException e) {
                    break;
                }
                input.add(anonymousClass);
            }
        }
        List<ClassWrapper> classes = new LinkedList<>();
        for (Class<?> clazz : output) {
            if (clazz == wrapped) {
                classes.add(this);
            } else {
                classes.add(new ClassWrapper(clazz));
            }
        }
        return classes.toArray(new ClassWrapper[0]);
    }

    @Override
    public Iterator<ClassWrapper> iterator() {
        return Arrays.asList(getSuperclasses()).iterator();
    }

    /**
     * Search a constructor.
     * Type value could be null or a string or a class, Same below.
     *
     * @param parameterTypes parameter types
     * @return matched constructor
     */
    public ConstructorWrapper getConstructor(Object... parameterTypes) {
        if (parameterTypes == null) {
            parameterTypes = new Object[0];
        }
        for (Constructor<?> constructor : wrapped.getDeclaredConstructors()) {
            Class<?>[] types = constructor.getParameterTypes();
            if (types.length != parameterTypes.length) {
                continue;
            }
            int i = 0;
            for (; i < types.length; i++) {
                if (!equalsType(types[i], parameterTypes[i])) {
                    break;
                }
            }
            if (i < types.length) {
                continue;
            }
            return new ConstructorWrapper(constructor);
        }
        throw new WrappingError("no such constructor");
    }

    /**
     * Search a normal field.
     *
     * @param fieldName field name
     * @return matched field
     */
    public FieldWrapper getField(String fieldName) {
        return getFieldWithObfuscate(fieldName, null);
    }

    /**
     * Search a field.
     *
     * @param fieldName field name
     * @param fieldType field type
     * @return matched field
     */
    public FieldWrapper getFieldWithObfuscate(String fieldName, Object fieldType) {
        for (Field field : wrapped.getDeclaredFields()) {
            if (!field.getName().equals(fieldName)) {
                continue;
            }
            if (!equalsType(field.getType(), fieldType)) {
                continue;
            }
            return new FieldWrapper(field);
        }
        throw new WrappingError("no such field");
    }

    /**
     * Search a normal method.
     *
     * @param methodName     method name
     * @param parameterTypes parameter types
     * @return matched method
     */
    public MethodWrapper getMethod(String methodName, Object... parameterTypes) {
        return getMethodWithObfuscate(methodName, null, parameterTypes);
    }

    /**
     * Search a method.
     *
     * @param methodName     method name
     * @param returnType     return type
     * @param parameterTypes parameter types
     * @return matched method
     */
    public MethodWrapper getMethodWithObfuscate(String methodName, Object returnType, Object... parameterTypes) {
        if (parameterTypes == null) {
            parameterTypes = new Object[0];
        }
        for (Method method : wrapped.getDeclaredMethods()) {
            if (!method.getName().equals(methodName)) {
                continue;
            }
            if (!equalsType(method.getReturnType(), returnType)) {
                continue;
            }
            Class<?>[] types = method.getParameterTypes();
            if (types.length != parameterTypes.length) {
                continue;
            }
            int i = 0;
            for (; i < types.length; i++) {
                if (!equalsType(types[i], parameterTypes[i])) {
                    break;
                }
            }
            if (i < types.length) {
                continue;
            }
            return new MethodWrapper(method);
        }
        throw new WrappingError("no such method");
    }

    private static boolean equalsType(Class<?> type, Object condition) {
        if (condition == null) {
            return true;
        }
        if (condition instanceof String) {
            return type.getName().equals(condition);
        }
        if (condition instanceof Class) {
            return type == condition;
        }
        throw new WrappingError("illegal condition type");
    }
}
