package utopia.android.patch.lang;

public class PatchError extends Error {
    public PatchError() {
        super();
    }

    public PatchError(String message) {
        super(message);
    }

    public PatchError(String message, Throwable cause) {
        super(message, cause);
    }

    public PatchError(Throwable cause) {
        super(cause);
    }
}
