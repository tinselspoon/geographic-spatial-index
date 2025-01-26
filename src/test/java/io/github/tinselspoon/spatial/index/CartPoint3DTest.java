package io.github.tinselspoon.spatial.index;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Unit tests for {@link CartPoint3D}. */
class CartPoint3DTest {

    @ParameterizedTest
    @CsvSource({
            "51.4706, -0.461941, 40.639447, -73.779317, 5539696.120653",
            "-16.690599, -179.876999, -16.4667, 179.339996, 87081.41472344",
            "30, -140, 30, -140, 0" })
    void testDistance(final double lat1, final double lon1, final double lat2, final double lon2,
                      final double expectedDistance) {
        final CartPoint3D start = CartPoint3D.fromLatLon(lat1, lon1);
        final CartPoint3D end = CartPoint3D.fromLatLon(lat2, lon2);

        final double actualDistance = start.distanceMeters(end);
        assertEquals(expectedDistance, actualDistance, 1E-6);
    }

    @ParameterizedTest(name = "{0}, {1} => {2}, {3}, {4}")
    @CsvSource({
            "-90, -180, 0, 0, -1",
            "-90, 180, 0, 0, -1",
            "90, -180, 0, 0, 1",
            "90, 180, 0, 0, 1",
            "0, 0, 1, 0, 0",
            "-90, 0, 0, 0, -1",
            "90, 0, 0, 0, 1",
            "0, 180, -1, 0, 0",
            "0, -180, -1, 0, 0",
            "0, 90, 0, 1, 0",
            "0, -90, 0, -1, 0",
            "30, 60, 0.433012701892, 0.75, 0.5" })
    void testLatLonToCartesian(final double inputLatitude, final double inputLongitude, final double expectedX,
                               final double expectedY, final double expectedZ) {
        final CartPoint3D point = CartPoint3D.fromLatLon(inputLatitude, inputLongitude);

        assertAll(() -> assertEquals(expectedX, point.x(), 1E-12, "x"),
                  () -> assertEquals(expectedY, point.y(), 1E-12, "y"),
                  () -> assertEquals(expectedZ, point.z(), 1E-12, "z"));
    }

    @ParameterizedTest
    @CsvSource({ "-90.1, 0", "90.1, 0", "0, -180.1", "0, 180.1", "NaN, 0", "0, NaN" })
    void testInvalidLatLon(final double inputLatitude, final double inputLongitude) {
        assertThrows(IllegalArgumentException.class, () -> CartPoint3D.fromLatLon(inputLatitude, inputLongitude));
    }

}
