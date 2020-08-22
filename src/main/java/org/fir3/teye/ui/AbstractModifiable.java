package org.fir3.teye.ui;

import java.util.*;

/**
 * An abstract implementation of the {@link Modifiable} interface that handles
 * setting the {@link ModificationListener} and keeps track of the obtained
 * {@link Modification} objects.
 *
 * If the {@link ModificationListener} is being replaced while there are
 * obtained {@link Modification} instances, the implementation will release the
 * {@link Modification} instances using the old {@link ModificationListener}
 * and discarding any attempt to notify the new {@link ModificationListener}
 * with {@link Modification} instances of the old {@link ModificationListener}.
 *
 * @param <M>   The modifiable itself.
 * @param <T>   The type of the modifications that may be reported to the
 *              {@link ModificationListener}.
 */
public abstract class AbstractModifiable<
        M extends Modifiable<M, T>,
        T extends Modification>
        implements Modifiable<M, T> {
    private final Set<T> obtainedModifications;
    private ModificationListener<M, T> listener;

    protected AbstractModifiable() {
        this.obtainedModifications = Collections.newSetFromMap(
                new IdentityHashMap<>());
    }

    @Override
    public void setModificationListener(ModificationListener<M, T> listener) {
        // NOTE:    We need to synchronize on the obtainedModifications list
        //          because otherwise somebody could try to obtain a
        //          modification instance while we change the listener.

        synchronized (this.obtainedModifications) {
            // If there modification objects that had been obtained but never
            // have been used to notify the listener, we need to return them to
            // the listener to prevent memory leaks.

            if (!this.obtainedModifications.isEmpty()) {
                if (this.listener == null)
                    throw new IllegalStateException(
                            "obtainedModifications, but listener null!");

                Iterator<T> mods = this.obtainedModifications.iterator();

                while (mods.hasNext()) {
                    mods.next();
                    mods.remove();
                }
            }

            this.listener = listener;
        }
    }

    /**
     * Notifies the assigned {@link ModificationListener} that this instance
     * has been modified.
     *
     * If there no {@link ModificationListener} has been assigned to this
     * instance, this method will have no effect at all.
     *
     * @param modification  The modification that this instance experienced.
     */
    @SuppressWarnings("unchecked")
    protected void notifyModified(T modification) {
        // NOTE:    We need to synchronize on the obtainedModifications set
        //          because otherwise we cannot ensure that the assigned
        //          ModificationListener does not change while we notify it.

        synchronized (this.obtainedModifications) {
            if (this.listener == null)
                return;

            // The following check ensures that the ModificationListener has
            // not been replaced while the modification was prepared.

            if (!this.obtainedModifications.contains(modification))
                return;

            this.listener.notifyModified((M) this, modification);
            this.obtainedModifications.remove(modification);
        }
    }

    /**
     * Notifies the assigned {@link ModificationListener} that this instance
     * has been modified, if the <code>previousValue</code> is not equal to the
     * <code>newValue</code>.
     *
     * The equality check is performed by
     * {@link Objects#equals(Object, Object)}.
     *
     * @param previousValue The previous value.
     * @param newValue      The new value.
     * @param modification  The modification that this instance may have
     *                      experienced.
     *
     * @param <V>           The type of the values.
     */
    protected <V> void notifyIfModified(
            V previousValue, V newValue,
            T modification) {
        if (Objects.equals(previousValue, newValue))
            return;

        this.notifyModified(modification);
    }

    /**
     * Returns whether a {@link ModificationListener} has been assigned to this
     * instance or not.
     *
     * @return  Either <code>true</code>, if a {@link ModificationListener} has
     *          been assigned, otherwise <code>false</code>.
     */
    protected boolean hasListener() {
        return this.listener != null;
    }

    /**
     * Obtains a new modification object, or returns <code>null</code>, if
     * there is no {@link ModificationListener} assigned to this instance.
     *
     * You should always notify the assigned {@link ModificationListener} with
     * this object, otherwise memory leaks may occur.
     *
     * @return  The new modification object or <code>null</code>, if there is
     *          no assigned {@link ModificationListener}.
     */
    protected T obtainModification() {
        // NOTE:    We need to synchronize on the obtainedModifications set
        //          because otherwise the modificationListener may change while
        //          we obtain a modification.

        synchronized (this.obtainedModifications) {
            if (!this.hasListener())
                return null;

            T mod = this.listener.newModification();
            this.obtainedModifications.add(mod);

            return mod;
        }
    }
}
