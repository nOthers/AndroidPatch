package utopia.android.patch.util.logcat.extra;

import android.util.Pair;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import utopia.android.patch.util.SupplyAPI;
import utopia.android.patch.util.logcat.Format;
import utopia.android.patch.util.logcat.FormatMap;
import utopia.android.patch.util.logcat.Logcat;
import utopia.android.patch.util.reflect.ClassWrapper;
import utopia.android.patch.util.reflect.Wrapper;

public final class Rose extends Logcat {
    private static volatile Rose instance; //rose

    /**
     * Get the rose.
     *
     * @return rose
     */
    private static Rose getInstance() {
        if (instance == null) {
            synchronized (Rose.class) {
                if (instance == null) {
                    instance = new Rose();
                }
            }
        }
        return instance;
    }

    private final Set<String> mIgnored; //ignored packages when hooking
    private final Set<Class<?>> mHooking; //hooking classes

    private Rose() {
        super("rose");
        mIgnored = new HashSet<>();
        mHooking = new HashSet<>();
        getFormatter().add(formatWrapper);
    }

    private static final Format formatWrapper = new Format() {
        @Override
        public Pair<String, Object[]> format(Object object) {
            if (object instanceof Wrapper) {
                return new Pair<>("%s", new Object[]{((Wrapper) object).wrapped});
            }
            return null;
        }
    }; //format wrapper

    /**
     * Ignore all classes belong to the package.
     *
     * @param packageName package name
     */
    public void ignore(String packageName) {
        if (packageName == null) {
            return;
        }
        synchronized (mIgnored) {
            synchronized (mHooking) {
                mIgnored.add(packageName);
                for (Class<?> clazz : new HashSet<>(mHooking)) {
                    if (clazz.getName().startsWith(packageName)) {
                        unhook(clazz);
                    }
                }
            }
        }
    }

    @Override
    public void hook(Class<?> clazz) {
        if (clazz == null) {
            return;
        }
        synchronized (mIgnored) {
            synchronized (mHooking) {
                for (String ignored : mIgnored) {
                    if (clazz.getName().startsWith(ignored)) {
                        return;
                    }
                }
                mHooking.add(clazz);
                super.hook(clazz);
            }
        }
    }

    @Override
    public void unhook(Class<?> clazz) {
        if (clazz == null) {
            return;
        }
        synchronized (mHooking) {
            super.unhook(clazz);
            mHooking.remove(clazz);
        }
    }

    /**
     * Suggest ignores, not default.
     */
    public static final String[] SUGGEST_IGNORES = new String[]{"android"};

    /**
     * Make object to a readable string.
     *
     * @param object any object
     * @return readable string
     */
    public static String toString(Object object) {
        return getInstance().getFormatter().toString(object);
    }

    /**
     * Begin a chain.
     *
     * @return chain
     */
    public static Chain chain() {
        return getInstance().new Chain();
    }

    public class Chain {
        private volatile ClassLoader mClassLoader; //chain context

        private Chain() {
            mClassLoader = null;
        }

        /**
         * End of the chain.
         */
        public void over() {
        }

        /**
         * Ignore all classes belong to those packages.
         *
         * @param packageNames package names
         * @return chain
         */
        public Chain ignores(String... packageNames) {
            if (packageNames != null) {
                for (String packageName : packageNames) {
                    ignore(packageName);
                }
            }
            return this;
        }

        /**
         * Specified the class loader.
         *
         * @param classLoader class loader
         * @return chain
         */
        public Chain use(ClassLoader classLoader) {
            mClassLoader = classLoader;
            return this;
        }

        /**
         * Hooking a class by name or class.
         *
         * @param classLike name or class
         * @return chain
         */
        public Chain trace(Object classLike) {
            hook(makeClassWrapper(classLike).wrapped);
            return this;
        }

        /**
         * Hooking all superclasses by name or class.
         *
         * @param classLike name or class
         * @return chain
         */
        public Chain traceSuper(Object classLike) {
            for (ClassWrapper wrapper : makeClassWrapper(classLike).getSuperclassesSameOrigin()) { //Only app defined
                hook(wrapper.wrapped);
            }
            return this;
        }

        /**
         * Hooking all inner classes by name or class
         *
         * @param classLike name or class
         * @return chain
         */
        public Chain traceInner(Object classLike) {
            for (ClassWrapper wrapper : makeClassWrapper(classLike).getInnerClasses()) {
                hook(wrapper.wrapped);
            }
            return this;
        }

        /**
         * Format object with custom algorithm.
         *
         * @param classLike   name or class
         * @param getterLikes key or getter
         * @return chain
         */
        public Chain format(Object classLike, Object... getterLikes) {
            if (getterLikes == null) {
                getterLikes = new Object[0];
            }
            Getter[] getters = new Getter[getterLikes.length];
            for (int i = 0; i < getters.length; i++) {
                getters[i] = Getter.makeGetter(getterLikes[i]); //Making getter
            }
            getFormatter().add(newFormat1(makeClassWrapper(classLike).wrapped, getters));
            return this;
        }

        /**
         * Format whole package with fixed algorithm, packageName=null means every package.
         *
         * @param packageName package name
         * @return chain
         */
        public Chain formatPackage(String packageName) {
            getFormatter().add(newFormat2(packageName));
            return this;
        }

        private ClassWrapper makeClassWrapper(Object classLike) {
            if (classLike instanceof ClassWrapper) {
                return (ClassWrapper) classLike;
            } else if (classLike instanceof Class) {
                return new ClassWrapper((Class<?>) classLike);
            } else if (classLike instanceof String) {
                return new ClassWrapper((String) classLike, mClassLoader);
            }
            throw new IllegalArgumentException("illegal class like object");
        }
    }

    static abstract class Format0 extends FormatMap {
        final Getter[] mGetters; //custom getters

        Format0(Getter[] getters) {
            mGetters = getters;
        }

        Getter[] getGetters(Object object) {
            Getter[] getters = mGetters;
            if (getters == null || getters.length <= 0) { //Dynamically find all getters
                getters = Getter.findGetters(object != null ? object.getClass() : null);
            }
            return getters;
        }

        @Override
        public Object[] toArray2(Object object) {
            List<Object> array = new LinkedList<>();
            if (object != null) {
                for (Getter getter : getGetters(object)) {
                    Object val;
                    try {
                        val = getter.get(object);
                    } catch (Throwable t) {
                        val = t; //Throw from app or plugin
                    }
                    array.add(getter.key);
                    array.add(val);
                }
            }
            return array.toArray();
        }
    }

    static Format newFormat1(final Class<?> clazz, Getter[] getters) {
        return new Format0(getters) {
            @Override
            public boolean belongTo(Object object) {
                if (clazz == null) {
                    return true;
                }
                return clazz.isInstance(object);
            }
        };
    }

    static Format newFormat2(final String packageName) {
        return new Format0(null) {
            @Override
            public boolean belongTo(Object object) {
                if (packageName == null) {
                    return true;
                }
                for (Class<?> clazz : SupplyAPI.getInheritClasses(object != null ? object.getClass() : null)) {
                    if (clazz.getName().startsWith(packageName)) {
                        return true;
                    }
                }
                return false;
            }
        };
    }
}
