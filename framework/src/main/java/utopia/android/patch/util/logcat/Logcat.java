package utopia.android.patch.util.logcat;

import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import utopia.android.patch.lang.Patch;
import utopia.android.patch.lang.PatchError;
import utopia.android.patch.lang.Patchwork;

public class Logcat extends Patch {
    private final String mTag; //tag
    private final ThreadLocal<Boolean> mIgnores; //cycle ignore
    private final ThreadLocal<Integer> mDepths; //stack depth
    private final ThreadLocal<String> mRecords; //name record
    private final Map<Class<?>, List<Long>> mUnhooks; //logging unhooks
    private final FormatEntry mFormatter; //logging formatter

    public Logcat(String tag) {
        super(Integer.MAX_VALUE);
        mTag = tag;
        mIgnores = new ThreadLocal<>();
        mDepths = new ThreadLocal<>();
        mRecords = new ThreadLocal<>();
        mUnhooks = new LinkedHashMap<>();
        mFormatter = new FormatEntry();
    }

    /**
     * Get logging formatter.
     *
     * @return formatter
     */
    public FormatEntry getFormatter() {
        return mFormatter;
    }

    /**
     * Start logging the class.
     *
     * @param clazz class
     */
    public void hook(Class<?> clazz) {
        if (clazz == null) {
            return;
        }
        synchronized (mUnhooks) {
            if (mUnhooks.containsKey(clazz)) {
                return;
            }
            List<Long> unhooks = new LinkedList<>();
            List<Member> members = new LinkedList<>();
            Collections.addAll(members, clazz.getDeclaredConstructors());
            Collections.addAll(members, clazz.getDeclaredMethods());
            for (Member member : members) {
                try {
                    unhooks.add(Patchwork.patch(member, this));
                } catch (PatchError ignore) {
                }
            }
            mUnhooks.put(clazz, unhooks);
        }
    }

    /**
     * End logging the class.
     *
     * @param clazz class
     */
    public void unhook(Class<?> clazz) {
        if (clazz == null) {
            return;
        }
        synchronized (mUnhooks) {
            if (mUnhooks.containsKey(clazz)) {
                for (Long id : mUnhooks.remove(clazz)) {
                    Patchwork.recover(id);
                }
            }
        }
    }

    protected String currentTag() {
        StringBuilder sBuilder = new StringBuilder();
        sBuilder.append(mTag).append("/");
        Thread currentThread = Thread.currentThread();
        if ("main".equals(currentThread.getName())) {
            return sBuilder.append("0000").toString();
        } else {
            int id = currentThread.hashCode();
            if (id < 0) {
                id += 0x80000000;
            }
            id %= 0x10000;
            return sBuilder.append(String.format("%04x", id)).toString();
        }
    }

    @Override
    protected void beforeExecuted(Registers registers) {
        if (Boolean.TRUE.equals(mIgnores.get())) {
            return;
        }
        mIgnores.set(true);
        try {
            Integer depth = mDepths.get();
            if (depth == null) {
                depth = -1;
            }
            mDepths.set(++depth);
            StringBuilder sBuilder = new StringBuilder();
            while (depth-- > 0) {
                sBuilder.append("  ");
            }
            String name = registers.func.getDeclaringClass().getName();
            if (!name.equals(mRecords.get())) {
                mRecords.set(name);
                sBuilder.append(name);
            } else {
                for (int i = 0; i < name.length(); i++) {
                    sBuilder.append(i < 3 ? "." : " ");
                }
            }
            if (registers.func instanceof Method) {
                sBuilder.append(".").append(registers.func.getName());
            }
            sBuilder.append("(");
            if (registers.self != null) {
                sBuilder.append("@").append(Integer.toHexString(registers.self.hashCode()));
            } else {
                sBuilder.append("@null");
            }
            if (registers.args != null) {
                for (Object argObject : registers.args) {
                    sBuilder.append(", ");
                    sBuilder.append(mFormatter.toString(argObject));
                }
            }
            sBuilder.append(")");
            if (registers.func instanceof Constructor) {
                Log.w(currentTag(), sBuilder.toString().replace('\n', '\\'));
            } else {
                Log.i(currentTag(), sBuilder.toString().replace('\n', '\\'));
            }
        } finally {
            mIgnores.set(false);
        }
    }


    @Override
    protected void afterExecuted(Registers registers) {
        if (Boolean.TRUE.equals(mIgnores.get())) {
            return;
        }
        mIgnores.set(true);
        mRecords.set(null);
        try {
            Integer depth = mDepths.get();
            mDepths.set(depth - 1);
            StringBuilder sBuilder = new StringBuilder();
            while (depth-- > 0) {
                sBuilder.append("  ");
            }
            if (!registers.hasThrown()) {
                if (registers.func instanceof Constructor) {
                    sBuilder.append(">>> ").append(mFormatter.toString(registers.self));
                } else if (registers.func instanceof Method) {
                    if (((Method) registers.func).getReturnType() == void.class) {
                        sBuilder.append(">>>");
                    } else {
                        sBuilder.append(">>> ").append(mFormatter.toString(registers.getReturned()));
                    }
                }
                Log.i(currentTag(), sBuilder.toString().replace('\n', '\\'));
            } else {
                sBuilder.append("<<< ").append(mFormatter.toString(registers.getThrown()));
                Log.e(currentTag(), sBuilder.toString().replace('\n', '\\'));
            }
        } finally {
            mIgnores.set(false);
        }
    }
}
