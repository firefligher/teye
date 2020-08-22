package org.fir3.teye.ui.renderer;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.fir3.teye.ui.renderer.TestUtil.createDummyAtlas;
import static org.fir3.teye.ui.renderer.TestUtil.createDummyTexture;
import static org.junit.jupiter.api.Assertions.*;

public class AbstractTextureAtlasTest {
    @Test
    public void testConstructorArgumentValidation() {
        // Invalid width

        assertThrows(IllegalArgumentException.class,
                () -> new DummyTextureAtlas(
                        0,
                        TestUtil.DEFAULT_ATLAS_HEIGHT));

        // Invalid height

        assertThrows(IllegalArgumentException.class,
                () -> new DummyTextureAtlas(
                        TestUtil.DEFAULT_ATLAS_WIDTH,
                        0));

        // Only powers of two are allowed for both, width and height

        assertThrows(IllegalArgumentException.class,
                () -> new DummyTextureAtlas(
                        135,
                        TestUtil.DEFAULT_ATLAS_HEIGHT));

        assertThrows(IllegalArgumentException.class,
                () -> new DummyTextureAtlas(
                        TestUtil.DEFAULT_ATLAS_WIDTH,
                        135));
    }

    //
    // ******************************* INSERTING ******************************
    //

    @Test
    public void testInsertingFragment() {
        DummyTextureAtlas atlas = createDummyAtlas();
        DummyTexture fragment = createDummyTexture();

        // Inserting a fitting fragment into an empty atlas should always
        // succeed.

        assertTrue(atlas.insert(fragment));

        // Double-inserting the same fragment should also succeed (also if the
        // atlas must not be changed at all).

        assertTrue(atlas.insert(fragment));
    }

    @Test
    public void testInsertingForeignFragment() {
        DummyTextureAtlas atlas1 = createDummyAtlas();
        DummyTextureAtlas atlas2 = createDummyAtlas();
        DummyTexture fragment = createDummyTexture();

        // While the first insertion should succeed, the second one should fail
        // with an exception because the fragment has already been assigned to
        // another atlas.

        assertTrue(atlas1.insert(fragment));
        assertThrows(IllegalArgumentException.class,
                () -> atlas2.insert(fragment));
    }

    @Test
    public void testInsertingTooBigFragment() {
        DummyTextureAtlas atlas = createDummyAtlas(4, 4);
        DummyTexture fragment = createDummyTexture(10, 10);

        assertFalse(atlas.insert(fragment));
    }

    @Test
    public void testInsertingFull() {
        DummyTextureAtlas atlas = createDummyAtlas(8, 8);
        DummyTexture fragment1 = createDummyTexture(8, 8);
        DummyTexture fragment2 = createDummyTexture(8, 8);

        // Inserting the first fragment should succeed, because the atlas
        // provides enough empty space. The second operation should fail,
        // because the atlas has been totally occupied by the first fragment.

        assertTrue(atlas.insert(fragment1));
        assertFalse(atlas.insert(fragment2));
    }

    @Test
    public void testContainingFragment() {
        DummyTextureAtlas atlas = createDummyAtlas();
        DummyTexture fragment1 = createDummyTexture();
        DummyTexture fragment2 = createDummyTexture();
        DummyTexture fragment3 = createDummyTexture();
        DummyTexture fragment4 = createDummyTexture();

        // Inserting the three fragments should work as expected (that's not
        // what we're testing here)

        assertTrue(atlas.insert(fragment1));
        assertTrue(atlas.insert(fragment2));
        assertTrue(atlas.insert(fragment3));

        // Now, testing if the atlas contains a fragment should succeed for
        // fragment1-3, but fail for fragment4

        assertTrue(atlas.contains(fragment1));
        assertTrue(atlas.contains(fragment2));
        assertTrue(atlas.contains(fragment3));
        assertFalse(atlas.contains(fragment4));
    }

    @Test
    public void testXYQuery() {
        DummyTextureAtlas atlas = createDummyAtlas();
        DummyTexture fragment1 = createDummyTexture();
        DummyTexture fragment2 = createDummyTexture();

        assertTrue(atlas.insert(fragment1));

        // Querying the x- and y-coordinates of fragment1 should return a
        // non-negative value for both coordinates, while -1 should be returned
        // for fragment2.

        assertTrue(atlas.getX(fragment1) >= 0);
        assertTrue(atlas.getY(fragment1) >= 0);

        assertEquals(-1, atlas.getX(fragment2));
        assertEquals(-1, atlas.getY(fragment2));
    }

    @Test
    public void testOccupationQuery() {
        DummyTextureAtlas atlas = createDummyAtlas(1024, 1024);
        DummyTexture halfFilling = createDummyTexture(1024, 512);
        DummyTexture quarterFilling1 = createDummyTexture(512, 512);
        DummyTexture quarterFilling2 = createDummyTexture(512, 512);
        DummyTexture quarterFilling3 = createDummyTexture(512, 512);
        DummyTexture quarterFilling4 = createDummyTexture(512, 512);

        // Since nothing has been added to the atlas yet, the occupation should
        // be zero.

        assertEquals(0.0F, atlas.getOccupation());

        // Adding a rectangle that is half the size of the atlas should
        // increase the occupation to 50%.

        assertTrue(atlas.insert(halfFilling));
        assertEquals(0.5F, atlas.getOccupation(), 0.001F);

        // Adding a quarter filling fragment should increase the occupation to
        // 75%.

        assertTrue(atlas.insert(quarterFilling1));
        assertEquals(0.75F, atlas.getOccupation(), 0.001F);

        // Removing the half filling fragment and adding three other quarter
        // filling textures should totally occupy the atlas.

        atlas.free(halfFilling);

        assertTrue(atlas.insert(quarterFilling2));
        assertTrue(atlas.insert(quarterFilling3));
        assertTrue(atlas.insert(quarterFilling4));

        assertEquals(1.0F, atlas.getOccupation());
    }

    @Test
    public void testComputeBorderRatio() {
        DummyTextureAtlas atlas = createDummyAtlas(128, 128);

        // Since inserting a fragment with the same size of the atlas would
        // lead to no border, the border ratio should be zero.

        assertEquals(0.0F, atlas.computeBorderRatio(128, 128));

        // Inserting a fragment of one pixel should lead to the biggest
        // possible border ratio.

        assertEquals(16383F, atlas.computeBorderRatio(1, 1));

        // Attempting to insert a fragment that exceeds the sizes of the atlas
        // should fail with a negative return value.

        assertTrue(atlas.computeBorderRatio(1000, 1000) < 0.0F);
    }

    @Test
    public void testTextureAdjustment() {
        DummyTextureAtlas atlas = createDummyAtlas();
        DummyTexture fragment = createDummyTexture();

        // Precondition check

        assertNotNull(fragment.getData());
        assertFalse(fragment.getX() >= 0);
        assertFalse(fragment.getY() >= 0);

        // If we add the fragment to the atlas, the AbstractTextureAtlas
        // implementation needs to adjust the data and x-/y-coordinates of the
        // fragment.

        assertTrue(atlas.insert(fragment));
        assertNull(fragment.getData());
        assertTrue(fragment.getX() >= 0);
        assertTrue(fragment.getY() >= 0);

        // If we remove the fragment from the atlas, the implementation should
        // restore the original state.

        atlas.free(fragment);
        assertNotNull(fragment.getData());
        assertFalse(fragment.getX() >= 0);
        assertFalse(fragment.getY() >= 0);
    }

    //
    // ********************* ADVANCED FRAGMENTATION TESTS *********************
    //

    @Disabled("Disabled until bug #1 has been fixed")
    @Test
    public void testRestoreEmpty() {
        DummyTextureAtlas atlas = createDummyAtlas(100, 100);
        DummyTexture fragment1 = createDummyTexture(17, 23);
        DummyTexture fragment2 = createDummyTexture(95, 3);
        DummyTexture fragment3 = createDummyTexture(50, 50);
        DummyTexture fragment4 = createDummyTexture(100, 100);

        // We test, if adding and removing crooked fragments restores the
        // atlas' whole capacity

        assertTrue(atlas.insert(fragment1));
        assertTrue(atlas.insert(fragment2));
        assertTrue(atlas.insert(fragment3));

        atlas.free(fragment1);
        atlas.free(fragment2);
        atlas.free(fragment3);

        assertTrue(atlas.insert(fragment4));
    }
}
