package utopia.android.patch.util.reflect;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;

public class MethodWrapper extends Wrapper<Method> {
    public MethodWrapper(Method method) {
        super(method);
    }

    /**
     * Call method and transform checked exception.
     *
     * @param obj  this object
     * @param args method parameter
     * @param <T>  any type
     * @return result returned
     */
    public <T> T invoke(Object obj, Object... args) {
        try {
            return invokeWithException(obj, args);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new UndeclaredThrowableException(e); //Transform checked exception
        }
    }

    /**
     * Call method.
     *
     * @param obj  this object
     * @param args method parameter
     * @param <T>  any type
     * @return result returned
     * @throws Exception checked exception
     */
    public <T> T invokeWithException(Object obj, Object... args) throws Exception {
        wrapped.setAccessible(true);
        try {
            return (T) wrapped.invoke(obj, args);
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
