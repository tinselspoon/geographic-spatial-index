package io.github.tinselspoon.spatial.index;

/**
 * Example airport data object.
 *
 * @param ident the airport code.
 * @param latitude the airport reference latitude.
 * @param longitude the airport reference longitude.
 */
record Airport(String ident, double latitude, double longitude) {
}
