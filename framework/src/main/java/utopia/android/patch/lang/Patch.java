package utopia.android.patch.lang;

import java.lang.reflect.Member;

public abstract class Patch extends Sortable {
    public Patch() {
        super();
    }

    public Patch(int priority) {
        super(priority);
    }

    /**
     * Before execute a constructor or a method.
     *
     * @param registers executing info
     */
    protected void beforeExecuted(Registers registers) {
    }

    /**
     * After executed a constructor or a method.
     *
     * @param registers executing info
     */
    protected void afterExecuted(Registers registers) {
    }

    public final static class Registers {
        static final Object NULL = new Object(); //meaning will executing original

        public final Member func; //constructor or method
        public final Object self; //this object
        public final Object[] args; //parameters
        volatile Object returned;
        volatile Throwable thrown;

        Registers(Member member, Object object, Object[] objects) {
            func = member;
            self = object;
            args = objects;
            returned = NULL;
            thrown = null;
        }

        /**
         * Has returned.
         *
         * @return has or not
         */
        public boolean hasReturned() {
            return returned != NULL;
        }

        /**
         * Get returned.
         *
         * @return returning object
         */
        public Object getReturned() {
            Object ret = returned;
            if (ret == NULL) {
                ret = null;
            }
            return ret;
        }

        /**
         * Set returned.
         *
         * @param object returning object
         */
        public void setReturned(Object object) {
            if (object == NULL) {
                throw new IllegalArgumentException("cannot set returned to null");
            }
            returned = object;
            thrown = null;
        }

        /**
         * Has thrown.
         *
         * @return has or not
         */
        public boolean hasThrown() {
            return thrown != null;
        }

        /**
         * Get thrown.
         *
         * @return throwing throwable
         */
        public Throwable getThrown() {
            return thrown;
        }

        /**
         * Set thrown.
         *
         * @param throwable throwing throwable
         */
        public void setThrown(Throwable throwable) {
            if (throwable == null) {
                throw new IllegalArgumentException("cannot set thrown to null");
            }
            returned = NULL;
            thrown = throwable;
        }
    }
}
