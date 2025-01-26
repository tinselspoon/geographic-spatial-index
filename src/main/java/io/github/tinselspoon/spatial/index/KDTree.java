package io.github.tinselspoon.spatial.index;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

/** Implementation of a k-d tree indexed by {@link CartPoint3D} with arbitrary associated data. */
class KDTree<T> {

    /** The root node of the tree. */
    private final KDNode<T> root;

    /**
     * Default constructor.
     *
     * @param root the root node of the tree.
     */
    private KDTree(final KDNode<T> root) {
        this.root = root;
    }

    /**
     * Build a {@code KDTree} instance containing the given items.
     *
     * @param items the items to store in the index.
     * @param extractor a function to extract a representative {@link CartPoint3D} for each item.
     * @param <T> the type of items in the index.
     * @return a constructed instance.
     */
    static <T> KDTree<T> create(final List<T> items, final Function<T, CartPoint3D> extractor) {
        final List<KDData<T>> data = items.stream().map(d -> new KDData<>(d, extractor.apply(d))).toList();
        return new KDTree<>(createNode(data, 0));
    }

    /**
     * Recursively build a tree of nodes to represent the given items.
     *
     * @param items the items to represent within the node.
     * @param depth the zero-based depth of the current level of the tree.
     * @param <T> the type of items in the index.
     * @return the node, or {@code null} if there are no items in the supplied list.
     */
    private static <T> KDNode<T> createNode(final List<KDData<T>> items, final int depth) {
        if (items.isEmpty()) {
            return null;
        }

        final List<KDData<T>> sorted = new ArrayList<>(items);
        sorted.sort(Comparator.comparing(d -> d.point().getOrdinate(depth % CartPoint3D.DIMENSIONS)));

        final int halfwayIndex = sorted.size() / 2;
        final KDData<T> currentData = sorted.get(halfwayIndex);
        final KDNode<T> leftNode = createNode(sorted.subList(0, halfwayIndex), depth + 1);
        final KDNode<T> rightNode = createNode(sorted.subList(halfwayIndex + 1, sorted.size()), depth + 1);

        return new KDNode<>(leftNode, rightNode, currentData);
    }

    /**
     * Search for all items having a {@link CartPoint3D} contained by the range described by the given
     * {@link CartBox3D}.
     *
     * @param range the range to search on.
     * @param callback a function to invoke for each item found.
     */
    void rangeSearch(final CartBox3D range, final BiConsumer<T, CartPoint3D> callback) {
        if (root != null) {
            root.rangeSearch(range, callback, 0);
        }
    }

    /**
     * A single node within the tree.
     *
     * @param left the child node on the left side, if any.
     * @param right the child node on the right side, if any.
     * @param data the item at this node.
     * @param <T> the type of data in the tree.
     */
    private record KDNode<T>(KDNode<T> left, KDNode<T> right, KDData<T> data) {

        /**
         * Find items that match the given range.
         *
         * @param range the range to search on.
         * @param callback the function to notify on finding a matching item.
         * @param depth the current depth within the tree we are searching.
         */
        private void rangeSearch(final CartBox3D range, final BiConsumer<T, CartPoint3D> callback, int depth) {
            final CartPoint3D point = data.point();
            if (range.contains(point)) {
                callback.accept(data.item(), point);
            }

            final int dimension = depth % CartPoint3D.DIMENSIONS;

            if (left != null) {
                final double ordinate = range.lower().getOrdinate(dimension);
                if (ordinate <= point.getOrdinate(dimension)) {
                    left.rangeSearch(range, callback, depth + 1);
                }
            }

            if (right != null) {
                final double ordinate = range.upper().getOrdinate(dimension);
                if (ordinate >= point.getOrdinate(dimension)) {
                    right.rangeSearch(range, callback, depth + 1);
                }
            }
        }
    }

    /**
     * The data represented by a node.
     * <p>
     * This exists as separate from the {@link KDNode} so we can construct it ahead of time and keep the node
     * immutable.
     *
     * @param item the data item.
     * @param point the point that characterises the item within the index.
     * @param <T> the type of data.
     */
    private record KDData<T>(T item, CartPoint3D point) {
    }
}
