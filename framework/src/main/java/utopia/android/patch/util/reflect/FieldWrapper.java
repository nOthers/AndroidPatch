package utopia.android.patch.util.reflect;

import java.lang.reflect.Field;

public class FieldWrapper extends Wrapper<Field> {
    public FieldWrapper(Field field) {
        super(field);
    }

    /**
     * Get field value.
     *
     * @param obj this object
     * @param <T> any type
     * @return field value
     */
    public <T> T get(Object obj) {
        wrapped.setAccessible(true);
        try {
            return (T) wrapped.get(obj);
        } catch (IllegalArgumentException e) {
            throw new WrappingError(e); //Ignore reflect runtime exception
        } catch (ReflectiveOperationException e) {
            throw new WrappingError(e); //Ignore reflect exception
        }
    }

    /**
     * Set field value.
     *
     * @param obj   this object
     * @param value field value
     */
    public void set(Object obj, Object value) {
        wrapped.setAccessible(true);
        try {
            wrapped.set(obj, value);
        } catch (IllegalArgumentException e) {
            throw new WrappingError(e); //Ignore reflect runtime exception
        } catch (ReflectiveOperationException e) {
            throw new WrappingError(e); //Ignore reflect exception
        }
    }
}
