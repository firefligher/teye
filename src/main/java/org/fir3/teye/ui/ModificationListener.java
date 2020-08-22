package org.fir3.teye.ui;

/**
 * The interface of a listener that listens for modifications of a group of
 * {@link Modifiable}s.
 *
 * @param <M>   The type of the group's members.
 * @param <T>   The type of the modification that may occur.
 */
public interface ModificationListener<
        M extends Modifiable<M, T>,
        T extends Modification> {
    /**
     * Notifies this listener that the specified <code>modified</code> object
     * has been modified.
     *
     * After calling this method, the specified <code>modification</code>
     * instance should not be modified anymore. Otherwise undefined behavior
     * may be expected.
     *
     * @param modified          The modified object
     * @param modification      The modification that the <code>modified</code>
     *                          object experienced. After the modification has
     *                          been processed, this
     *                          {@link ModificationListener} discard it, or
     *                          reuse it after calling its
     *                          {@link Modification#reset()} method.
     *
     * @throws IllegalArgumentException If the passed <code>modified</code> is
     *                                  unknown to this
     *                                  {@link ModificationListener}.
     *
     * @throws NullPointerException     If the passed <code>modified</code>
     *                                  object is <code>null</code>, or, if the
     *                                  passed <code>modification</code> is
     *                                  <code>null</code> and that is invalid
     *                                  for this kind of {@link Modifiable}s.
     */
    void notifyModified(M modified, T modification);

    /**
     * Returns a new modification object that may be used for invoking the
     * {@link #notifyModified(Modifiable, Modification)} method.
     *
     * You should always prefer this mechanism over creating new instances of
     * the corresponding {@link Modification} implementation, because the
     * {@link ModificationListener} implementation may recycle those instances
     * after processing its content.
     *
     * To prevent a memory leak, instances that have been obtained by this
     * method should be always used for calling
     * {@link #notifyModified(Modifiable, Modification)} or
     * {@link #releaseModification(Modification, boolean)}.
     *
     * @return  A new modification object.
     */
    T newModification();

    /**
     * Tells the listener that the specified <code>modification</code> is no
     * longer in use without performing a modification notification.
     *
     * If you've called {@link #notifyModified(Modifiable, Modification)}, you
     * do not need to call this method.
     *
     * @param modification  The modification object that is no longer in use.
     * @param reuse         Whether the passed <code>modification</code> object
     *                      may be reused or must be destroyed.
     *                      This may be <code>true</code>, if it cannot be
     *                      ensured that no third-party may modify the passed
     *                      <code>modification</code> object in the future.
     */
    void releaseModification(T modification, boolean reuse);
}
