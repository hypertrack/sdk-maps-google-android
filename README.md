# HyperTrack Android Maps utility library for google maps (DEPRECATED)

> This library is DEPRECATED and not supported by Hypertrack team. Please do not depend on it and use your own solutions 

![Android HyperTrackViews SDK](https://img.shields.io/badge/Android%20Views%20SDK-0.6.0-brightgreen.svg)

[HyperTrack](https://www.hypertrack.com) lets you add live location tracking to your mobile app. Live location is made available along with ongoing activity, 
tracking controls and tracking outage with reasons.

HyperTrack Android Maps SDK is an implementation for [HyperTrackViews SDK](https://github.com/hypertrack/views-android) map adapter that allows to bind HyperTrackViews SDK with google maps 
and display tracking and static data in realtime. SDK provided access to functionality that helps with displaying current location, active and completed trips, 
including any map objects manipulations and customizations. The module exposes methods to add trips, add filter of trip displaying, move map camera to these objects,
and interact with the map objects.

* [Publishable Key](#publishable-key)–Sign up and get your keys
* [Usage](#usage)–Integrate the HyperTrack Maps into your app
* [How it works](#how-it-works)–Simple implementation example
* [Documentations](#installation)–Additional information that helps to use HyperTrack Android Maps.

## Publishable Key

We use Publishable Key to identify your devices. To get one:
1. Go to the [Signup page](https://dashboard.hypertrack.com/signup). Enter your email address and password.
2. Open the verification link sent to your email.
3. Open the [Setup page](https://dashboard.hypertrack.com/setup), where you can copy your Publishable Key.

## Usage

### Dependency
**project stage:**
```
allprojects {
    repositories {
        ...
        maven {
            name 'hypertrack'
            url 'http://m2.hypertrack.com'
        }
    }
}
```
**app stage:**
```
dependencies {
    ...
    implementation 'com.hypertrack:hypertrack-views:0.6.0' // !important >= 0.6.0
    implementation 'com.hypertrack:maps-google:0.1.1'
}
```

### Simple
```
HyperTrackViews hyperTrackViews = HyperTrackViews.getInstance(context, "HYPERTRACK_PUB_KEY");

GoogleMapAdapter mapAdapter = new GoogleMapAdapter(googleMap, GoogleMapConfig.newBuilder(context).build());
hyperTrackMap = HyperTrackMap.getInstance(context, mapAdapter);
// Enable device location tracking via LocationManager
hyperTrackMap.bind(new GpsLocationProvider(mContext));
// Bind HyperTrackMap and HyperTrackViews together to display device location and trips updates from HyperTrack
hyperTrackMap.bind(hyperTrackViews, deviceId);

```


### Extended
```
HyperTrack hyperTrack = HyperTrack.getInstance(this, "HYPERTRACK_PUB_KEY");
String deviceId = hyperTrack.getDeviceID();

HyperTrackViews hyperTrackViews = HyperTrackViews.getInstance(context, "HYPERTRACK_PUB_KEY");

GoogleMapAdapter mapAdapter = new GoogleMapAdapter(googleMap, GoogleMapConfig.newBuilder(context).build());
hyperTrackMap = HyperTrackMap.getInstance(context, mapAdapter);
hyperTrackMap.bind(hyperTrackViews, deviceId);


mapAdapter.addTripFilter(new Predicate<Trip>() {
    @Override
    public boolean apply(Trip trip) {
        return <your condition to display trip>;
    }
});

hyperTrackMap.subscribeTrip(tripId);
//or
MapTrip mapTrip = mapAdapter.addTrip(trip);
// mapTrip.remove() if it's not more needed on the map.

```

### Styles
Change the styles of displaying objects on the map in two ways.

in style.xml
```
<style name="AppTheme">
        ...

        <item name="hyperTrackMapStyle">@style/HyperTrackMap</item>
    </style>

<style name="HyperTrackMap">
        <item name="myLocationIcon">@drawable/icon_drive_base_transparent</item>
        <item name="myLocationBearingIcon">@drawable/icondrive</item>
        <item name="tripDestinationIcon">@drawable/ic_destination_marker</item>
        <item name="tripRouteColor">@color/black</item>
        <item name="tripCompletedOriginIcon">@drawable/ic_source_marker</item>
        <item name="tripCompletedDestinationIcon">@drawable/ic_destination_marker</item>
        <item name="tripCompletedRouteColor">@color/black</item>
    </style>
```

or in code
```
GoogleMapConfig.TripOptions tripOptions = GoogleMapConfig.newTripOptions()
                .tripDestinationMarker(new MarkerOptions()
                        .anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_destination_marker)))
                .tripPassedRoutePolyline(null)
                .tripComingRoutePolyline(new PolylineOptions()
                        .width(mapRouteWidth)
                        .color(Color.BLACK)
                        .pattern(Collections.<PatternItem>singletonList(new Dash(mapRouteWidth))));
GoogleMapConfig mapConfig = GoogleMapConfig.newBuilder(context)
                .tripOptions(tripOptions).build();
```


## How it works

You can use Maps in two ways.

The first is just to bind `HyperTrackMap` and `HyperTrackViews` to a specific device. After that, just subscribe a trip `hyperTrackMap.subscribeTrip(tripId)`.
`HyperTrackMap` will manage all updates and displays .

The second way to work directly with `GoogleMapAdapter` and to manage all updates via adapter interface. 
For example add trip with `addTrip(trip)`, it will return `MapTrip`, after that you have to update `mapTrip.update(trip)` on trip update event from Views SDK.

## Documentation

[HyperTrack Tracking Sample](https://github.com/hypertrack/live-app-android)

[HyperTrack Views Sample](https://github.com/hypertrack/views-android)

[Ridesharing Sample](https://github.com/hypertrack/ridesharing-android)
