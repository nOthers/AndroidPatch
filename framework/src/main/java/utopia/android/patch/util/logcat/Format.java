package utopia.android.patch.util.logcat;

import android.util.Pair;

public interface Format {
    /**
     * Make object to a formatting.
     * Return null means unable to deal with, maybe continue.
     *
     * @param object any object
     * @return formatting or null
     */
    Pair<String, Object[]> format(Object object);
}
