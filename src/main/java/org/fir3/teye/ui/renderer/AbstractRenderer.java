package org.fir3.teye.ui.renderer;

import org.fir3.teye.ui.AbstractModifiable;
import org.fir3.teye.ui.Modification;
import org.fir3.teye.ui.ModificationListener;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class AbstractRenderer<E extends AbstractElement<E>>
        extends AbstractModifiable<
            Renderer<Modification.NullModification>,
            Modification.NullModification>
        implements Renderer<Modification.NullModification>,
            ModificationListener<E, ElementModification> {
    private static final float DEFAULT_OVERSIZE_FACTOR = 1.5F;

    private final float oversizeFactor;
    private final Lock poolLock;
    private final AtomicInteger elementCount;
    private final AtomicInteger poolCount;

    private ElementModification firstPoolEntry;

    /**
     * Creates a new instance.
     *
     * @param oversizeFactor    This value multiplied with the number of
     *                          elements is the maximum number of cached
     *                          {@link ElementModification} instances.
     *
     * @throws IllegalArgumentException If the specified
     *                                  <code>oversizeFactor</code> is less
     *                                  than zero.
     */
    protected AbstractRenderer(float oversizeFactor) {
        if (oversizeFactor < 0.0F)
            throw new IllegalArgumentException("Invalid oversizeFactor!");

        this.oversizeFactor = oversizeFactor;
        this.poolLock = new ReentrantLock();
        this.elementCount = new AtomicInteger();
        this.poolCount = new AtomicInteger();
    }

    protected AbstractRenderer() {
        this(AbstractRenderer.DEFAULT_OVERSIZE_FACTOR);
    }

    @Override
    public final void notifyModified(E modified, ElementModification modification) {
        if (!this.notifyModified0(modified, modification))
            return;

        // We need to notify the ModificationListener that we want to re-render
        // the current scene.

        this.notifyModified();
        this.releaseModification(modification, true);
    }

    @Override
    public ElementModification newModification() {
        this.poolLock.lock();

        try {
            if (this.firstPoolEntry != null) {
                ElementModification result = this.firstPoolEntry;
                this.firstPoolEntry = result.next;
                result.next = null;

                this.poolCount.decrementAndGet();
                return result;
            }
        } finally {
            this.poolLock.unlock();
        }

        return new ElementModification();
    }

    @Override
    public void releaseModification(
            ElementModification modification,
            boolean reuse) {
        if (!reuse)
            return;

        if (this.poolCount.get() >=
                this.elementCount.get() * this.oversizeFactor) {
            return;
        }

        modification.reset();

        // Adding the instance back to the pool

        this.poolLock.lock();

        try {
            modification.next = this.firstPoolEntry;
            this.firstPoolEntry = modification;
        } finally {
            this.poolLock.unlock();
        }

        this.poolCount.incrementAndGet();
    }

    @Override
    public final Element newElement() {
        Element result = this.newElement0();
        this.elementCount.incrementAndGet();
        return result;
    }

    @Override
    public final void release(Element element) {
        this.release0(element);
        this.elementCount.decrementAndGet();
    }

    protected void notifyModified() {
        this.notifyModified(Modification.NullModification.INSTANCE);
    }

    // NOTE:    Some methods are marked final in the AbstractRenderer and there
    //          is a *0-method with the same return type and arguments. In this
    //          case, we do want that the actual implementing class is required
    //          to put there some logic.

    protected abstract Element newElement0();
    protected abstract void release0(Element element);

    /**
     * Handles the passed {@link ElementModification} in the actual
     * implementation.
     *
     * @param modified      The {@link Element} instance that has been
     *                      modified.
     *
     * @param modification  The modification that the passed
     *                      <code>modified</code> {@link Element} experienced.
     *
     * @return  Either <code>true</code>, which indicates that the passed
     *          <code>modification</code> is no longer in use and may be
     *          returned to the internal pool, otherwise <code>false</code>.
     *          If <code>false</code>, then the implementation has to take care
     *          of calling
     *          {@link #releaseModification(ElementModification, boolean)}
     *          manually and notifying the {@link ModificationListener} to
     *          re-render the current scene.
     */
    protected abstract boolean notifyModified0(E modified, ElementModification modification);
}
