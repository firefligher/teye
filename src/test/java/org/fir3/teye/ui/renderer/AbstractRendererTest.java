package org.fir3.teye.ui.renderer;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class AbstractRendererTest {
    @Test
    public void testConstructorArgumentValidation() {
        // oversizeFactor is negative (invalid)

        assertThrows(IllegalArgumentException.class,
                () -> new DummyRenderer(-1.0F));
    }

    @Test
    public void testElementModificationCaching() {
        DummyRenderer renderer = new DummyRenderer(1.0F);
        Set<ElementModification> modificationInstances =
                Collections.newSetFromMap(new IdentityHashMap<>());

        int elementCount = 100;

        // Creating elements

        for (int i = 0; i < elementCount; i++) {
            renderer.newElement();
        }

        // Now, we need to trigger the ElementModification cache mechanism.
        //
        // Since the oversizeFactor is 1.0, it well cache the same number of
        // ElementModification instances as elements exist.

        for (int i = 0; i < (elementCount + 50); i++) {
            modificationInstances.add(renderer.newModification());
        }

        for (ElementModification mod : modificationInstances) {
            renderer.releaseModification(mod, true);
        }

        // Now, the cache should contain exactly elementCount
        // ElementModification instances.

        for (int i = 0; i < elementCount; i++) {
            assertTrue(modificationInstances.contains(
                    renderer.newModification()));
        }

        // Any further allocation of a modification object should create a new
        // ElementModification instance

        for (int i = 0; i < 50; i++) {
            assertFalse(modificationInstances.contains(
                    renderer.newModification()));
        }
    }
}
