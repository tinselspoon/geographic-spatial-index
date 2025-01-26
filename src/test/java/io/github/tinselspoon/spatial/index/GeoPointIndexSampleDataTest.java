package io.github.tinselspoon.spatial.index;

import io.github.tinselspoon.spatial.index.GeoPointIndex.WithinDistanceConsumer;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.NamedCsvRecord;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/** Tests for the {@link GeoPointIndex}. */
@ExtendWith(MockitoExtension.class)
class GeoPointIndexSampleDataTest {

    private static List<Airport> airports;

    private static GeoPointIndex<Airport> classUnderTest;

    @Mock
    private WithinDistanceConsumer<Airport> mockCallback;

    @BeforeAll
    static void setUp() throws IOException {
        // Read in some "real world" representative data
        try (CsvReader<NamedCsvRecord> reader = CsvReader.builder()
                                                         .ofNamedCsvRecord(Path.of("src/test/resources/ourairports.csv"))) {
            airports = reader.stream()
                             .map(r -> new Airport(r.getField("ident"),
                                                   Double.parseDouble(r.getField("latitude_deg")),
                                                   Double.parseDouble(r.getField("longitude_deg"))))
                             .toList();
            classUnderTest = GeoPointIndex.buildFrom(airports, Airport::latitude, Airport::longitude);
        }
    }

    @Test
    void testTinyNegativeSearchArea() {
        final boolean result = classUnderTest.queryWithinDistance(51, 0.1, 500, mockCallback);

        assertFalse(result);
        verify(mockCallback, never()).accept(any(), anyDouble());
    }

    @Test
    void testTinySearchArea() {
        final Airport egll = airports.stream().filter(a -> a.ident().equals("EGLL")).findFirst().orElseThrow();

        final boolean result = classUnderTest.queryWithinDistance(51.47168589670692,
                                                                  -0.45898471100754595,
                                                                  500,
                                                                  mockCallback);

        assertTrue(result);
        verify(mockCallback).accept(eq(egll), AdditionalMatchers.eq(237.715073, 1E-6));
    }

    @Test
    void testLargeSearchArea() {
        final List<Airport> foundAirports = new ArrayList<>();
        final boolean result = classUnderTest.queryWithinDistance(51,
                                                                  0.1,
                                                                  100_000,
                                                                  (airport, distance) -> foundAirports.add(airport));

        assertTrue(result);
        assertEquals(221, foundAirports.size());
    }

    @Test
    void testWholeWorldSearchArea() {
        // This is obviously a daft query, but we want to ensure we are indeed able to satisfy such a large extent
        // WHEN querying for the entire extent of the earth
        final List<Airport> foundAirports = new ArrayList<>();
        classUnderTest.queryWithinDistance(51,
                                           0.1,
                                           CartPoint3D.WGS84_RADIUS_METERS * Math.PI,
                                           (airport, distance) -> foundAirports.add(airport));

        // THEN all airports are found
        final List<Airport> missingAirports = new ArrayList<>(airports);
        missingAirports.removeAll(foundAirports);
        assertEquals(0, missingAirports.size(), "Missing airports: " + missingAirports);
    }

}
