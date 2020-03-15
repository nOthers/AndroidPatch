package utopia.android.patch.util.reflect;

public class Wrapper<T> {
    public final T wrapped; //wrapped object

    protected Wrapper(T anyObject) {
        if (anyObject == null) {
            throw new WrappingError("wrapped can not be null");
        }
        wrapped = anyObject;
    }
}
