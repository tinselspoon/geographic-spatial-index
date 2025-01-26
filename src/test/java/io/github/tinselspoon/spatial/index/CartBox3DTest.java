package io.github.tinselspoon.spatial.index;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Unit tests for {@link CartBox3D}. */
class CartBox3DTest {

    @Test
    void testDistanceAround() {
        final CartPoint3D point = new CartPoint3D(1, 2, 3);
        assertEquals(new CartBox3D(new CartPoint3D(0, 1, 2), new CartPoint3D(2, 3, 4)),
                     CartBox3D.distanceAround(point, CartPoint3D.WGS84_RADIUS_METERS));
    }

    @ParameterizedTest
    @CsvSource({ "1, 2, 3", "4, 5, 6", "3, 4, 5" })
    void testContains(final double x, final double y, final double z) {
        final CartBox3D box = new CartBox3D(new CartPoint3D(1, 2, 3), new CartPoint3D(4, 5, 6));
        final CartPoint3D point = new CartPoint3D(x, y, z);
        assertTrue(box.contains(point));
    }

    @ParameterizedTest
    @CsvSource({
            // ordinate too low
            "0, 2, 3", "1, 1, 3", "1, 2, 2",
            // ordinate too high
            "5, 5, 6", "4, 6, 6", "4, 5, 7" })
    void testNotContains(final double x, final double y, final double z) {
        final CartBox3D box = new CartBox3D(new CartPoint3D(1, 2, 3), new CartPoint3D(4, 5, 6));
        final CartPoint3D point = new CartPoint3D(x, y, z);
        assertFalse(box.contains(point));
    }
}
