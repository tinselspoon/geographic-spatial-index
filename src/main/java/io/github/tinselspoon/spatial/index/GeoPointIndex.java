package io.github.tinselspoon.spatial.index;

import lombok.NonNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.ObjDoubleConsumer;
import java.util.function.ToDoubleFunction;

/**
 * A spatial index for data items that can each be represented as geographic WGS84 latitude-longitude points. Provides
 * functionality to query the index to find items that are within a specified distance to a given search point. The
 * index is immutable once constructed.
 * <p>
 * The index handles points in a geographic rather than a planar context, and as a result correctly handles issues such
 * as wrapping at the 180° antimeridian without requiring queries to be split.
 * <p>
 * Note that as with many such libraries distance calculations are based on a sphere and not a spheroid. This leads to
 * small inaccuracies of up to 0.56% at extremes (near a pole or meridional near the equator). If this extra precision
 * is needed, it is suggested to add a small percentage buffer to the given distance and then filter using a more
 * precise spheroid distance calculation on the returned items.
 *
 * @param <T> the type of items contained within the index.
 */
public class GeoPointIndex<T> {

    /** The underlying k-d tree that backs this index. */
    private final KDTree<T> tree;

    /**
     * Construct a new instance.
     *
     * @param tree the underlying k-d tree that backs this index.
     */
    private GeoPointIndex(final KDTree<T> tree) {
        this.tree = tree;
    }

    /**
     * Construct a new {@code PointIndex} that contains the specified items.
     *
     * @param items the items to represent in the index.
     * @param latitudeExtractor a function that will extract the latitude value from each of the {@code items}. This
     * must provide a value between -90 and +90.
     * @param longitudeExtractor a function that will extract the longitude value from each of the {@code items}, This
     * must provide a value between -180 and +180.
     * @param <T> the type of items in the index.
     * @return a new spatial index.
     */
    public static <T> GeoPointIndex<T> buildFrom(@NonNull final List<T> items,
                                                 @NonNull final ToDoubleFunction<T> latitudeExtractor,
                                                 @NonNull final ToDoubleFunction<T> longitudeExtractor) {
        return new GeoPointIndex<>(KDTree.create(items,
                                                 t -> CartPoint3D.fromLatLon(latitudeExtractor.applyAsDouble(t),
                                                                             longitudeExtractor.applyAsDouble(t))));
    }

    /**
     * Find all items contained in the index that are within the given great circle distance of the specified point.
     *
     * @param latitude the latitude of the point to search around.
     * @param longitude the longitude of the point to search around.
     * @param distanceMeters the distance in meters around the point for which to return items.
     * @param consumer a function that will be called upon finding each suitable item.
     * @return {@code true} if any items were found; otherwise, {@code false}.
     */
    public boolean queryWithinDistance(final double latitude, final double longitude, final double distanceMeters,
                                       @NonNull final WithinDistanceConsumer<T> consumer) {
        // This approach is adapted from PostGIS, which converts the latitude/longitude polar coordinates to cartesian
        // x/y/z coordinates to form a 3D bounding box, which is then expanded by the given distance in meters
        // (it also multiplies by a 1% "fudge factor" to account for sphere/spheroid calculation differences -
        // since we are not calculating spheroid distances, we will not do this here)
        // See https://github.com/postgis/postgis/blob/master/postgis/geography_measurement.c#L454
        final CartPoint3D searchPoint = CartPoint3D.fromLatLon(latitude, longitude);
        final CartBox3D range = CartBox3D.distanceAround(searchPoint, distanceMeters);
        final AtomicBoolean found = new AtomicBoolean();

        tree.rangeSearch(range, (item, point) -> {
            // The index is inexact; items returned are not guaranteed to be within the originally given distance
            // Calculate the distance and ensure it is within tolerance before handing it to the consumer
            final double distanceToSearchPoint = point.distanceMeters(searchPoint);
            if (distanceToSearchPoint <= distanceMeters) {
                consumer.accept(item, distanceToSearchPoint);
                found.set(true);
            }
        });

        return found.get();
    }

    /** Defines a function to iteratively receive the results of a distance query on the index. */
    @FunctionalInterface
    public interface WithinDistanceConsumer<T> extends ObjDoubleConsumer<T> {

        /**
         * Receive notification that a given item was located in the index.
         *
         * @param item the item that was found.
         * @param distanceMeters the great circle distance in meters from the search point to the item.
         */
        void accept(T item, double distanceMeters);
    }
}
