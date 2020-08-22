package org.fir3.teye.ui;

/**
 * The interface of an object that is being passed from a {@link Modifiable}
 * instance to its {@link ModificationListener} to inform the
 * {@link ModificationListener} about the modification that the
 * {@link Modifiable} experienced.
 *
 * An instance of an implementation of this interface may be reusable after
 * calling its {@link #reset()} method, but this also depends on the
 * {@link ModificationListener} implementation.
 */
public interface Modification {
    /**
     * A {@link Modification} implementation that does not provide any data.
     */
    final class NullModification implements Modification {
        public static final NullModification INSTANCE = new NullModification();
        private NullModification() { }

        @Override
        public void reset() { }
    }

    /**
     * Resets this object and makes it available for reuse.
     */
    void reset();
}
