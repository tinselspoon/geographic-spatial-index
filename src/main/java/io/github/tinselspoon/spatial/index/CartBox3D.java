package io.github.tinselspoon.spatial.index;

/**
 * Represents a three-dimensional envelope or bounding box.
 *
 * @param lower the coordinate representing the minimum range of the box.
 * @param upper the coordinate representing the maximum range of the box.
 */
record CartBox3D(CartPoint3D lower, CartPoint3D upper) {

    /**
     * Constructs a {@code GeoBox3D} that by expanding the given point on all sides by the given WGS84 distance.
     *
     * @param point the point to base the box around.
     * @param distanceMeters the distance by which to expand the box in all directions.
     * @return a new {@code GeoBox3D}.
     */
    static CartBox3D distanceAround(final CartPoint3D point, final double distanceMeters) {
        // Normalise the distance to be that on a unit sphere
        final double distance = distanceMeters / CartPoint3D.WGS84_RADIUS_METERS;
        return new CartBox3D(new CartPoint3D(point.x() - distance, point.y() - distance, point.z() - distance),
                             new CartPoint3D(point.x() + distance, point.y() + distance, point.z() + distance));
    }

    /**
     * Determines whether the range represented by this {@code GeoBox3D} would include the given point.
     *
     * @param point the point to test.
     * @return {@code true} if the point is inside or is touching the boundary of the box; otherwise, {@code false}.
     */
    public boolean contains(final CartPoint3D point) {
        return point.x() >= lower.x() && point.x() <= upper.x() //
                && point.y() >= lower.y() && point.y() <= upper.y() //
                && point.z() >= lower.z() && point.z() <= upper.z();
    }
}
