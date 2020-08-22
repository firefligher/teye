package org.fir3.teye.ui;

/**
 * The interface of an object that notifies its {@link ModificationListener},
 * if itself has been modified.
 *
 * If a {@link Modifiable} is being modified, but no or a <code>null</code>
 * {@link ModificationListener} has been specified by a previous
 * {@link #setModificationListener(ModificationListener)} call, the object
 * shall do nothing.
 *
 * @param <M>   The modifiable itself.
 * @param <T>   The type of the modifications that may be reported to the
 *              {@link ModificationListener}.
 */
public interface Modifiable<
        M extends Modifiable<M, T>,
        T extends Modification> {
    /**
     * Sets the listener of this {@link Modifiable}.
     *
     * @param listener  The new listener is the target of modification
     *                  notifications.
     */
    void setModificationListener(ModificationListener<M, T> listener);
}
