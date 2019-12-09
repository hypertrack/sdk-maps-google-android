package com.hypertrack.maps.google;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.hypertrack.maps.google.widget.GoogleMapAdapter;
import com.hypertrack.maps.google.widget.GoogleMapConfig;
import com.hypertrack.sdk.views.DeviceUpdatesHandler;
import com.hypertrack.sdk.views.HyperTrackViews;
import com.hypertrack.sdk.views.dao.StatusUpdate;
import com.hypertrack.sdk.views.dao.Trip;
import com.hypertrack.sdk.views.maps.GpsLocationProvider;
import com.hypertrack.sdk.views.maps.HyperTrackMap;


/**
 * A class extends SupportMapFragment and includes all needed sdk initializations to simplify
 * the integration of HyperTrack Views Sdk and Map Sdk.
 *
 * @see HyperTrackViews
 * @see HyperTrackMap
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class HyperTrackMapFragment extends SupportMapFragment
        implements OnMapReadyCallback, LocationListener, DeviceUpdatesHandler {
    private static final String TAG = "HTMapFragment";

    private GoogleMapConfig mapConfig;
    protected HyperTrackViews hyperTrackViews;
    protected HyperTrackMap hyperTrackMap;

    /**
     * Provide GoogleMapConfig for {@link HyperTrackMap}.
     *
     * @param mapConfig
     */
    public void setMapConfig(GoogleMapConfig mapConfig) {
        this.mapConfig = mapConfig;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        try {
            ApplicationInfo app = layoutInflater.getContext().getPackageManager()
                    .getApplicationInfo(layoutInflater.getContext().getPackageName(), PackageManager.GET_META_DATA);
            String hyperTrackPubKey = app.metaData.getString("com.hypertrack.sdk.PUB_KEY");
            if (TextUtils.isEmpty(hyperTrackPubKey)) {
                Log.e(TAG, "There is not HyperTrack PUB_KEY in manifest");
            } else {
                hyperTrackViews = HyperTrackViews.getInstance(layoutInflater.getContext(), hyperTrackPubKey);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return super.onCreateView(layoutInflater, viewGroup, bundle);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getMapAsync(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (getContext() != null) {
            if (mapConfig == null) {
                mapConfig = GoogleMapConfig.newBuilder(getContext()).build();
            }
            GoogleMapAdapter mapAdapter = new GoogleMapAdapter(googleMap, mapConfig);
            hyperTrackMap = HyperTrackMap.getInstance(getContext(), mapAdapter)
                    .bind(new GpsLocationProvider(getContext()));
            hyperTrackMap.setLocationUpdatesListener(this);
        }

    }

    /**
     * Subscribes device {@link DeviceUpdatesHandler} and map updates e.g. {@link #onTripUpdateReceived(Trip)}
     *
     * @param deviceId HyperTrack device id (HyperTrack.getInstance(context, "HYPER_TRACK_PUB_KEY").getDeviceID())
     */
    public void subscribeToDevice(String deviceId) {
        if (hyperTrackViews != null) {
            hyperTrackViews.subscribeToDeviceUpdates(deviceId, this);
            if (hyperTrackMap != null) {
                hyperTrackMap.bind(hyperTrackViews, deviceId);
                hyperTrackMap.subscribeActiveTrips();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLocationChanged(Location location) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onProviderEnabled(String s) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onProviderDisabled(String s) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLocationUpdateReceived(@NonNull com.hypertrack.sdk.views.dao.Location location) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBatteryStateUpdateReceived(int i) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStatusUpdateReceived(@NonNull StatusUpdate statusUpdate) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onTripUpdateReceived(@NonNull Trip trip) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onError(Exception e, String s) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCompleted(String s) {

    }
}
