package utopia.android.patch.util.reflect;

public class WrappingError extends Error {
    public WrappingError() {
        super();
    }

    public WrappingError(String message) {
        super(message);
    }

    public WrappingError(String message, Throwable cause) {
        super(message, cause);
    }

    public WrappingError(Throwable cause) {
        super(cause);
    }
}
