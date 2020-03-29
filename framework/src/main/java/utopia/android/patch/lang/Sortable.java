package utopia.android.patch.lang;

public class Sortable implements Comparable<Sortable> {
    final int priority; //priority

    public Sortable() {
        this.priority = 0;
    }

    public Sortable(int priority) {
        this.priority = priority;
    }

    @Override
    public final int compareTo(Sortable other) {
        if (other == null) {
            return -1;
        }
        if (other == this) {
            return 0;
        }
        if (other.priority != priority) {
            return other.priority > priority ? 1 : -1;
        } else {
            int hash1, hash2;
            hash1 = System.identityHashCode(getClass().getClassLoader());
            hash2 = System.identityHashCode(other.getClass().getClassLoader());
            if (hash1 != hash2) {
                return hash1 > hash2 ? 1 : -1;
            }
            hash1 = System.identityHashCode(this);
            hash2 = System.identityHashCode(other);
            if (hash1 != hash2) {
                return hash1 > hash2 ? 1 : -1;
            }
            return 0;
        }
    }
}
