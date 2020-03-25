package utopia.android.patch.util.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;

public class ConstructorWrapper extends Wrapper<Constructor<?>> {
    public ConstructorWrapper(Constructor<?> constructor) {
        super(constructor);
    }

    /**
     * Call constructor and transform checked exception.
     *
     * @param initargs constructor parameter
     * @param <T>      any type
     * @return new object
     */
    public <T> T newInstance(Object... initargs) {
        try {
            return newInstanceWithException(initargs);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new UndeclaredThrowableException(e); //Transform checked exception
        }
    }

    /**
     * Call constructor.
     *
     * @param initargs constructor parameter
     * @param <T>      any type
     * @return new object
     * @throws Exception checked exception
     */
    public <T> T newInstanceWithException(Object... initargs) throws Exception {
        wrapped.setAccessible(true);
        try {
            return (T) wrapped.newInstance(initargs);
        } catch (IllegalArgumentException e) {
            throw new WrappingError(e); //Ignore reflect runtime exception
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            if (cause instanceof Exception) {
                throw (Exception) cause;
            }
            throw new WrappingError(e); //Never happen
        } catch (ReflectiveOperationException e) {
            throw new WrappingError(e); //Ignore reflect exception
        }
    }
}
