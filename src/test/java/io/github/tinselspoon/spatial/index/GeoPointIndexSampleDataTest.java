package io.github.tinselspoon.spatial.index;

import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.NamedCsvRecord;
import io.github.tinselspoon.spatial.index.GeoPointIndex.WithinDistanceConsumer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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

    @ParameterizedTest
    @CsvSource({
            // These points are a spherical projection from the location of EGLL (51.4706, -0.461941)
            // of a distance of 99.99999999 meters at a bearing of 0, 15, 30, 45, ... up to 360
            "51.471499321605826, -0.461941",
            "51.47146867737145, -0.4615673285227443",
            "51.471378833140854, -0.4612191235607355",
            "51.47123591197401, -0.4609201157473675",
            "51.47104965415505, -0.460690682351841",
            "51.47083275328927, -0.4605464584999225",
            "51.470599991136304, -0.4604972717680947",
            "51.47036723017084, -0.460546472729285",
            "51.47015033254939, -0.4606907069978011",
            "51.469964079162274, -0.4609201442060821",
            "51.4698211624273, -0.4612191482067053",
            "51.46973132144103, -0.4615673427520996",
            "51.46970067839417, -0.461941",
            "51.46973132144103, -0.4623146572479004",
            "51.4698211624273, -0.4626628517932903",
            "51.469964079162274, -0.4629618557939179",
            "51.47015033254939, -0.463191293002199",
            "51.47036723017084, -0.463335527270715",
            "51.470599991136304, -0.4633847282319053",
            "51.47083275328927, -0.4633355415000774",
            "51.47104965415505, -0.463191317648159",
            "51.47123591197401, -0.4629618842526326",
            "51.471378833140854, -0.4626628764392687",
            "51.47146867737145, -0.4623146714772557",
            "51.471499321605826, -0.461941", })
    void testBoundarySearchArea(final double searchLatitude, final double searchLongitude) {
        final Airport egll = airports.stream().filter(a -> a.ident().equals("EGLL")).findFirst().orElseThrow();

        final boolean result = classUnderTest.queryWithinDistance(searchLatitude, searchLongitude, 100, mockCallback);

        assertTrue(result);
        verify(mockCallback).accept(eq(egll), AdditionalMatchers.eq(100, 1E-7));
    }
}
