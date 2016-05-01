package com.evature.evasdk.appinterface;

import android.content.Context;

import com.evature.evasdk.evaapis.crossplatform.EvaLocation;
import com.evature.evasdk.evaapis.crossplatform.EvaTravelers;
import com.evature.evasdk.evaapis.crossplatform.FlightAttributes;
import com.evature.evasdk.evaapis.crossplatform.RequestAttributes.SortOrderEnum;
import com.evature.evasdk.evaapis.crossplatform.RequestAttributes.SortEnum;

import java.util.Date;

/**
 * Created by iftah on 6/2/15.
 */
public interface EvaFlightSearch {

    /***
     * handleFlightSearch - callback when Eva collects criteria to search for flights
     * @param context - Android context
     * @param isComplete - true if Eva considers the search flow "complete", ie. all the mandatory criteria have been requested by the user
     * @param origin - location of take-off
     * @param destination - location of landing
     * @param departDateMin - range of dates the user wishes to depart on
     * @param departDateMax   if only a single date is entered the Max date will be equal to the Min date
     * @param returnDateMin - range of dates the user wishes to return on, null if one-way flight
     * @param returnDateMax   if only a single date is entered the Max date will be equal to the Min date
     * @param travelers - how many travelers, split into age categories
     * @param attributes - different flight related attributes - airline, nonstop, seat type, etc...
     * @param sortBy - how should the results be sorted (eg. price, date, etc..), or null if not mentioned
     * @param sortOrder - ascending or descending or null if not mentioned
     */
    EvaResult handleFlightSearch(Context context,
                                 boolean isComplete, EvaLocation origin, EvaLocation destination,
                                 Date departDateMin, Date departDateMax,
                                 Date returnDateMin, Date returnDateMax,
                                 EvaTravelers travelers,
                                 FlightAttributes attributes,
                                 SortEnum sortBy, SortOrderEnum sortOrder);
}
