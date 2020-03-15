package utopia.android.patch.lang;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Member;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * First, startup plugin app.
 * Create class loaders, and use {@link #initPatchLoader} to create patch loader instances, recommend calling in Zygote process.
 * When a child process is forked, use {@link #initPatch} to notify all patch loader instances.
 * <p>
 * Second, provide hook ability.
 * After called {@link #hookMember}, when member be executed you need calling {@link #replaceHookedMember} except be executed by {@link #invokeOriginalMember}.
 */
public abstract class ExternalKernel {
    public static class MemberStruct {
        private final Member mMember;
        private final CopyOnWriteSortedSet<Patch> mPatches;

        public MemberStruct(Member member) {
            mMember = member;
            mPatches = new CopyOnWriteSortedSet<>();
        }

        public final Member getMember() {
            return mMember;
        }

        final CopyOnWriteSortedSet<Patch> getPatches() {
            return mPatches;
        }
    }

    private final String mName;
    private final List<PatchLoader> mPatchLoaders;
    private final Map<Member, MemberStruct> mMemberMapping;

    protected ExternalKernel(String name) {
        mName = name;
        mPatchLoaders = new LinkedList<>();
        mMemberMapping = new HashMap<>();
        Patchwork.provide(this);
    }

    String getName() {
        return mName;
    }

    protected void initPatchLoader(ClassLoader classLoader) {
        List<String> classNames = new LinkedList<>();
        InputStream inputStream = classLoader.getResourceAsStream("assets/patch_loader");
        if (inputStream != null) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    classNames.add(line);
                }
            } catch (IOException e) {
                wtf(e);
            } finally {
                try {
                    inputStream.close();
                } catch (IOException ignore) {
                }
            }
        }
        if (classNames.isEmpty()) {
            classNames.add("patch.Loader");
        }
        for (String className : classNames) {
            PatchLoader patchLoader;
            try {
                patchLoader = (PatchLoader) classLoader.loadClass(className).newInstance();
            } catch (Throwable t) {
                wtf(t);
                continue;
            }
            synchronized (mPatchLoaders) {
                mPatchLoaders.add(patchLoader);
            }
        }
    }

    protected void initPatch() {
        Object[] patchLoaders;
        synchronized (mPatchLoaders) {
            patchLoaders = mPatchLoaders.toArray();
        }
        for (Object patchLoader : patchLoaders) {
            try {
                ((PatchLoader) patchLoader).loadPatch();
            } catch (Throwable t) {
                wtf(t);
            }
        }
    }

    void addPatch(Member member, Patch patch) throws Throwable {
        MemberStruct struct;
        synchronized (mMemberMapping) {
            struct = mMemberMapping.get(member);
            if (struct == null) {
                struct = hookMember(member);
                mMemberMapping.put(member, struct);
            }
        }
        struct.getPatches().add(patch);
    }

    void removePatch(Member member, Patch patch) {
        MemberStruct struct;
        synchronized (mMemberMapping) {
            struct = mMemberMapping.get(member);
        }
        if (struct != null) {
            struct.getPatches().remove(patch);
        }
    }

    protected abstract MemberStruct hookMember(Member member) throws Throwable;

    protected Object replaceHookedMember(MemberStruct struct, Object object, Object[] objects) throws Throwable {
        Object[] patches = struct.getPatches().getSnapshot();
        int patchesLength = patches.length;
        if (patchesLength <= 0) {
            return invokeOriginalMember(struct, object, objects);
        }
        Patch.Registers registers = new Patch.Registers(struct.getMember(), object, objects);
        int index = 0;
        do {
            try {
                ((Patch) patches[index]).beforeExecuted(registers);
            } catch (Throwable t) {
                wtf(t);
            }
        } while (++index < patchesLength);
        if (registers.returned == Patch.Registers.NULL && registers.thrown == null) {
            try {
                registers.returned = invokeOriginalMember(struct, object, objects);
            } catch (Throwable t) {
                registers.thrown = t;
            }
        }
        index--;
        do {
            try {
                ((Patch) patches[index]).afterExecuted(registers);
            } catch (Throwable t) {
                wtf(t);
            }
        } while (--index >= 0);
        Throwable thrown = registers.thrown;
        if (thrown != null) {
            throw thrown;
        }
        return registers.returned;
    }

    protected abstract Object invokeOriginalMember(MemberStruct struct, Object object, Object[] objects) throws Throwable;

    protected void wtf(Throwable t) {
        if (t != null) {
            t.printStackTrace();
        }
    }
}

class CopyOnWriteSortedSet<E> {
    private static final Object[] EMPTY_ARRAY = new Object[0];
    private volatile Object[] elements = EMPTY_ARRAY;

    public synchronized boolean add(E e) {
        int index = indexOf(e);
        if (index >= 0) {
            return false;
        }
        Object[] newElements = new Object[elements.length + 1];
        System.arraycopy(elements, 0, newElements, 0, elements.length);
        newElements[elements.length] = e;
        Arrays.sort(newElements);
        elements = newElements;
        return true;
    }

    public synchronized boolean remove(E e) {
        int index = indexOf(e);
        if (index == -1) {
            return false;
        }
        Object[] newElements = new Object[elements.length - 1];
        System.arraycopy(elements, 0, newElements, 0, index);
        System.arraycopy(elements, index + 1, newElements, index, elements.length - index - 1);
        elements = newElements;
        return true;
    }

    private int indexOf(Object o) {
        for (int i = 0; i < elements.length; i++) {
            if (o.equals(elements[i])) {
                return i;
            }
        }
        return -1;
    }

    public Object[] getSnapshot() {
        return elements;
    }
}
