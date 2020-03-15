package utopia.android.patch.util.logcat;

import android.util.Pair;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import utopia.android.patch.util.SupplyAPI;

public final class FormatEntry extends FormatObject {
    private final ThreadLocal<Stack<Object>> mIgnores; //cycle ignore
    private final List<Format> mFormatList; //format list
    private volatile Format[] mFormatArray; //format array

    public FormatEntry() {
        mIgnores = new ThreadLocal<>();
        mFormatList = new LinkedList<>();
        flush();
    }

    /**
     * Add a format node.
     *
     * @param format node
     */
    public void add(Format format) {
        if (format == null) {
            return;
        }
        synchronized (mFormatList) {
            mFormatList.remove(format);
            mFormatList.add(0, format);
            flush();
        }
    }

    /**
     * Remove a format node.
     *
     * @param format node
     */
    public void remove(Format format) {
        if (format == null) {
            return;
        }
        synchronized (mFormatList) {
            mFormatList.remove(format);
            flush();
        }
    }

    private void flush() {
        List<Format> formatList = new LinkedList<>();
        Collections.addAll(formatList, FormatObject.B, FormatIterable.B, FormatMap.B, FormatMap.A, FormatIterable.A, FormatObject.A);
        formatList.addAll(3, mFormatList);
        mFormatArray = formatList.toArray(new Format[0]);
    }

    @Override
    public boolean belongTo(Object object) {
        return true;
    }

    @Override
    public String toString(Object object) {
        Stack<Object> ignore = mIgnores.get();
        if (ignore == null) {
            ignore = new Stack<>();
            mIgnores.set(ignore);
        }
        if (ignore.lastIndexOf(object) >= 0) {
            return (object != null ? SupplyAPI.getSimpleName(object.getClass()) : null) + ".this";
        }
        Format[] formatArray = mFormatArray;
        for (Format format : formatArray) {
            Pair<String, Object[]> pair = format.format(object);
            if (pair == null) {
                continue; //Calling until got notnull
            }
            if (pair.second == null || pair.second.length <= 0) {
                return pair.first;
            } else {
                Object[] args = new Object[pair.second.length];
                ignore.push(object);
                try {
                    for (int i = 0; i < args.length; i++) {
                        args[i] = toString(pair.second[i]); //Iterative call self
                    }
                } finally {
                    ignore.pop();
                }
                return String.format(pair.first, args);
            }
        }
        return null;
    }
}
