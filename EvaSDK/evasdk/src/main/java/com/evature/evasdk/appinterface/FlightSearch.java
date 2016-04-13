package com.evature.evasdk.appinterface;

import android.content.Context;

import com.evature.evasdk.evaapis.crossplatform.EvaLocation;
import com.evature.evasdk.evaapis.crossplatform.EvaTravelers;
import com.evature.evasdk.evaapis.crossplatform.FlightAttributes.SeatClass;
import com.evature.evasdk.evaapis.crossplatform.FlightAttributes.SeatType;
import com.evature.evasdk.evaapis.crossplatform.FlightAttributes.FoodType;
import com.evature.evasdk.evaapis.crossplatform.RequestAttributes.SortOrderEnum;
import com.evature.evasdk.evaapis.crossplatform.RequestAttributes.SortEnum;

import java.util.Date;

/**
 * Created by iftah on 6/2/15.
 */
public interface FlightSearch {

    /***
     * handleOneWayFlightSearch - callback when Eva collects criteria to search for one way flights
     * @param context - Android context
     * @param isComplete - true if Eva considers the search flow "complete", ie. all the mandatory criteria have been requested by the user
     * @param origin - location of take-off
     * @param destination - location of landing
     * @param departDateMin - range of dates the user wishes to depart on
     * @param departDateMax   if only a single date is entered the Max date will be equal to the Min date
     * @param returnDateMin - range of dates the user wishes to return on, null if one-way flight
     * @param returnDateMax   if only a single date is entered the Max date will be equal to the Min date
     * @param travelers - how many travelers, split into age categories
     * @param nonstop - True if the user requested nonstop, False if the user requested NOT nonstop, and null if the user did not mention this criteria
     * @param seatClass - array of seat classes (eg. economy, business, etc) requested by the user
     * @param airlines - array of airline codes requested by the user
     * @param redeye - True if the user requested Red Eye flight, False if the user requested NOT Red Eye flight, and null if the user did not mention this criteria
     * @param food - text describing food in flight as requested by the user, null if not mentioned
     * @param seatType - window/aisle seats, or null if not mentioned
     * @param sortBy - how should the results be sorted (eg. price, date, etc..), or null if not mentioned
     * @param sortOrder - ascending or descending or null if not mentioned
     */
    CallbackResult handleFlightSearch(Context context,
                                     boolean isComplete, EvaLocation origin, EvaLocation destination,
                                     Date departDateMin, Date departDateMax,
                                     Date returnDateMin, Date returnDateMax,
                                     EvaTravelers travelers,
                                     Boolean nonstop,
                                     SeatClass[] seatClass,
                                     String[] airlines,
                                     Boolean redeye,
                                     FoodType food,
                                     SeatType seatType,
                                     SortEnum sortBy, SortOrderEnum sortOrder);
}
