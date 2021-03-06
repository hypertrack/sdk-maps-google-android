package com.hypertrack.maps.google.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.TypedValue;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.PolylineOptions;
import com.hypertrack.maps.google.R;
import com.hypertrack.maps.google.utils.TileSystem;
import com.hypertrack.sdk.views.maps.models.MapTrip;

import java.util.Arrays;
import java.util.Collections;

/**
 * Class with configuration parameters for GoogleMapAdapter e.g marker option, polyline option, camera update.
 *
 * @see GoogleMapAdapter
 */
@SuppressWarnings("unused")
public class GoogleMapConfig {
    MarkerOptions locationMarker;
    MarkerOptions bearingMarker;
    CircleOptions accuracyCircle;
    CircleOptions arrivePlaceCircle;
    CircleOptions arrivePlacePassedCircle;

    GoogleMapConfig.TripOptions tripOptions;
    GoogleMapConfig.TripOptions tripCompletedOptions;

    float maxZoomPreference;
    int mapBoundingBoxPadding;
    int boundingBoxWidth = -1;
    int boundingBoxHeight = -1;

    boolean isPassedRouteVisible = true;

    /**
     * Creates new GoogleMapConfig.Builder with styles from application theme or default resources.
     * Don't use application context, only components have have the app theme and all described styles there.
     *
     * @param context Activity context, with main application theme. The context is needed to get styles.
     * @return new instance of GoogleMapConfig.Builder.
     */
    public static GoogleMapConfig.Builder newBuilder(@NonNull Context context) {
        return new GoogleMapConfig.Builder(context);
    }

    public static GoogleMapConfig.TripOptions newTripOptions() {
        return new GoogleMapConfig.TripOptions(null);
    }

    private GoogleMapConfig() {
    }

    /**
     * Class provides detailed configuration of trip markers {@link MapTrip}
     */
    public static class TripOptions {

        MarkerOptions tripOriginMarker;
        MarkerOptions tripDestinationMarker;
        PolylineOptions tripPassedRoutePolyline;
        PolylineOptions tripComingRoutePolyline;
        MarkerOptions tripEndMarker;

        private TripOptions(StyleAttrs styleAttrs) {
            if (styleAttrs != null) {
                tripOriginMarker = new MarkerOptions()
                        .anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromResource(styleAttrs.tripOriginIcon));
                tripDestinationMarker = new MarkerOptions()
                        .anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromResource(styleAttrs.tripDestinationIcon));
                tripPassedRoutePolyline = new PolylineOptions()
                        .width(styleAttrs.tripRouteWidth)
                        .color(styleAttrs.tripRouteColor)
                        .pattern(Collections.singletonList((PatternItem) new Dash(styleAttrs.tripRouteWidth)));
                tripComingRoutePolyline = new PolylineOptions()
                        .width(styleAttrs.tripRouteWidth)
                        .color(styleAttrs.tripRouteColor)
                        .pattern(Arrays.asList(
                                new Dash(styleAttrs.tripRouteWidth * 2),
                                new Gap(styleAttrs.tripRouteWidth)));
                tripEndMarker = new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(styleAttrs.tripEndIcon));
            }
        }

        /**
         * Defines MarkerOptions for a trip origin marker.
         *
         * @param markerOptions a new set of marker options {@link MarkerOptions}.
         * @return this instance of the class.
         */
        public TripOptions tripOriginMarker(MarkerOptions markerOptions) {
            this.tripOriginMarker = markerOptions;
            return this;
        }

        /**
         * Defines MarkerOptions for a trip destination marker.
         *
         * @param markerOptions a new set of marker options {@link MarkerOptions}.
         * @return this instance of the class.
         */
        public TripOptions tripDestinationMarker(MarkerOptions markerOptions) {
            this.tripDestinationMarker = markerOptions;
            return this;
        }

        /**
         * Defines PolylineOptions for a trip passed route.
         *
         * @param polylineOptions a new set of polyline options {@link PolylineOptions}.
         * @return this instance of the class.
         */
        public TripOptions tripPassedRoutePolyline(PolylineOptions polylineOptions) {
            this.tripPassedRoutePolyline = polylineOptions;
            return this;
        }

        /**
         * Defines PolylineOptions for a trip coming route.
         *
         * @param polylineOptions a new set of polyline options {@link PolylineOptions}.
         * @return this instance of the class.
         */
        public TripOptions tripComingRoutePolyline(PolylineOptions polylineOptions) {
            this.tripComingRoutePolyline = polylineOptions;
            return this;
        }

        /**
         * Defines MarkerOptions for a trip end marker. In most cases it's a last location in a trip passed route.
         *
         * @param markerOptions a new set of marker options {@link MarkerOptions}.
         * @return this instance of the class.
         */
        public TripOptions tripEndMarker(MarkerOptions markerOptions) {
            this.tripEndMarker = markerOptions;
            return this;
        }

        private TripOptions build() {
            return this;
        }

        private static class StyleAttrs {
            float tripRouteWidth;
            int tripOriginIcon;
            int tripDestinationIcon;
            int tripRouteColor;
            int tripEndIcon;
        }
    }

    /**
     * Class provides interface to configure {@link GoogleMapConfig}. By default it is filled by app theme or hypertrack styles.
     * You can set certain params and others remains by default.
     */
    @SuppressWarnings({"unused", "WeakerAccess"})
    public static class Builder {
        private final GoogleMapConfig config = new GoogleMapConfig();

        private int myLocationIcon;

        private TripOptions.StyleAttrs tripStyleAttrs = new TripOptions.StyleAttrs();
        private TripOptions.StyleAttrs tripCompletedStyleAttrs = new TripOptions.StyleAttrs();

        @SuppressWarnings("unused")
        private Builder(Context context) {
            Resources r = context.getResources();
            float density = r.getDisplayMetrics().density;
            int size = (int) (256 * density);
            TileSystem.setTileSize(size);

            config.mapBoundingBoxPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16,
                    r.getDisplayMetrics()
            );
            float tripRouteWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3,
                    r.getDisplayMetrics()
            );
            float accuracyStrokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1,
                    r.getDisplayMetrics()
            );

            myLocationIcon = R.drawable.marker;
            int myLocationBearingIcon = R.drawable.bearing_arrow_green;
            int myLocationAccuracyColor = r.getColor(R.color.ht_accuracy);
            int myLocationAccuracyStrokeColor = r.getColor(R.color.ht_accuracy_stroke);

            int placeArriveRadiusColor = r.getColor(R.color.ht_place_arrive);
            int placeArriveRadiusPassedColor = r.getColor(R.color.ht_place_arrive_passed);

            tripStyleAttrs.tripRouteWidth = tripRouteWidth;
            tripStyleAttrs.tripOriginIcon = R.drawable.starting_position;
            tripStyleAttrs.tripDestinationIcon = R.drawable.destination;
            tripStyleAttrs.tripRouteColor = r.getColor(R.color.ht_route);
            tripCompletedStyleAttrs.tripRouteWidth = tripRouteWidth;
            tripCompletedStyleAttrs.tripOriginIcon = R.drawable.departure_sd_c;
            tripCompletedStyleAttrs.tripDestinationIcon = R.drawable.arrival_sd_c;
            tripCompletedStyleAttrs.tripRouteColor = r.getColor(R.color.ht_route_completed);
            tripCompletedStyleAttrs.tripEndIcon = R.drawable.destination_red_sd;

            TypedValue attrs = new TypedValue();
            context.getTheme().resolveAttribute(R.attr.hyperTrackMapStyle, attrs, true);
            if (attrs.data > 0) {
                TypedArray typedArray = context.obtainStyledAttributes(attrs.data, R.styleable.HyperTrackMap);
                myLocationIcon = typedArray.getResourceId(R.styleable.HyperTrackMap_myLocationIcon, myLocationIcon);
                myLocationBearingIcon = typedArray.getResourceId(R.styleable.HyperTrackMap_myLocationBearingIcon, myLocationBearingIcon);
                myLocationAccuracyColor = typedArray.getColor(R.styleable.HyperTrackMap_myLocationAccuracyColor, myLocationAccuracyColor);
                myLocationAccuracyStrokeColor = typedArray.getColor(R.styleable.HyperTrackMap_myLocationAccuracyStrokeColor, myLocationAccuracyStrokeColor);

                placeArriveRadiusColor = typedArray.getColor(R.styleable.HyperTrackMap_placeArriveRadiusColor, placeArriveRadiusColor);
                placeArriveRadiusPassedColor = typedArray.getColor(R.styleable.HyperTrackMap_placeArriveRadiusPassedColor, placeArriveRadiusPassedColor);

                tripStyleAttrs.tripOriginIcon = typedArray.getResourceId(R.styleable.HyperTrackMap_tripOriginIcon, tripStyleAttrs.tripOriginIcon);
                tripStyleAttrs.tripDestinationIcon = typedArray.getResourceId(R.styleable.HyperTrackMap_tripDestinationIcon, tripStyleAttrs.tripDestinationIcon);
                tripStyleAttrs.tripRouteColor = typedArray.getColor(R.styleable.HyperTrackMap_tripRouteColor, tripStyleAttrs.tripRouteColor);
                tripCompletedStyleAttrs.tripOriginIcon = typedArray.getResourceId(R.styleable.HyperTrackMap_tripCompletedOriginIcon, tripCompletedStyleAttrs.tripOriginIcon);
                tripCompletedStyleAttrs.tripDestinationIcon = typedArray.getResourceId(R.styleable.HyperTrackMap_tripCompletedDestinationIcon, tripCompletedStyleAttrs.tripDestinationIcon);
                tripCompletedStyleAttrs.tripRouteColor = typedArray.getColor(R.styleable.HyperTrackMap_tripCompletedRouteColor, tripCompletedStyleAttrs.tripRouteColor);
                tripCompletedStyleAttrs.tripEndIcon = typedArray.getResourceId(R.styleable.HyperTrackMap_tripCompletedEndIcon, tripCompletedStyleAttrs.tripEndIcon);
                typedArray.recycle();
            }

            if (myLocationBearingIcon != 0) {
                config.bearingMarker = new MarkerOptions()
                        .flat(true)
                        .anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromResource(myLocationBearingIcon));
            }
            config.accuracyCircle = new CircleOptions()
                    .fillColor(myLocationAccuracyColor)
                    .strokeColor(myLocationAccuracyStrokeColor)
                    .strokeWidth(accuracyStrokeWidth);
            config.arrivePlaceCircle = new CircleOptions()
                    .fillColor(placeArriveRadiusColor)
                    .strokeColor(Color.TRANSPARENT);
            config.arrivePlacePassedCircle = new CircleOptions()
                    .fillColor(placeArriveRadiusPassedColor)
                    .strokeColor(Color.TRANSPARENT);
        }

        /**
         * Defines MarkerOptions for a my location marker.
         *
         * @param markerOptions a new set of marker options {@link MarkerOptions}.
         * @return this instance of the class.
         */
        public Builder locationMarker(MarkerOptions markerOptions) {
            config.locationMarker = markerOptions;
            return this;
        }

        /**
         * Defines MarkerOptions for a my location bearing marker.
         *
         * @param markerOptions a new set of marker options {@link MarkerOptions}.
         * @return this instance of the class.
         */
        public Builder bearingMarker(MarkerOptions markerOptions) {
            config.bearingMarker = markerOptions;
            return this;
        }

        /**
         * Defines CircleOptions for a my location accuracy circle.
         *
         * @param circleOptions a new set of marker options {@link CircleOptions}.
         * @return this instance of the class.
         */
        public Builder accuracyCircle(CircleOptions circleOptions) {
            config.accuracyCircle = circleOptions;
            return this;
        }

        /**
         * Defines TripOptions for an active trips.
         *
         * @param tripOptions a new set of trip options {@link TripOptions}.
         * @return this instance of the class.
         */
        public Builder tripOptions(TripOptions tripOptions) {
            config.tripOptions = tripOptions;
            return this;
        }

        /**
         * Defines TripOptions for an completed trips.
         *
         * @param tripOptions a new set of trip options {@link TripOptions}.
         * @return this instance of the class.
         */
        public Builder tripCompletedOptions(TripOptions tripOptions) {
            config.tripCompletedOptions = tripOptions;
            return this;
        }

        /**
         * Defines CircleOptions for a destination and a geofence arriving radius.
         *
         * @param circleOptions a new set of marker options {@link CircleOptions}.
         * @return this instance of the class.
         */
        public Builder arrivePlaceCircle(CircleOptions circleOptions) {
            config.arrivePlaceCircle = circleOptions;
            return this;
        }

        /**
         * Defines CircleOptions for a destination and a geofence arriving radius after it's passed.
         *
         * @param circleOptions a new set of marker options {@link CircleOptions}.
         * @return this instance of the class.
         */
        public Builder arrivePlacePassedCircle(CircleOptions circleOptions) {
            config.arrivePlacePassedCircle = circleOptions;
            return this;
        }

        /**
         * Setup visibility of passed routes.
         *
         * @param isVisible true if should be shown, false otherwise.
         * @return this instance of the class.
         */
        public Builder showPassedRoute(boolean isVisible) {
            config.isPassedRouteVisible = isVisible;
            return this;
        }

        /**
         * Setup bounding box of specified dimensions.
         *
         * @param width  bounding box width in pixels (px)
         * @param height bounding box height in pixels (px)
         * @return this instance of the class.
         */
        public Builder boundingBoxDimensions(int width, int height) {
            config.boundingBoxWidth = width;
            config.boundingBoxHeight = height;
            return this;
        }

        /**
         * Builds map configuration of specified options.
         *
         * @return the instance of {@link GoogleMapConfig}.
         */
        public GoogleMapConfig build() {
            if (config.locationMarker == null) {
                config.locationMarker = new MarkerOptions()
                        .anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromResource(myLocationIcon));
            }

            if (config.tripOptions == null) {
                config.tripOptions = new TripOptions(tripStyleAttrs).build();
            }
            if (config.tripCompletedOptions == null) {
                config.tripCompletedOptions = new TripOptions(tripCompletedStyleAttrs).build();
            }
            config.maxZoomPreference = 18;

            return config;
        }
    }
}
