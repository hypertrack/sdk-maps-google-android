package com.hypertrack.maps.google.utils;

import android.graphics.Point;
import android.graphics.Rect;

import com.google.android.gms.maps.model.LatLng;
import com.hypertrack.maps.google.utils.constants.GeoConstants;

@SuppressWarnings("ALL")
public abstract class TileSystem {

	@Deprecated
	public static final double EarthRadius = GeoConstants.RADIUS_EARTH_METERS;
	/**
	 * Use {@link TileSystem#getMinLatitude()} instead
	 */
	@Deprecated
	public static final double MinLatitude = -85.05112877980659;
	/**
	 * Use {@link TileSystem#getMaxLatitude()} instead
	 */
    @Deprecated
	public static final double MaxLatitude = 85.05112877980659;
	/**
	 * Use {@link TileSystem#getMinLongitude()} instead
	 */
    @Deprecated
	public static final double MinLongitude = -180d;
	/**
	 * Use {@link TileSystem#getMaxLongitude()} instead
	 */
    @Deprecated
	public static final double MaxLongitude = 180d;

    /**
     * Used to be in the `TileSystem` class of another package
     */
	private static int mTileSize = 256;

	/**
	 * The maximum possible zoom for primary key of SQLite table is 29,
	 * because it gives enough space for y(29bits), x(29bits) and zoom(5bits in order to code 29),
	 * total: 63 bits used, just small enough for a `long` variable of 4 bytes
     * @since 6.0.2
     * Used to be in the `TileSystem` class of another package
	 */
	public static final int primaryKeyMaxZoomLevel = 29;

	/**
	 * Maximum Zoom Level - we use Integers to store zoom levels so overflow happens at 2^32 - 1,
	 * but we also have a tile size that is typically 2^8, so (32-1)-8-1 = 22
     * @since 6.0.2
     * Used to be in the `TileSystem` class of another package
	 */
	private static int mMaxZoomLevel = primaryKeyMaxZoomLevel;

	public static void setTileSize(final int tileSize) {
		int pow2 = (int) (0.5 + Math.log(tileSize) / Math.log(2));
		mMaxZoomLevel = Math.min(primaryKeyMaxZoomLevel, (64 - 1) - pow2 - 1);

		mTileSize = tileSize;
	}

	public static int getTileSize() {
		return mTileSize;
	}

    /**
     * Used to be in the `TileSystem` class of another package
     */
	public static int getMaximumZoomLevel() {
		return mMaxZoomLevel;
	}

	/**
	 *
	 */
	public static double getTileSize(final double pZoomLevel) {
		return MapSize(pZoomLevel - getInputTileZoomLevel(pZoomLevel));
	}

	/**
	 *
	 */
	public static int getInputTileZoomLevel(final double pZoomLevel) {
		return MyMath.floorToInt(pZoomLevel);
	}

	/**
	 *
	 */
	public static double MapSize(final double pZoomLevel) {
		return getTileSize() * getFactor(pZoomLevel);
	}

	/**
	 *
	 */
	public static double getFactor(final double pZoomLevel) {
		return Math.pow(2, pZoomLevel > 15 ? 15 : pZoomLevel);
	}

	public static double GroundResolution(final double latitude, final int levelOfDetail) {
		return GroundResolution(latitude, (double)levelOfDetail);
	}

	/**
	 *
	 */
	public static double GroundResolution(final double latitude, final double zoomLevel) {
		return GroundResolutionMapSize(wrap(latitude, -90, 90, 180), MapSize(zoomLevel));
	}

	/**
	 * Most likely meters/pixel at the given latitude
	 */
	public static double GroundResolutionMapSize(double latitude, final double mapSize) {
		latitude = Clip(latitude, -90, 90);
		return Math.cos(latitude * Math.PI / 180) * 2 * Math.PI * GeoConstants.RADIUS_EARTH_METERS
				/ mapSize;
	}

	public static double MapScale(final double latitude, final int levelOfDetail, final int screenDpi) {
		return GroundResolution(latitude, levelOfDetail) * screenDpi / 0.0254;
	}

	/**
	 *
	 * Same as {@link #PixelXYToLatLong(int, int, double, LatLng) PixelXYToLatLong} but without wrap
	 */
	public LatLng PixelXYToLatLongWithoutWrap(
			final int pixelX, final int pixelY, final double zoomLevel, final LatLng reuse) {
		final double mapSize = MapSize(zoomLevel);
		return PixelXYToLatLongMapSizeWithoutWrap(
				pixelX,
				pixelY,
				mapSize, reuse);
	}

	/**
	 *
	 * Same as {@link #PixelXYToLatLongMapSize(int, int, double, LatLng, boolean, boolean) PixelXYToLatLongMapSize}
	 * but without wrap
	 */
	public LatLng PixelXYToLatLongMapSizeWithoutWrap(final int pixelX, final int pixelY,
			final double mapSize, final LatLng reuse) {
		final double x = (pixelX / (double)mapSize) - 0.5;
		final double y = 0.5 - (pixelY / (double)mapSize);
		final double latitude = 90 - 360 * Math.atan(Math.exp(-y * 2 * Math.PI)) / Math.PI;
		final double longitude = 360 * x;
		return (reuse == null ? new LatLng(latitude, longitude) : reuse);
	}

	/**
	 *
	 */
	public static double Clip(final double n, final double minValue, final double maxValue) {
		return Math.min(Math.max(n, minValue), maxValue);
	}

	@Deprecated
	public Point PixelXYToTileXY(final int pixelX, final int pixelY, final Point reuse) {
		return PixelXYToTileXY(pixelX, pixelY, getTileSize(), reuse);
	}

	/**
	 *
	 * Use {@link TileSystem#getTileFromMercator(long, double)} instead
	 */
	@Deprecated
	public Point PixelXYToTileXY(final int pPixelX, final int pPixelY, final double pTileSize, final Point pReuse) {
		final Point out = (pReuse == null ? new Point() : pReuse);
		out.x = getTileFromMercator(pPixelX, pTileSize);
		out.y = getTileFromMercator(pPixelY, pTileSize);
		return out;
	}

	/**
	 *
	 * Use {@link TileSystem#getTileFromMercator(long, double)} instead
	 */
	@Deprecated
	public Rect PixelXYToTileXY(final Rect rect, final double pTileSize, final Rect pReuse) {
		final Rect out = (pReuse == null ? new Rect() : pReuse);
		out.left = getTileFromMercator(rect.left, pTileSize);
		out.top = getTileFromMercator(rect.top, pTileSize);
		out.right = getTileFromMercator(rect.right, pTileSize);
		out.bottom = getTileFromMercator(rect.bottom, pTileSize);
		return out;
	}

	@Deprecated
	public Point TileXYToPixelXY(final int tileX, final int tileY, final Point reuse) {
		final Point out = (reuse == null ? new Point() : reuse);
		final int size = getTileSize();
		out.x = truncateToInt(getMercatorFromTile(tileX, size));
		out.y = truncateToInt(getMercatorFromTile(tileY, size));
		return out;
	}

	/**
	 * Quadkey principles can be found at https://msdn.microsoft.com/en-us/library/bb259689.aspx
	 * Works only for zoom level >= 1
	 */
	public static String TileXYToQuadKey(final int tileX, final int tileY, final int levelOfDetail) {
		final char[] quadKey = new char[levelOfDetail];
		for (int i = 0 ; i < levelOfDetail;  i++) {
			char digit = '0';
			final int mask = 1 << i;
			if ((tileX & mask) != 0) {
				digit++;
			}
			if ((tileY & mask) != 0) {
				digit++;
				digit++;
			}
			quadKey[levelOfDetail - i - 1] = digit;
		}
		return new String(quadKey);
	}

	/**
	 * Quadkey principles can be found at https://msdn.microsoft.com/en-us/library/bb259689.aspx
	 */
	public static Point QuadKeyToTileXY(final String quadKey, final Point reuse) {
		final Point out = reuse == null ? new Point() : reuse;
		if (quadKey == null || quadKey.length() == 0) {
			throw new IllegalArgumentException("Invalid QuadKey: " + quadKey);
		}
		int tileX = 0;
		int tileY = 0;
		final int zoom = quadKey.length();
		for (int i = 0 ; i < zoom;  i++) {
			final int value = 1 << i;
			switch (quadKey.charAt(zoom - i - 1)) {
				case '0':
					break;
				case '1':
					tileX += value;
					break;
				case '2':
					tileY += value;
					break;
				case '3':
					tileX += value;
					tileY += value;
					break;
				default:
					throw new IllegalArgumentException("Invalid QuadKey: " + quadKey);
			}
		}
		out.x = tileX;
		out.y = tileY;
		return out;
	}

	/**
	 * Returns a value that lies within <code>minValue</code> and <code>maxValue</code> by
	 * subtracting/adding <code>interval</code>.
	 *
	 * @param n
	 *            the input number
	 * @param minValue
	 *            the minimum value
	 * @param maxValue
	 *            the maximum value
	 * @param interval
	 *            the interval length
	 * @return a value that lies within <code>minValue</code> and <code>maxValue</code> by
	 *         subtracting/adding <code>interval</code>
	 */
	private static double wrap(double n, final double minValue, final double maxValue, final double interval) {
		if (minValue > maxValue) {
			throw new IllegalArgumentException("minValue must be smaller than maxValue: "
					+ minValue + ">" + maxValue);
		}
		if (interval > maxValue - minValue + 1) {
			throw new IllegalArgumentException(
					"interval must be equal or smaller than maxValue-minValue: " + "min: "
							+ minValue + " max:" + maxValue + " int:" + interval);
		}
		while (n < minValue) {
			n += interval;
		}
		while (n > maxValue) {
			n -= interval;
		}
		return n;
	}

	/**
	 *
	 */
	public long getMercatorFromXY01(final double pXY01, final double pMapSize, boolean wrapEnabled) {
		return ClipToLong(pXY01 * pMapSize, pMapSize, wrapEnabled);
	}

	/**
	 *
	 */
	public long getCleanMercator(final long pMercator, final double pMercatorMapSize, boolean wrapEnabled) {
		return ClipToLong(wrapEnabled ? wrap(pMercator, 0, pMercatorMapSize, pMercatorMapSize) : pMercator, pMercatorMapSize, wrapEnabled);
	}

	/**
	 *
	 */
	public static long ClipToLong(final double pValue, final double pMax, final boolean pWrapEnabled) {
		final long longValue =  MyMath.floorToLong(pValue);
		if (!pWrapEnabled) {
			return longValue;
		}
		if (longValue <= 0) {
			return 0;
		}
		final long longMax = MyMath.floorToLong(pMax - 1);
		return longValue >= pMax ? longMax : longValue;
	}

	/**
	 *
	 */
	@Deprecated
	public static long Clip(final long n, final long minValue, final long maxValue) {
		return Math.min(Math.max(n, minValue), maxValue);
	}

	/**
	 *
	 * Casts a long type value into an int with no harm.
	 * The typical use case is to compute pixel coordinates with high zoom
	 * (which won't fit into int but will fit into long)
	 * and to truncate them into int in order to display them on the screen (which requires int)
	 * The meaning of a pixel coordinate of MIN/MAX_VALUE is just:
	 * it's far far away and it doesn't crash the app
	 */
	public static int truncateToInt(final long value) {
		return (int)Math.max(Math.min(value, Integer.MAX_VALUE), Integer.MIN_VALUE);
	}

	/**
	 *
	 */
	public double getXY01FromMercator(final long pMercator, final double pMapSize, boolean wrapEnabled) {
		return wrapEnabled ? Clip(pMercator / pMapSize, 0, 1) : pMercator / pMapSize;
	}

	/**
	 *
	 */
	public static int getTileFromMercator(final long pMercator, final double pTileSize) {
		return MyMath.floorToInt(pMercator / pTileSize);
	}

	/**
	 *
	 */
	public static long getMercatorFromTile(final int pTile, final double pTileSize) {
		return Math.round(pTile * pTileSize);
	}

	/**
	 * @since 6.0.3
	 */
	private int clipTile(final int pTile, final int pZoom) {
		if (pTile < 0) {
			return 0;
		}
		final int max = 1 << pZoom;
		if (pTile >= max) {
			return max - 1;
		}
		return pTile;
	}
}