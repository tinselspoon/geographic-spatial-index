package io.github.tinselspoon.spatial.index;

import io.github.tinselspoon.spatial.index.GeoPointIndex.WithinDistanceConsumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/** Unit tests for the {@link GeoPointIndex}. */
@ExtendWith(MockitoExtension.class)
class GeoPointIndexTest {

    @Mock
    private WithinDistanceConsumer<Airport> mockConsumer;

    @Test
    void testEmptyIndex() {
        final GeoPointIndex<Airport> classUnderTest = GeoPointIndex.buildFrom(Collections.emptyList(),
                                                                              Airport::latitude,
                                                                              Airport::longitude);

        final boolean result = classUnderTest.queryWithinDistance(0, 1, 100_000, mockConsumer);

        assertFalse(result);
        verify(mockConsumer, never()).accept(any(), anyDouble());
    }

}
