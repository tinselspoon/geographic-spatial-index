package io.github.tinselspoon.spatial.index;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Unit tests for {@link KDTree}. */
class KDTreeTest {

    private KDTree<Item> classUnderTest;

    @BeforeEach
    void setUp() {
        classUnderTest = KDTree.create(List.of(new Item("a", new CartPoint3D(2, 3, 4)),
                                               new Item("b", new CartPoint3D(1, 2, 3)),
                                               new Item("c", new CartPoint3D(4, 5, 6)),
                                               new Item("d", new CartPoint3D(7, 8, 9)),
                                               new Item("e", new CartPoint3D(10, 11, 12)),
                                               new Item("f", new CartPoint3D(6, 5, 4)),
                                               new Item("g", new CartPoint3D(3, 2, 1)),
                                               new Item("h", new CartPoint3D(3, 2, 1))), Item::point);
    }

    @Test
    void testBasicRangeSearch() {
        // GIVEN a range of points
        CartPoint3D lower = new CartPoint3D(2, 2, 3);
        CartPoint3D upper = new CartPoint3D(6, 8, 6);

        // WHEN searching
        List<Item> result = new ArrayList<>();
        classUnderTest.rangeSearch(new CartBox3D(lower, upper), (item, point) -> result.add(item));

        // THEN we get the points (2, 3, 4), (4, 5, 6), (6, 5, 4)
        assertEquals(3, result.size());
        assertTrue(result.contains(new Item("a", new CartPoint3D(2, 3, 4))));
        assertTrue(result.contains(new Item("c", new CartPoint3D(4, 5, 6))));
        assertTrue(result.contains(new Item("f", new CartPoint3D(6, 5, 4))));
    }

    @Test
    void testNoPointsInRange() {
        // GIVEN a range outside the points
        final CartPoint3D lower = new CartPoint3D(11, 11, 11);
        final CartPoint3D upper = new CartPoint3D(15, 15, 15);

        // WHEN searching
        List<Item> result = new ArrayList<>();
        classUnderTest.rangeSearch(new CartBox3D(lower, upper), (item, point) -> result.add(item));

        // THEN no points should be in the range
        assertTrue(result.isEmpty());
    }

    @Test
    void testAllPointsInRange() {
        // GIVEN a range covering all points
        final CartPoint3D lower = new CartPoint3D(1, 1, 1);
        final CartPoint3D upper = new CartPoint3D(10, 12, 13);

        // WHEN searching
        List<Item> result = new ArrayList<>();
        classUnderTest.rangeSearch(new CartBox3D(lower, upper), (item, point) -> result.add(item));

        // THEN all points should be in the range
        assertEquals(8, result.size());
    }

    @Test
    void testPartialRangeMatch() {
        // GIVEN a range
        final CartPoint3D lower = new CartPoint3D(3, 4, 5);
        final CartPoint3D upper = new CartPoint3D(8, 9, 10);

        // WHEN searching
        List<Item> result = new ArrayList<>();
        classUnderTest.rangeSearch(new CartBox3D(lower, upper), (item, point) -> result.add(item));

        // THEN matching points should be within the range
        assertEquals(2, result.size());
        assertTrue(result.contains(new Item("c", new CartPoint3D(4, 5, 6))));
        assertTrue(result.contains(new Item("d", new CartPoint3D(7, 8, 9))));
    }

    @Test
    void testExactRangeMatch() {
        // GIVEN an exact range to search for
        final CartPoint3D exact = new CartPoint3D(3, 2, 1);

        // WHEN searching
        List<Item> result = new ArrayList<>();
        classUnderTest.rangeSearch(new CartBox3D(exact, exact), (item, point) -> result.add(item));

        // THEN matching points should be within the range
        assertEquals(2, result.size());
        assertTrue(result.contains(new Item("g", new CartPoint3D(3, 2, 1))));
        assertTrue(result.contains(new Item("h", new CartPoint3D(3, 2, 1))));
    }

    @Test
    void testBoundaryRangeMatch() {
        // GIVEN a range matching exactly two points
        final CartPoint3D lower = new CartPoint3D(4, 5, 6);
        final CartPoint3D upper = new CartPoint3D(7, 8, 9);

        // WHEN searching
        List<Item> result = new ArrayList<>();
        classUnderTest.rangeSearch(new CartBox3D(lower, upper), (item, point) -> result.add(item));

        // THEN matching points should be within the range
        assertEquals(2, result.size());
        assertTrue(result.contains(new Item("c", new CartPoint3D(4, 5, 6))));
        assertTrue(result.contains(new Item("d", new CartPoint3D(7, 8, 9))));
    }

    /**
     * Simple wrapper to simulate data attached to a point.
     *
     * @param identifier a label for the point.
     * @param point the point to index on.
     */
    private record Item(String identifier, CartPoint3D point) {
    }
}
