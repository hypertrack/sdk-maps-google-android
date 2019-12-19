package com.hypertrack.maps.google.widget;

import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.hypertrack.maps.google.utils.TileSystem;
import com.hypertrack.sdk.views.dao.Trip;
import com.hypertrack.sdk.views.maps.HyperTrackMap;
import com.hypertrack.sdk.views.maps.Predicate;
import com.hypertrack.sdk.views.maps.models.HTLatLng;
import com.hypertrack.sdk.views.maps.models.MapLocation;
import com.hypertrack.sdk.views.maps.models.MapObject;
import com.hypertrack.sdk.views.maps.models.MapTrip;
import com.hypertrack.sdk.views.maps.widget.MapAdapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A class that provides interaction between google maps and hypertrack.
 * <a href="https://developers.google.com/maps/documentation/android-sdk/intro">Google Maps</a>
 *
 * @see HyperTrackMap
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class GoogleMapAdapter extends MapAdapter {
    static final String TAG = "HTMap: GoogleMapAdapter";

    private static final String MY_LOCATION_KEY = "htgm:my_location";

    private WeakReference<GoogleMap> mGoogleMap;
    private GoogleMapConfig mConfig;

    private Location currentLocation;

    private final Map<String, MapObject> gMapObjects = new HashMap<>();

    private boolean isLocationEnabled = true;
    private boolean isCameraFixed = false;

    private final TripFilters tripFilter = new TripFilters();

    /**
     * Finds MapObject by marker in the adapter.
     *
     * @param marker marker on which to search in the adapter.
     * @return instance of MapObject if it has the marker, otherwise null.
     */
    public MapObject findMapObjectByMarker(Marker marker) {
        List<MapObject> mapObjects = new ArrayList<>(gMapObjects.values());
        for (MapObject mapObject : mapObjects) {
            if (mapObject.getType() == HyperTrackMap.LOCATION_MAP_OBJECT_TYPE) {
                GMapLocation mapLocation = (GMapLocation) mapObject;
                if (mapLocation.has(marker)) {
                    return mapObject;
                }
            } else if (mapObject.getType() == HyperTrackMap.TRIP_MAP_OBJECT_TYPE) {
                GMapTrip gMapTrip = (GMapTrip) mapObject;
                if (gMapTrip.has(marker)) {
                    return mapObject;
                }
            }
        }
        return null;
    }

    /**
     * Constructs a GoogleMapAdapter with the given map instance {@link GoogleMap}
     * and config {@link GoogleMapConfig}.
     *
     * @param googleMap instance of google map {@link SupportMapFragment#getMapAsync(OnMapReadyCallback)}
     * @param config    that needed to setup GoogleMapAdapter.
     */
    public GoogleMapAdapter(@NonNull GoogleMap googleMap, @NonNull GoogleMapConfig config) {
        mGoogleMap = new WeakReference<>(googleMap);
        mConfig = config;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMyLocationEnabled(boolean enabled) {
        isLocationEnabled = enabled;
        if (enabled) {
            updateMyLocation(currentLocation);
        } else {
            if (gMapObjects.containsKey(MY_LOCATION_KEY)) {
                gMapObjects.remove(MY_LOCATION_KEY).remove();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCameraFixedEnabled(boolean enabled) {
        isCameraFixed = enabled;
        if (enabled) {
            updateCamera();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void moveToLocation(@NonNull HTLatLng latLng) {
        if (mGoogleMap != null) {
            final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                    new LatLng(latLng.latitude, latLng.longitude),
                    14
            );
            mGoogleMap.get().setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                @Override
                public void onMapLoaded() {
                    mGoogleMap.get().animateCamera(cameraUpdate, 1000, null);
                }
            });
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void moveToTrip(@NonNull Trip trip) {
        if (mGoogleMap != null) {
            final LatLngBounds.Builder builder = new LatLngBounds.Builder();
            if (currentLocation != null) {
                builder.include(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
            }
            if (trip.getDestination() != null) {
                builder.include(new LatLng(trip.getDestination().getLatitude(), trip.getDestination().getLongitude()));
            }
            if (trip.getSummary() != null) {
                for (com.hypertrack.sdk.views.dao.Location location : trip.getSummary().getLocations()) {
                    builder.include(new LatLng(location.getLatitude(), location.getLongitude()));
                }
            }
            if (trip.getEstimate() != null && trip.getEstimate().getRoute() != null) {
                for (Trip.Point2D point2D : trip.getEstimate().getRoute().getPoints()) {
                    builder.include(new LatLng(point2D.getLatitude(), point2D.getLongitude()));
                }
            }
            if (mConfig.boundingBoxWidth == -1 && mConfig.boundingBoxHeight == -1) {
                mGoogleMap.get().animateCamera(
                        CameraUpdateFactory.newLatLngBounds(builder.build(), mConfig.mapBoundingBoxPadding),
                        1000,
                        null);
            } else {
                mGoogleMap.get().animateCamera(
                        CameraUpdateFactory.newLatLngBounds(builder.build(),
                                mConfig.boundingBoxWidth, mConfig.boundingBoxHeight,
                                mConfig.mapBoundingBoxPadding),
                        1000,
                        null);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addTripFilter(Predicate<Trip> filter) {
        tripFilter.add(filter);
        remapTrips();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MapTrip addTrip(@NonNull Trip trip) {
        if (mGoogleMap != null) {
            GMapTrip mapTrip = (GMapTrip) gMapObjects.get(trip.getTripId());
            if (mapTrip == null) {
                mapTrip = new GMapTrip(trip);
                gMapObjects.put(trip.getTripId(), mapTrip);
            }
            if (mapTrip.isAdded()) {
                mapTrip.update(trip);
            } else {
                if (tripFilter.apply(trip)) mapTrip.addTo(this);
            }

            updateActiveTrip();
            return mapTrip;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateMyLocation(@Nullable Location location) {
        Log.d(TAG, "updateMyLocation: " + location);
        currentLocation = location;

        if (isLocationEnabled && mGoogleMap != null && location != null) {

            GMapLocation mapLocation = (GMapLocation) gMapObjects.get(MY_LOCATION_KEY);
            if (mapLocation == null || !mapLocation.isAdded()) {
                mapLocation = new GMapLocation(location);
                mapLocation.addTo(this);
                gMapObjects.put(MY_LOCATION_KEY, mapLocation);
            } else {
                mapLocation.update(location);
            }

            updateActiveTrip();
        }
    }

    private void updateActiveTrip() {
        if (isLocationEnabled && currentLocation != null) {
            for (MapObject item : gMapObjects.values()) {
                if (item instanceof GMapTrip) {
                    GMapTrip gMapTrip = (GMapTrip) item;
                    if (gMapTrip.trip.getStatus().equals("active") && gMapTrip.isAdded()) {
                        gMapTrip.updateMyPosition(currentLocation);
                        break;
                    }
                }
            }
        }
        updateCamera();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyDataSetChanged() {
        remapTrips();
    }

    private void remapTrips() {
        if (mGoogleMap != null) {

            for (MapObject mapObject : gMapObjects.values()) {
                if (mapObject.getType() == HyperTrackMap.TRIP_MAP_OBJECT_TYPE) {
                    GMapTrip mapTrip = (GMapTrip) mapObject;
                    Log.d(TAG, "remapTrips trip - " + mapTrip.trip.getTripId() + " : " + tripFilter.apply(mapTrip.trip));
                    if (!tripFilter.apply(mapTrip.trip)) {
                        mapTrip.remove();
                    } else if (!mapTrip.isAdded()) {
                        mapTrip.addTo(this);
                    } else {
                        mapTrip.update(mapTrip.trip);
                    }
                }
            }
        }
        updateCamera();
    }

    private void updateCamera() {
        if (mGoogleMap != null && isCameraFixed) {

            final LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (MapObject mapObject : gMapObjects.values()) {
                if (mapObject.isAdded()) {
                    if (mapObject.getType() == HyperTrackMap.LOCATION_MAP_OBJECT_TYPE) {
                        GMapLocation mapLocation = (GMapLocation) mapObject;
                        builder.include(new LatLng(mapLocation.location.getLatitude(), mapLocation.location.getLongitude()));
                    } else if (mapObject.getType() == HyperTrackMap.TRIP_MAP_OBJECT_TYPE) {
                        GMapTrip gMapTrip = (GMapTrip) mapObject;
                        builder.include(gMapTrip.destination);
                        if (!gMapTrip.estimateRoute.isEmpty()) {
                            for (LatLng latLng : gMapTrip.estimateRoute) {
                                builder.include(latLng);
                            }
                        }
                        if (!gMapTrip.summaryRoute.isEmpty()) {
                            for (LatLng latLng : gMapTrip.summaryRoute) {
                                builder.include(latLng);
                            }
                        }
                    }
                }
            }
            if (mConfig.boundingBoxWidth == -1 && mConfig.boundingBoxHeight == -1) {
                mGoogleMap.get().animateCamera(
                        CameraUpdateFactory.newLatLngBounds(builder.build(), mConfig.mapBoundingBoxPadding),
                        1000,
                        null);
            } else {
                mGoogleMap.get().animateCamera(
                        CameraUpdateFactory.newLatLngBounds(builder.build(),
                                mConfig.boundingBoxWidth, mConfig.boundingBoxHeight,
                                mConfig.mapBoundingBoxPadding),
                        1000,
                        null);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        for (MapObject mapObject : gMapObjects.values()) {
            mapObject.remove();
        }
        gMapObjects.clear();

        if (mGoogleMap != null) {
            mGoogleMap.clear();
            mGoogleMap = null;
        }
    }

    private static int locationPositionInRoute(List<LatLng> route, LatLng location) {
        float minDistance = Integer.MAX_VALUE;
        int currentLocationPositionInRoute = 0;
        if (location != null) {
            for (int i = 0; i < route.size(); i++) {
                LatLng latLng = route.get(i);
                float[] results = new float[1];
                Location.distanceBetween(location.latitude, location.longitude,
                        latLng.latitude, latLng.longitude,
                        results);
                if (results[0] < minDistance) {
                    minDistance = results[0];
                    currentLocationPositionInRoute = i;
                }
            }
        }
        return currentLocationPositionInRoute;
    }

    /**
     * A google implementation of MapLocation. This class extends {@link MapLocation} and stores
     * location data, markers, accuracy circle.
     */
    public static class GMapLocation extends MapLocation {
        private WeakReference<GoogleMap> googleMap;

        private Circle accuracyCircle;
        private Marker locationMarker;
        private Marker bearingMarker;

        private GMapLocation(@NonNull Location location) {
            super(location);
        }

        private void addTo(@NonNull GoogleMapAdapter mapAdapter) {
            googleMap = mapAdapter.mGoogleMap;

            LatLng center = new LatLng(location.getLatitude(), location.getLongitude());
            final float radius = location.getAccuracy()
                    / (float) TileSystem.GroundResolution(location.getLatitude(),
                    googleMap.get().getCameraPosition().zoom);

            if (mapAdapter.mConfig.accuracyCircle != null) {
                accuracyCircle = mapAdapter.mGoogleMap.get().addCircle(mapAdapter.mConfig.accuracyCircle
                        .center(center)
                        .radius(radius)
                );
            }

            locationMarker = mapAdapter.mGoogleMap.get().addMarker(mapAdapter.mConfig.locationMarker
                    .anchor(0.5f, 0.5f)
                    .position(center)
            );

            if (mapAdapter.mConfig.bearingMarker != null) {
                bearingMarker = mapAdapter.mGoogleMap.get().addMarker(mapAdapter.mConfig.bearingMarker
                        .anchor(0.5f, 0.5f)
                        .flat(true)
                        .position(center)
                        .rotation(location.getBearing())
                );
            }
            isAdded = true;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void update(@NonNull Location location) {
            this.location = location;

            if (isAdded) {
                LatLng center = new LatLng(location.getLatitude(), location.getLongitude());
                final float radius = location.getAccuracy()
                        / (float) TileSystem.GroundResolution(location.getLatitude(),
                        googleMap.get().getCameraPosition().zoom);

                if (accuracyCircle != null) {
                    accuracyCircle.setCenter(center);
                    accuracyCircle.setRadius(radius);
                }
                if (locationMarker != null) {
                    locationMarker.setPosition(center);
                }
                if (bearingMarker != null) {
                    bearingMarker.setPosition(center);
                    bearingMarker.setRotation(location.getBearing());
                }
            }
        }

        private boolean has(Marker marker) {
            return locationMarker != null && marker.getId().equals(locationMarker.getId());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void remove() {
            if (accuracyCircle != null) {
                accuracyCircle.remove();
                accuracyCircle = null;
            }
            if (locationMarker != null) {
                locationMarker.remove();
                locationMarker = null;
            }
            if (bearingMarker != null) {
                bearingMarker.remove();
                bearingMarker = null;
            }
            googleMap = null;
            isAdded = false;
        }
    }

    /**
     * A google implementation of MapTrip. This class extends {@link MapTrip} and stores trip data, markers, polylines.
     */
    public static class GMapTrip extends MapTrip {
        private GoogleMapConfig mConfig;

        private LatLng destination;
        private List<LatLng> summaryRoute = new ArrayList<>();
        private List<LatLng> estimateRoute = new ArrayList<>();
        private LatLng myPosition;

        Marker originMarker;
        Marker destinationMarker;
        Polyline routePassedPolyline;
        Polyline routeCommingPolyline;

        /**
         * Marker of origin location in the trip.
         *
         * @return {@link Marker} that corresponds to origin place on the map.
         */
        public Marker getOriginMarker() {
            return originMarker;
        }

        /**
         * Marker of destination location in the trip.
         *
         * @return {@link Marker} that corresponds to destination place on the map.
         */
        public Marker getDestinationMarker() {
            return destinationMarker;
        }

        private GMapTrip(@NonNull Trip trip) {
            super(trip);
            updateData();
        }

        private void updateData() {
            summaryRoute.clear();
            if (trip.getSummary() != null && !trip.getSummary().getLocations().isEmpty()) {
                for (com.hypertrack.sdk.views.dao.Location location : trip.getSummary().getLocations()) {
                    summaryRoute.add(new LatLng(location.getLatitude(), location.getLongitude()));
                }
            }

            estimateRoute.clear();
            if (trip.getEstimate() != null && trip.getEstimate().getRoute() != null && !trip.getEstimate().getRoute().getPoints().isEmpty()) {
                for (Trip.Point2D item : trip.getEstimate().getRoute().getPoints()) {
                    estimateRoute.add(new LatLng(item.getLatitude(), item.getLongitude()));
                }
            }

            if (trip.getDestination() == null ||
                    trip.getDestination().getLatitude() == null || trip.getDestination().getLongitude() == null) {
                destination = null;
            } else {
                destination = new LatLng(trip.getDestination().getLatitude(), trip.getDestination().getLongitude());
            }
        }

        private void addTo(@NonNull GoogleMapAdapter mapAdapter) {
            Log.d(TAG, "add trip - " + trip.getTripId());
            mConfig = mapAdapter.mConfig;

            if (!isAdded) {
                GoogleMapConfig.TripOptions options = trip.getStatus().equals("completed") ?
                        mConfig.tripCompletedOptions
                        : mConfig.tripOptions;

                if (options.tripPassedRoutePolyline != null) {
                    routePassedPolyline = mapAdapter.mGoogleMap.get().addPolyline(options.tripPassedRoutePolyline);
                }
                routeCommingPolyline = mapAdapter.mGoogleMap.get().addPolyline(options.tripComingRoutePolyline);

                if (destination != null) {
                    destinationMarker = mapAdapter.mGoogleMap.get().addMarker(
                            options.tripDestinationMarker
                                    .anchor(0.5f, 0.5f)
                                    .position(destination)
                    );
                }
                if (routePassedPolyline != null && (mConfig.isPassedRouteVisible || trip.getStatus().equals("completed"))) {
                    if (options.tripOriginMarker != null && !summaryRoute.isEmpty()) {
                        originMarker = mapAdapter.mGoogleMap.get().addMarker(
                                options.tripOriginMarker
                                        .anchor(0.5f, 0.5f)
                                        .position(summaryRoute.get(0))
                        );
                    }
                    routePassedPolyline.setPoints(summaryRoute);
                }
                if (!estimateRoute.isEmpty() && routeCommingPolyline != null) {
                    routeCommingPolyline.setPoints(estimateRoute);
                }

                isAdded = true;
            }
        }

        void updateMyPosition(@NonNull Location location) {
            this.myPosition = new LatLng(location.getLatitude(), location.getLongitude());
            update(trip);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void update(@NonNull Trip trip) {
            Log.d(TAG, "update trip - " + trip.getTripId());

            boolean isStatusChanged = !this.trip.getStatus().equals(trip.getStatus());
            this.trip = trip;
            updateData();

            if (isAdded) {

                if (isStatusChanged) {

                    GoogleMapConfig.TripOptions options = trip.getStatus().equals("completed") ?
                            mConfig.tripCompletedOptions
                            : mConfig.tripOptions;
                    if (destinationMarker != null) {
                        destinationMarker.setIcon(options.tripDestinationMarker.getIcon());
                    }
                    if (originMarker != null) {
                        originMarker.setIcon(options.tripOriginMarker.getIcon());
                    }
                    routeCommingPolyline.setColor(options.tripComingRoutePolyline.getColor());
                    routeCommingPolyline.setWidth(options.tripComingRoutePolyline.getWidth());
                    routeCommingPolyline.setPattern(options.tripComingRoutePolyline.getPattern());
                    if (routePassedPolyline != null) {
                        routePassedPolyline.setColor(options.tripPassedRoutePolyline.getColor());
                        routePassedPolyline.setWidth(options.tripPassedRoutePolyline.getWidth());
                        routePassedPolyline.setPattern(options.tripPassedRoutePolyline.getPattern());
                    }
                }

                if (destinationMarker != null && destination != null) {
                    destinationMarker.setPosition(destination);
                }
                if (routePassedPolyline != null && (mConfig.isPassedRouteVisible || trip.getStatus().equals("completed"))) {
                    if (originMarker != null && !summaryRoute.isEmpty()) {
                        originMarker.setPosition(summaryRoute.get(0));
                    }
                    routePassedPolyline.setPoints(summaryRoute);
                } else if (routeCommingPolyline != null) {
                    List<LatLng> points = new ArrayList<>();
                    if (!estimateRoute.isEmpty()) {
                        if (myPosition == null) {
                            points.addAll(estimateRoute);
                        } else {
                            points.add(myPosition);
                            int position = locationPositionInRoute(estimateRoute, myPosition);
                            if (position != estimateRoute.size() - 1) {
                                points.addAll(estimateRoute.subList(position + 1, estimateRoute.size()));
                            }
                        }
                        if (destination != null) {
                            points.add(destination);
                        }
                    }
                    routeCommingPolyline.setPoints(points);
                }
            }
        }

        boolean has(Marker marker) {
            return (originMarker != null && marker.getId().equals(originMarker.getId()))
                    || (destinationMarker != null && marker.getId().equals(destinationMarker.getId()));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void remove() {
            isAdded = false;
            if (originMarker != null) {
                originMarker.remove();
                originMarker = null;
            }
            if (destinationMarker != null) {
                destinationMarker.remove();
                destinationMarker = null;
            }
            if (routePassedPolyline != null) {
                routePassedPolyline.remove();
                routePassedPolyline = null;
            }
            if (routeCommingPolyline != null) {
                routeCommingPolyline.remove();
                routeCommingPolyline = null;
            }
        }
    }
}
