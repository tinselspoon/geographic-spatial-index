package io.github.tinselspoon.spatial.index;

/**
 * Represents a geographic coordinate in cartesian space on the unit sphere.
 *
 * @param x the x-axis ordinate.
 * @param y the y-axis ordinate.
 * @param z the z-axis ordinate.
 */
record CartPoint3D(double x, double y, double z) {

    /** The number of ordinates within this point. */
    static int DIMENSIONS = 3;

    /** The mean earth radius as used in the WGS84 coordinate system in meters, {@code 6,371km}. */
    static double WGS84_RADIUS_METERS = 6371e3;

    /**
     * Converts the given polar latitude and longitude coordinates to cartesian coordinates on the unit sphere.
     *
     * @param latitude the latitude in degrees, [-90, +90].
     * @param longitude the longitude in degrees, [-180, +180].
     * @return a cartesian point.
     * @throws IllegalArgumentException if input coordinates are invalid or out of range.
     */
    static CartPoint3D fromLatLon(final double latitude, final double longitude) {
        // The inversion here is because it allows us to implicitly also check for NaNs
        if (!(latitude <= 90 && latitude >= -90)) {
            throw new IllegalArgumentException("Latitude out of range: " + longitude);
        }

        if (!(longitude <= 180 && longitude >= -180)) {
            throw new IllegalArgumentException("Longitude out of range: " + longitude);
        }

        // Adapted from https://github.com/postgis/postgis/blob/master/liblwgeom/lwgeodetic.c#L423
        final double latRadians = Math.toRadians(latitude);
        final double lonRadians = Math.toRadians(longitude);

        final double cosLat = Math.cos(latRadians);
        final double x = cosLat * Math.cos(lonRadians);
        final double y = cosLat * Math.sin(lonRadians);
        final double z = Math.sin(latRadians);
        return new CartPoint3D(x, y, z);
    }

    /**
     * Calculate the great-circle distance in meters to the destination point.
     *
     * @param destination the target point.
     * @return the calculated distance.
     */
    double distanceMeters(final CartPoint3D destination) {
        // Since we have cartesian coordinates we can work this out as the arc length of a chord
        final double dX = destination.x() - this.x();
        final double dY = destination.y() - this.y();
        final double dZ = destination.z() - this.z();
        final double hypot = Math.sqrt(dX * dX + dY * dY + dZ * dZ);

        return 2 * WGS84_RADIUS_METERS * Math.asin(hypot / 2);
    }

    /**
     * Gets the component part of the coordinate at the specified dimension index.
     *
     * @param dimension {@code 0} for {@code x}, {@code 1} for {@code y}, {@code 2} for {@code z}.
     * @return the value of the ordinate specified.
     * @throws IllegalArgumentException if {@code dimension} is not one of the listed values.
     */
    double getOrdinate(int dimension) {
        return switch (dimension) {
            case 0 -> x;
            case 1 -> y;
            case 2 -> z;
            default -> throw new IllegalArgumentException("Dimension index out of range: " + dimension);
        };
    }
}
