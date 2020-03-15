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
            return System.identityHashCode(other) > System.identityHashCode(this) ? 1 : -1;
        }
    }
}
