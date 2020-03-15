package utopia.android.patch.lang;

import java.lang.reflect.Member;
import java.util.HashMap;
import java.util.Map;

public final class Patchwork {
    private Patchwork() {
    }

    static volatile ExternalKernel externalKernel;

    /**
     * Provide patch implementation, calling by external kernel.
     *
     * @param kernel external kernel
     */
    static void provide(ExternalKernel kernel) {
        externalKernel = kernel;
    }

    /**
     * Get the provider name.
     *
     * @return provider name
     * @throws PatchError if not support
     */
    public static String provider() throws PatchError {
        try {
            return externalKernel.getName();
        } catch (PatchError e) {
            throw e;
        } catch (Throwable t) {
            throw new PatchError(t);
        }
    }

    static volatile long count = 0;
    static final Map<Long, Object[]> pairs = new HashMap<>();

    /**
     * Patch a constructor or a method.
     *
     * @param member constructor or method
     * @param patch  patch
     * @return patch id
     * @throws PatchError if not support
     */
    public static synchronized long patch(Member member, Patch patch) throws PatchError {
        if (member == null) {
            throw new IllegalArgumentException("member cannot be null");
        }
        if (patch == null) {
            throw new IllegalArgumentException("patch cannot be null");
        }
        try {
            for (Long id : pairs.keySet()) {
                Object[] pair = pairs.get(id);
                if (pair[0] == member && pair[1] == patch) {
                    return id; //Already patched
                }
            }
            externalKernel.addPatch(member, patch);
            pairs.put(++count, new Object[]{member, patch});
            return count;
        } catch (PatchError e) {
            throw e;
        } catch (Throwable t) {
            throw new PatchError(t);
        }
    }

    /**
     * Recover patch by patch id.
     *
     * @param id patch id
     * @return patch exists or not
     * @throws PatchError if not support
     */
    public static synchronized boolean recover(long id) throws PatchError {
        try {
            Object[] pair = pairs.remove(id);
            if (pair != null) {
                externalKernel.removePatch((Member) pair[0], (Patch) pair[1]);
            }
            return pair != null;
        } catch (PatchError e) {
            throw e;
        } catch (Throwable t) {
            throw new PatchError(t);
        }
    }
}
