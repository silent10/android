package com.evature.evasdk.appinterface;

import android.content.Context;

/**
 * Created by iftah on 12/04/2016.
 */
public interface FlightNavigate {
    enum FlightPageType {
        Unknown,
        Itinerary,
        Gate,
        BoardingPass,
        DepartureTime,
        ArrivalTime,
        BoardingTime
    };
    CallbackResult navigateTo(Context context, FlightPageType page);
}
