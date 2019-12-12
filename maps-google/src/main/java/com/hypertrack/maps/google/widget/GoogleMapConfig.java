package com.hypertrack.maps.google.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.TypedValue;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.PolylineOptions;
import com.hypertrack.maps.google.R;
import com.hypertrack.maps.google.utils.TileSystem;

import java.util.Collections;

@SuppressWarnings("unused")
public class GoogleMapConfig {
    MarkerOptions locationMarker;
    MarkerOptions bearingMarker;
    CircleOptions accuracyCircle;

    GoogleMapConfig.TripOptions tripOptions;
    GoogleMapConfig.TripOptions tripCompletedOptions;

    int mapAnimatePadding;

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

    public static class TripOptions {

        MarkerOptions tripOriginMarker;
        MarkerOptions tripDestinationMarker;
        PolylineOptions tripPassedRoutePolyline;
        PolylineOptions tripComingRoutePolyline;

        private TripOptions(StyleAttrs styleAttrs) {
            if (styleAttrs != null) {
                if (tripOriginMarker == null) {
                    tripOriginMarker = new MarkerOptions()
                            .anchor(0.5f, 0.5f)
                            .icon(BitmapDescriptorFactory.fromResource(styleAttrs.tripOriginIcon));
                }
                if (tripDestinationMarker == null) {
                    tripDestinationMarker = new MarkerOptions()
                            .anchor(0.5f, 0.5f)
                            .icon(BitmapDescriptorFactory.fromResource(styleAttrs.tripDestinationIcon));
                }
                if (tripPassedRoutePolyline == null) {
                    tripPassedRoutePolyline = new PolylineOptions()
                            .width(styleAttrs.tripRouteWidth)
                            .color(styleAttrs.tripRouteColor)
                            .pattern(Collections.singletonList((PatternItem) new Dash(styleAttrs.tripRouteWidth)));
                }
                if (tripComingRoutePolyline == null) {
                    tripComingRoutePolyline = new PolylineOptions()
                            .width(styleAttrs.tripRouteWidth)
                            .color(styleAttrs.tripRouteColor)
                            .pattern(Collections.singletonList((PatternItem) new Dot()));
                }
            }
        }

        public TripOptions tripOriginMarker(MarkerOptions markerOptions) {
            this.tripOriginMarker = markerOptions;
            return this;
        }

        public TripOptions tripDestinationMarker(MarkerOptions markerOptions) {
            this.tripDestinationMarker = markerOptions;
            return this;
        }

        public TripOptions tripPassedRoutePolyline(PolylineOptions polylineOptions) {
            this.tripPassedRoutePolyline = polylineOptions;
            return this;
        }

        public TripOptions tripComingRoutePolyline(PolylineOptions polylineOptions) {
            this.tripComingRoutePolyline = polylineOptions;
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
        }
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public static class Builder {
        private final GoogleMapConfig config = new GoogleMapConfig();

        private int myLocationIcon;

        private TripOptions.StyleAttrs tripStyleAttrs = new TripOptions.StyleAttrs();
        private TripOptions.StyleAttrs tripCompletedStyleAttrs = new TripOptions.StyleAttrs();

        private boolean isPassedRouteVisible = true;

        @SuppressWarnings("unused")
        private Builder(Context context) {
            Resources r = context.getResources();
            float density = r.getDisplayMetrics().density;
            int size = (int) (256 * density);
            TileSystem.setTileSize(size);

            config.mapAnimatePadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100,
                    r.getDisplayMetrics()
            );
            float tripRouteWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4,
                    r.getDisplayMetrics()
            );
            float accuracyStrokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1,
                    r.getDisplayMetrics()
            );

            myLocationIcon = R.drawable.marker;
            int myLocationBearingIcon = 0;
            int myLocationAccuracyColor = r.getColor(R.color.ht_accuracy);
            int myLocationAccuracyStrokeColor = r.getColor(R.color.ht_accuracy_stroke);

            tripStyleAttrs.tripRouteWidth = tripRouteWidth;
            tripCompletedStyleAttrs.tripRouteWidth = tripRouteWidth;
            tripStyleAttrs.tripOriginIcon = R.drawable.starting_position;
            tripStyleAttrs.tripDestinationIcon = R.drawable.destination;
            tripStyleAttrs.tripRouteColor = r.getColor(R.color.ht_route);
            tripCompletedStyleAttrs.tripOriginIcon = R.drawable.departure_sd_c;
            tripCompletedStyleAttrs.tripDestinationIcon = R.drawable.arrival_sd_c;
            tripCompletedStyleAttrs.tripRouteColor = r.getColor(R.color.ht_route_completed);

            TypedValue attrs = new TypedValue();
            context.getTheme().resolveAttribute(R.attr.hyperTrackMapStyle, attrs, true);
            if (attrs.data > 0) {
                TypedArray typedArray = context.obtainStyledAttributes(attrs.data, R.styleable.HyperTrackMap);
                myLocationIcon = typedArray.getResourceId(R.styleable.HyperTrackMap_myLocationIcon, myLocationIcon);
                myLocationBearingIcon = typedArray.getResourceId(R.styleable.HyperTrackMap_myLocationBearingIcon, myLocationBearingIcon);
                myLocationAccuracyColor = typedArray.getColor(R.styleable.HyperTrackMap_myLocationAccuracyColor, myLocationAccuracyColor);
                myLocationAccuracyStrokeColor = typedArray.getColor(R.styleable.HyperTrackMap_myLocationAccuracyStrokeColor, myLocationAccuracyStrokeColor);

                tripStyleAttrs.tripOriginIcon = typedArray.getResourceId(R.styleable.HyperTrackMap_tripOriginIcon, tripStyleAttrs.tripOriginIcon);
                tripStyleAttrs.tripDestinationIcon = typedArray.getResourceId(R.styleable.HyperTrackMap_tripDestinationIcon, tripStyleAttrs.tripDestinationIcon);
                tripStyleAttrs.tripRouteColor = typedArray.getColor(R.styleable.HyperTrackMap_tripRouteColor, tripStyleAttrs.tripRouteColor);
                tripCompletedStyleAttrs.tripOriginIcon = typedArray.getResourceId(R.styleable.HyperTrackMap_tripCompletedOriginIcon, tripCompletedStyleAttrs.tripOriginIcon);
                tripCompletedStyleAttrs.tripDestinationIcon = typedArray.getResourceId(R.styleable.HyperTrackMap_tripCompletedDestinationIcon, tripCompletedStyleAttrs.tripDestinationIcon);
                tripCompletedStyleAttrs.tripRouteColor = typedArray.getColor(R.styleable.HyperTrackMap_tripCompletedRouteColor, tripCompletedStyleAttrs.tripRouteColor);
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
            config.tripCompletedOptions = new TripOptions(tripCompletedStyleAttrs).build();
        }

        public Builder locationMarker(MarkerOptions markerOptions) {
            config.locationMarker = markerOptions;
            return this;
        }

        public Builder bearingMarker(MarkerOptions markerOptions) {
            config.bearingMarker = markerOptions;
            return this;
        }

        public Builder accuracyCircle(CircleOptions circleOptions) {
            config.accuracyCircle = circleOptions;
            return this;
        }

        public Builder tripOptions(TripOptions tripOptions) {
            config.tripOptions = tripOptions;
            return this;
        }

        public Builder tripCompletedOptions(TripOptions tripOptions) {
            config.tripCompletedOptions = tripOptions;
            return this;
        }

        public Builder showPassedRoute(boolean isVisible) {
            isPassedRouteVisible = isVisible;
            return this;
        }

        public GoogleMapConfig build() {
            if (config.locationMarker == null) {
                config.locationMarker = new MarkerOptions()
                        .anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromResource(myLocationIcon));
            }

            if (config.tripOptions == null) {
                config.tripOptions = new TripOptions(tripStyleAttrs).build();
                if (!isPassedRouteVisible) {
                    config.tripOptions.tripPassedRoutePolyline = null;
                }
            }
            if (config.tripCompletedOptions == null) {
                config.tripCompletedOptions = config.tripOptions;
            }
            if (!isPassedRouteVisible) {
                config.tripCompletedOptions.tripPassedRoutePolyline = null;
            }

            return config;
        }
    }
}
