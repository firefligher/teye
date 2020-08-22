package org.fir3.teye.ui.renderer;

import lombok.Data;
import lombok.experimental.Accessors;
import org.fir3.teye.ui.Modification;

@Data
@Accessors(fluent = true, chain = true)
public final class ElementModification implements Modification {
    private Object previousValue, newValue;
    private ElementAttribute modifiedAttribute;

    /**
     * This reference is required to attach the next neighbour to this object.
     *
     * This is only used when this instance is not in use and stored in the
     * pool.
     */
    ElementModification next;

    @Override
    public void reset() {
        this.previousValue = null;
        this.newValue = null;
        this.modifiedAttribute = null;
    }

    public int previousValueAsInt() {
        return (int) this.previousValue;
    }

    public int newValueAsInt() {
        return (int) this.newValue;
    }

    public Texture previousValueAsTexture() {
        return (Texture) this.previousValue;
    }

    public Texture newValueAsTexture() {
        return (Texture) this.newValue;
    }
}
