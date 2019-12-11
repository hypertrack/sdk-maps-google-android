package com.hypertrack.maps.google.utils.constants;

/**
 * This interface defines the constants of the geo package.
 */
public interface GeoConstants {
	/**
	 * <a href="http://en.wikipedia.org/wiki/Earth_radius#Equatorial_radius">Equatorial radius</a>.
	 */
	public static final int RADIUS_EARTH_METERS = 6378137;
	/**
	 * <a href="http://en.wikipedia.org/wiki/Mile">Mile</a>.
	 */
	public static final double METERS_PER_STATUTE_MILE = 1609.344;
	/**
	 * <a href="http://en.wikipedia.org/wiki/Nautical_mile">Nautical mile</a>.
	 */
	public static final double METERS_PER_NAUTICAL_MILE = 1852;
	/**
	 * <a href="http://en.wikipedia.org/wiki/Feet_%28unit_of_length%29">Foot (unit)</a>.
	 */
	public static final double FEET_PER_METER = 3.2808399;
	public static final int EQUATORCIRCUMFENCE = (int) (2 * Math.PI * RADIUS_EARTH_METERS);
}
