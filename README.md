# Geographic Spatial Index

A simple, zero-dependency library for creating a "true" geographic spatial index on WGS 84 latitude-longitude points.

```xml

<dependency>
    <groupId>io.github.tinselspoon</groupId>
    <artifactId>geographic-spatial-index</artifactId>
    <version>LATEST</version>
</dependency>
```

## Introduction

Many libraries for working with geometries operate on the premise that coordinates are planar.
This effectively assumes the world is flat. For a lot of real-world spatial data consisting of WGS 84 latitude and
longitude values, this naturally presents some problems:

- The antimeridian at ±180° longitude requires special handling to split up geometries and bounding boxes so they have
  the correct extent.
- The units of any expression of distance are in angular differences rather than meters, which have no real-world
  meaning and can be misinterpreted or converted incorrectly.

The solution to these problems is usually to project your data into a spatial reference that is specific for the area
you are working in.
However, if you are working with truly global data, this can be complex or impractical.

This library provides an **in-memory spatial index** that **treats points as true geographic coordinates**.
It allows for queries to find items in the index within a certain distance in meters of a specified search point.
It handles wraparound of longitude values at the antimeridian and eliminates other issues caused by planar
interpretation of coordinates.
It's ideal when you need fast look up of points against a reference dataset without involving a database call.

## Usage

You can index any class where it is possible to derive a latitude and longitude.
For example, let's assume we have objects that look like this:

```java
record Airport(String code, double latitude, double longitude) {}
```

We can build our spatial index by supplying the objects we wish to include and functions the index can use to obtain the
latitude and longitude values:

```java
final List<Airport> airports = new ArrayList<>(...);
final GeoPointIndex<Airport> index = GeoPointIndex.buildFrom(airports, Airport::latitude, Airport::longitude);
```

Then, we can query the index like so to find nearby airports for a location of interest:

<!-- @formatter:off -->
```java
index.queryWithinDistance(51.505628725927345,    // latitude
                          -0.09869391486201871,  // longitude
                          30_000,                // distance in meters
                          (airport, distance) -> // consumer function
                              System.out.printf("%s is %.0f km away from you.%n", airport, distance / 1000));
```
<!-- @formatter:on -->

The callback consumer function supplied is invoked for each matching item in the index,
along with the calculated distance from the provided search point.

> [!NOTE]
> As with many such libraries distance calculations are based on a sphere and not a spheroid. This is leads to small
> inaccuracies of up to 0.56% at extremes near a pole or meridional near the equator, which is usually irrelevant for
> most applications. If extra precision is needed, it is suggested to add a 1% buffer to the given distance and then
> filter using a more precise spheroid distance calculation on the returned items.

## How it works

This library is heavily inspired by how PostGIS internally handles spatial indexing for its `geography` type.

Points are internally transformed into 3D cartesian (x, y, z) space on a unit sphere.
This immediately removes some of the issues with latitude-longitude coordinates such as the hard wrap at ±180°.
Since the units of the coordinate system are somewhat related to meters rather than being angular units,
it also allows for a bounding box to be derived for a "distance within" query.

A k-d tree is used to index the points. It stores the cartesian coordinates of the items in the index, and a range
search is used based on the calculated bounding box of the input search criteria. This search is inexact and will
include false positives, so a second stage calculates the spherical distance between the candidate and search points and
performs a final filter before returning results.

## Contributing

The current implementation is fairly straightforward and should perform well for moderately-sized datasets.
It could have some issues with very large datasets - any suggested improvements are welcome.
