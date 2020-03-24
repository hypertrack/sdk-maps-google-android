package com.hypertrack.maps.google.widget;

import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
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
import java.util.Date;
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

    private GoogleMap mGoogleMap;
    private GoogleMapConfig mConfig;

    private Location currentLocation;
    private Trip currentTrip;

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
        mGoogleMap = googleMap;
        mConfig = config;
        if (googleMap.getMaxZoomLevel() == 21f) {
            googleMap.setMaxZoomPreference(config.maxZoomPreference);
        }
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
        } else {
            currentTrip = null;
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
            mGoogleMap.animateCamera(cameraUpdate, 1000, null);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void moveToTrip(@NonNull Trip trip) {
        if (mGoogleMap != null) {

            currentTrip = trip;
            List<LatLng> latLngs = new ArrayList<>();
            if (currentLocation != null) {
                latLngs.add(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
            }
            if (trip.getDestination() != null) {
                latLngs.add(new LatLng(trip.getDestination().getLatitude(), trip.getDestination().getLongitude()));
            }
            if (trip.getSummary() != null) {
                for (com.hypertrack.sdk.views.dao.Location location : trip.getSummary().getLocations()) {
                    latLngs.add(new LatLng(location.getLatitude(), location.getLongitude()));
                }
            }
            if (trip.getEstimate() != null && trip.getEstimate().getRoute() != null) {
                for (Trip.Point2D point2D : trip.getEstimate().getRoute().getPoints()) {
                    latLngs.add(new LatLng(point2D.getLatitude(), point2D.getLongitude()));
                }
            }

            if (!latLngs.isEmpty()) {
                final LatLngBounds.Builder builder = LatLngBounds.builder();
                for (LatLng latLng : latLngs) {
                    builder.include(latLng);
                }
                if (mConfig.boundingBoxWidth == -1 && mConfig.boundingBoxHeight == -1) {
                    mGoogleMap.animateCamera(
                            CameraUpdateFactory.newLatLngBounds(builder.build(), mConfig.mapBoundingBoxPadding),
                            1000,
                            null);
                } else {
                    mGoogleMap.animateCamera(
                            CameraUpdateFactory.newLatLngBounds(builder.build(),
                                    mConfig.boundingBoxWidth, mConfig.boundingBoxHeight,
                                    mConfig.mapBoundingBoxPadding),
                            1000,
                            null);
                }
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
    public void removeTripFilter(Predicate<Trip> filter) {
        tripFilter.remove(filter);
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
            } else {
                mapTrip.updateData(trip);
            }
            if (!mapTrip.isAdded()) {
                mapTrip.addTo(this);
            }
            if (tripFilter.apply(trip)) {
                mapTrip.update(trip);
            } else {
                mapTrip.hide();
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
                    if (tripFilter.apply(mapTrip.trip)) {
                        mapTrip.update(mapTrip.trip);
                    } else {
                        mapTrip.hide();
                    }
                }
            }
        }
        updateCamera();
    }

    private Trip findTrackedTrip() {
        Trip trackedTrip = null;
        if (currentTrip != null) {
            trackedTrip = currentTrip;
        } else {
            for (MapObject mapObject : gMapObjects.values()) {
                if (mapObject.getType() == HyperTrackMap.TRIP_MAP_OBJECT_TYPE) {
                    GMapTrip mapTrip = (GMapTrip) mapObject;
                    if (mapTrip.isAdded() && tripFilter.apply(mapTrip.trip)) {
                        trackedTrip = ((MapTrip) mapObject).trip;
                        break;
                    }
                }
            }
        }
        return trackedTrip;
    }

    private void updateCamera() {
        if (isCameraFixed) {
            Trip trackedTrip = findTrackedTrip();
            if (trackedTrip != null) {
                moveToTrip(trackedTrip);
            } else if (currentLocation != null) {
                moveToLocation(new HTLatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
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
            googleMap = new WeakReference<>(mapAdapter.mGoogleMap);

            LatLng center = new LatLng(location.getLatitude(), location.getLongitude());
            final float radius = location.getAccuracy()
                    / (float) TileSystem.GroundResolution(location.getLatitude(),
                    googleMap.get().getCameraPosition().zoom);

            if (mapAdapter.mConfig.accuracyCircle != null) {
                accuracyCircle = googleMap.get().addCircle(mapAdapter.mConfig.accuracyCircle
                        .center(center)
                        .radius(radius)
                        .zIndex(Float.MAX_VALUE)
                );
            }

            locationMarker = googleMap.get().addMarker(mapAdapter.mConfig.locationMarker
                    .anchor(0.5f, 0.5f)
                    .position(center)
                    .zIndex(Float.MAX_VALUE)
            );

            if (mapAdapter.mConfig.bearingMarker != null) {
                bearingMarker = googleMap.get().addMarker(mapAdapter.mConfig.bearingMarker
                        .anchor(0.5f, 0.5f)
                        .flat(true)
                        .position(center)
                        .rotation(location.getBearing())
                        .zIndex(Float.MAX_VALUE)
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
                    if (location.getBearing() == 0f) {
                        bearingMarker.setVisible(false);
                    } else {
                        bearingMarker.setRotation(location.getBearing());
                        bearingMarker.setVisible(true);
                    }
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
            if (googleMap != null) {
                googleMap = null;
            }
            isAdded = false;
        }
    }

    /**
     * A google implementation of MapTrip. This class extends {@link MapTrip} and stores trip data, markers, polylines.
     */
    public static class GMapTrip extends MapTrip {
        private GoogleMapConfig mConfig;

        private LatLng destination;
        private int destinationRadius;
        private Date destinationArrivedDate;
        private List<LatLng> summaryRoute = new ArrayList<>();
        private List<LatLng> estimateRoute = new ArrayList<>();
        private LatLng myPosition;

        Marker originMarker;
        Marker destinationMarker;
        Marker endMarker;
        Polyline routePassedPolyline;
        Polyline routeCommingPolyline;
        Circle destinationCircle;

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

        /**
         * Marker of the trip end location.
         *
         * @return {@link Marker} that corresponds to the trip end location on the map.
         */
        public Marker getEndMarker() {
            return endMarker;
        }

        private GMapTrip(Trip trip) {
            super(trip);
            updateData(trip);
        }

        private void updateData(Trip trip) {
            this.trip = trip;

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
                destinationRadius = 0;
            } else {
                destination = new LatLng(trip.getDestination().getLatitude(), trip.getDestination().getLongitude());
                destinationRadius = trip.getDestination().radius;
                destinationArrivedDate = trip.getDestination().getArrivedDate();
            }
        }

        private void addTo(@NonNull GoogleMapAdapter mapAdapter) {
            Log.d(TAG, "add trip - " + trip.getTripId());
            mConfig = mapAdapter.mConfig;

            if (!isAdded) {

                boolean isActive = !trip.getStatus().equals("completed");
                GoogleMapConfig.TripOptions options = isActive ?
                        mConfig.tripOptions
                        : mConfig.tripCompletedOptions;

                if (options.tripPassedRoutePolyline != null) {
                    routePassedPolyline = mapAdapter.mGoogleMap.addPolyline(options.tripPassedRoutePolyline);
                }
                routeCommingPolyline = mapAdapter.mGoogleMap.addPolyline(options.tripComingRoutePolyline);

                if (destination != null) {
                    destinationMarker = mapAdapter.mGoogleMap.addMarker(
                            options.tripDestinationMarker
                                    .position(destination)
                    );
                    if (isActive) {
                        CircleOptions circleOptions = destinationArrivedDate == null ?
                                mConfig.arrivePlaceCircle : mConfig.arrivePlacePassedCircle;
                        destinationCircle = mapAdapter.mGoogleMap.addCircle(
                                circleOptions
                                        .center(destination)
                                        .radius(destinationRadius)
                        );
                    }
                }
                if (routePassedPolyline != null && (mConfig.isPassedRouteVisible || trip.getStatus().equals("completed"))) {
                    originMarker = mapAdapter.mGoogleMap.addMarker(
                            options.tripOriginMarker
                                    .position(new LatLng(0, 0))
                                    .visible(false)
                    );

                    LatLng originLatLng = myPosition;
                    routePassedPolyline.setPoints(summaryRoute);
                    if (!summaryRoute.isEmpty()) {
                        originLatLng = summaryRoute.get(0);
                        if (trip.getStatus().equals("completed") && options.tripEndMarker != null) {
                            endMarker = mapAdapter.mGoogleMap.addMarker(
                                    options.tripEndMarker
                                            .position(summaryRoute.get(summaryRoute.size() - 1))
                            );
                        }
                    } else if (!estimateRoute.isEmpty()) {
                        originLatLng = estimateRoute.get(0);
                    }
                    if (originLatLng != null) {
                        originMarker.setPosition(originLatLng);
                        originMarker.setVisible(true);
                    }
                }
                routeCommingPolyline.setPoints(estimateRoute);

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
            updateData(trip);

            if (isAdded) {

                boolean isActive = !trip.getStatus().equals("completed");

                if (isStatusChanged) {
                    GoogleMapConfig.TripOptions options = isActive ?
                            mConfig.tripOptions
                            : mConfig.tripCompletedOptions;
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
                    destinationMarker.setVisible(true);
                    if (isActive) {
                        CircleOptions circleOptions = destinationArrivedDate == null ?
                                mConfig.arrivePlaceCircle : mConfig.arrivePlacePassedCircle;
                        destinationCircle.setFillColor(circleOptions.getFillColor());
                        destinationCircle.setStrokeColor(circleOptions.getStrokeColor());
                        destinationCircle.setStrokeWidth(circleOptions.getStrokeWidth());
                        destinationCircle.setCenter(destination);
                        destinationCircle.setRadius(destinationRadius);
                        destinationCircle.setVisible(true);
                    } else {
                        if (destinationCircle != null) {
                            destinationCircle.remove();
                        }
                    }
                }
                if (routePassedPolyline != null && (mConfig.isPassedRouteVisible || trip.getStatus().equals("completed"))) {
                    List<LatLng> points = new ArrayList<>();
                    if (!summaryRoute.isEmpty()) {
                        if (originMarker != null) {
                            originMarker.setPosition(summaryRoute.get(0));
                            originMarker.setVisible(true);
                        }
                        if (endMarker != null) {
                            endMarker.setPosition(summaryRoute.get(summaryRoute.size() - 1));
                            endMarker.setVisible(true);
                        }
                        points.addAll(summaryRoute);
                        if (myPosition != null) {
                            points.add(myPosition);
                        }
                    } else if (!estimateRoute.isEmpty()) {
                        if (originMarker != null) {
                            originMarker.setPosition(estimateRoute.get(0));
                            originMarker.setVisible(true);
                        }
                    }
                    routePassedPolyline.setPoints(points);
                    routePassedPolyline.setVisible(true);
                }
                if (routeCommingPolyline != null) {
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
                    routeCommingPolyline.setVisible(true);
                }
            }
        }

        boolean has(Marker marker) {
            return (originMarker != null && marker.getId().equals(originMarker.getId()))
                    || (destinationMarker != null && marker.getId().equals(destinationMarker.getId()));
        }

        public void hide() {
            if (originMarker != null) {
                originMarker.setVisible(false);
            }
            if (destinationMarker != null) {
                destinationMarker.setVisible(false);
            }
            if (endMarker != null) {
                endMarker.setVisible(false);
            }
            if (routePassedPolyline != null) {
                routePassedPolyline.setVisible(false);
            }
            if (routeCommingPolyline != null) {
                routeCommingPolyline.setVisible(false);
            }
            if (destinationCircle != null) {
                destinationCircle.setVisible(false);
            }
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
            if (endMarker != null) {
                endMarker.remove();
                endMarker = null;
            }
            if (routePassedPolyline != null) {
                routePassedPolyline.remove();
                routePassedPolyline = null;
            }
            if (routeCommingPolyline != null) {
                routeCommingPolyline.remove();
                routeCommingPolyline = null;
            }
            if (destinationCircle != null) {
                destinationCircle.remove();
                destinationCircle = null;
            }
        }
    }
}
