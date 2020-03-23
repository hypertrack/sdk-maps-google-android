package com.hypertrack.maps.google.widget;

import com.hypertrack.sdk.views.maps.Predicate;
import com.hypertrack.sdk.views.dao.Trip;

import java.util.HashSet;
import java.util.Set;

class TripFilters {
    private Set<Predicate<Trip>> filters = new HashSet<>();

    void add(Predicate<Trip> predicate) {
        filters.add(predicate);
    }

    void remove(Predicate<Trip> predicate) {
        filters.remove(predicate);
    }

    boolean apply(Trip trip) {
        for (Predicate<Trip> filter : filters) if (!filter.apply(trip)) return false;

        return true;
    }
}